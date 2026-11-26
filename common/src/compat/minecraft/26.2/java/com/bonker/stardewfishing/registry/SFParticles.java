package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class SFParticles {
    public static final SimpleParticleType SPARKLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, StardewFishing.identifier("sparkle"), new SimpleParticleType(false) {
            });

    /** Triggers class initialization so all particle types register. */
    public static void register() {
    }
}
