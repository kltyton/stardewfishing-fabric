package com.bonker.stardewfishing.mixin.client;

import com.bonker.stardewfishing.client.event.ClientEvents;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$replaceSound(SoundInstance sound, CallbackInfo ci) {
        SoundInstance replacement = ClientEvents.replaceSound(sound);
        if (replacement == null) {
            ci.cancel();
        } else if (replacement != sound) {
            ((SoundEngine) (Object) this).play(replacement);
            ci.cancel();
        }
    }
}
