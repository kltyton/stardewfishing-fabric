package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public final class SFParticles {
    public static final SimpleParticleType SPARKLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            StardewFishing.identifier("sparkle"),
            FabricParticleTypes.simple(false)
    );

    private SFParticles() {
    }
}
