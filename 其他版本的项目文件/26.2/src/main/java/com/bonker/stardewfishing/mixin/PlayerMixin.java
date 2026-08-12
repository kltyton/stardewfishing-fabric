package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.registry.SFAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void addStardewAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue();
        for (var attribute : SFAttributes.ALL) {
            builder.add(attribute);
        }
    }
}
