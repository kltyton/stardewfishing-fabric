package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.gameplay.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Inject(method = "handleSlotClick", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$handleBobber(PlayerEntity player, ClickType clickType, Slot slot,
                                             ItemStack stack, ItemStack cursorStack,
                                             CallbackInfoReturnable<Boolean> cir) {
        ScreenHandler handler = (ScreenHandler) (Object) this;
        StackReference cursor = StackReference.of(handler::getCursorStack, handler::setCursorStack);
        if (ItemUtils.handleStackedOnRod(stack, cursorStack, slot, clickType, player, cursor)) {
            cir.setReturnValue(true);
        }
    }
}
