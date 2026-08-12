package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.common.config.SFConfig;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reproduces the NeoForge {@code ItemStackedOnOtherEvent} behavior for attaching bobbers to
 * fishing rods by right-clicking a rod slot in a container while carrying a bobber.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "tryItemClickBehaviourOverride", at = @At("HEAD"), cancellable = true)
    private void handleBobberEquipping(Player player, ClickAction clickAction, Slot slot, ItemStack clicked, ItemStack carried, CallbackInfoReturnable<Boolean> cir) {
        if (!SFConfig.isInventoryEquippingEnabled() || clickAction != ClickAction.SECONDARY) {
            return;
        }

        if (!FishingItemSupport.isFishingRod(clicked)) {
            return;
        }

        ItemStack currentBobber = FishingItemSupport.getBobber(clicked, player.registryAccess());

        boolean equipped = true;
        if (FishingItemSupport.isBobber(carried)) {
            if (currentBobber.isEmpty()) {
                FishingItemSupport.setBobber(clicked, carried.copyWithCount(1), player.registryAccess());
                carried.shrink(1);
                cir.setReturnValue(true);
            } else if (carried.getCount() == 1) {
                FishingItemSupport.setBobber(clicked, carried.copyWithCount(1), player.registryAccess());
                carried.setCount(currentBobber.getCount());
                ((AbstractContainerMenu) (Object) this).setCarried(currentBobber.copy());
                cir.setReturnValue(true);
            } else if (ItemStack.isSameItemSameComponents(carried, currentBobber)) {
                int transferAmount = Math.min(carried.getMaxStackSize() - carried.getCount(), currentBobber.getCount());
                FishingItemSupport.setBobber(clicked, currentBobber.copyWithCount(currentBobber.getCount() - transferAmount), player.registryAccess());
                carried.grow(transferAmount);
                cir.setReturnValue(true);
                equipped = false;
            }
        } else if (!currentBobber.isEmpty() && carried.isEmpty()) {
            FishingItemSupport.setBobber(clicked, ItemStack.EMPTY, player.registryAccess());
            ((AbstractContainerMenu) (Object) this).setCarried(currentBobber.copy());
            cir.setReturnValue(true);
            equipped = false;
        }

        if (cir.isCancelled()) {
            com.bonker.stardewfishing.common.BobberEquipEvents.notify(player, slot, equipped);
        }
    }
}
