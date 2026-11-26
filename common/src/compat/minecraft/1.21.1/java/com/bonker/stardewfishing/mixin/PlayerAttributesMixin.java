package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.common.init.SFAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttributesMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void stardewFishing$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        for (var attribute : SFAttributes.ALL) {
            cir.getReturnValue().add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get()));
        }
    }
}
