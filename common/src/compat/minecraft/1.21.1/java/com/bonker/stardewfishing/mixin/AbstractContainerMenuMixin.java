package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.proxy.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "tryItemClickBehaviourOverride", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$equipBobber(Player player, ClickAction action, Slot slot,
                                             ItemStack slotItem, ItemStack carried,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (!SFConfig.isInventoryEquippingEnabled() || action != ClickAction.SECONDARY
                || !ItemUtils.isFishingRod(slotItem)) return;

        ItemStack current = ItemUtils.getBobber(slotItem, player.registryAccess());
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (ItemUtils.isBobber(carried)) {
            if (current.isEmpty()) {
                ItemUtils.setBobber(slotItem, carried.copyWithCount(1), player.registryAccess());
                carried.shrink(1);
                cir.setReturnValue(true);
            } else if (carried.getCount() == 1) {
                ItemUtils.setBobber(slotItem, carried.copyWithCount(1), player.registryAccess());
                menu.setCarried(current.copy());
                cir.setReturnValue(true);
            } else if (ItemStack.isSameItemSameComponents(carried, current)) {
                int amount = Math.min(carried.getMaxStackSize() - carried.getCount(), current.getCount());
                if (amount > 0) {
                    ItemUtils.setBobber(slotItem, current.copyWithCount(current.getCount() - amount), player.registryAccess());
                    carried.grow(amount);
                    cir.setReturnValue(true);
                }
            }
        } else if (carried.isEmpty() && !current.isEmpty()) {
            ItemUtils.setBobber(slotItem, ItemStack.EMPTY, player.registryAccess());
            menu.setCarried(current.copy());
            cir.setReturnValue(true);
        }
    }
}
