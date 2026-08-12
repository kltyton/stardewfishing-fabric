package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.registry.SFAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void stardewFishing$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(Registries.ATTRIBUTE.getEntry(SFAttributes.LINE_STRENGTH))
                .add(Registries.ATTRIBUTE.getEntry(SFAttributes.BAR_SIZE))
                .add(Registries.ATTRIBUTE.getEntry(SFAttributes.TREASURE_CHANCE_BONUS))
                .add(Registries.ATTRIBUTE.getEntry(SFAttributes.GOLDEN_CHEST_BONUS))
                .add(Registries.ATTRIBUTE.getEntry(SFAttributes.EXP_MULTIPLIER));
    }
}
