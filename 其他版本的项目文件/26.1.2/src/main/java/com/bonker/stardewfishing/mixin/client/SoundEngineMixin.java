package com.bonker.stardewfishing.mixin.client;

import com.bonker.stardewfishing.client.event.ClientEvents;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$replaceSound(SoundInstance instance,
                                             CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        SoundInstance replacement = ClientEvents.replaceSound(instance);
        if (replacement == null) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        } else if (replacement != instance) {
            cir.setReturnValue(((SoundEngine) (Object) this).play(replacement));
        }
    }
}
