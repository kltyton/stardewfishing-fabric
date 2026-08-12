package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.client.FishingSoundInterceptor;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$replaceSound(SoundInstance sound,
                                             CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        SoundInstance replacement = FishingSoundInterceptor.replace(sound);
        if (replacement == null) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        } else if (replacement != sound) {
            cir.setReturnValue(((SoundSystem) (Object) this).play(replacement));
        }
    }
}
