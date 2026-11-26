package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.client.ClientEvents;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @WrapMethod(method = "play")
    private void stardewFishing$transformSound(SoundInstance instance, Operation<Void> original) {
        SoundInstance transformed = ClientEvents.transformSound(instance);
        if (transformed != null) original.call(transformed);
    }
}
