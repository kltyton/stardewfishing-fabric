package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.common.item.FishingItemSupport;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$handleBobber(ItemStack carried, Slot slot, ClickAction clickAction,
                                             Player player, SlotAccess carriedSlotAccess,
                                             CallbackInfoReturnable<Boolean> cir) {
        ItemStack fishingRod = (ItemStack) (Object) this;
        if (FishingItemSupport.handleStackedOnRod(fishingRod, carried, slot, clickAction, player, carriedSlotAccess)) {
            cir.setReturnValue(true);
        }
    }
}
