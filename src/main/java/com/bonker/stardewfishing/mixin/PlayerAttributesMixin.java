package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.registry.SFAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttributesMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void stardewFishing$addFishingAttributes(
            CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
        callback.getReturnValue()
                .add(SFAttributes.LINE_STRENGTH)
                .add(SFAttributes.BAR_SIZE)
                .add(SFAttributes.TREASURE_CHANCE_BONUS)
                .add(SFAttributes.GOLDEN_CHEST_BONUS)
                .add(SFAttributes.EXP_MULTIPLIER);
    }
}
