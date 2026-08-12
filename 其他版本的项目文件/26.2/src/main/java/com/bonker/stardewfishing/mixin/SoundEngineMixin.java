package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.client.ClientEvents;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void replaceOrCancelSound(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        SoundInstance replacement = ClientEvents.replaceSound(instance);
        if (replacement == instance) {
            return;
        }

        if (replacement == null) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        } else {
            cir.setReturnValue(((SoundEngine) (Object) this).play(replacement));
        }
    }
}
