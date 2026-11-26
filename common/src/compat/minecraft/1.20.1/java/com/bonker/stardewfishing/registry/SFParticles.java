package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public final class SFParticles {
    public static final SimpleParticleType SPARKLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, StardewFishing.resource("sparkle"), FabricParticleTypes.simple());

    private SFParticles() {
    }

    public static void initialize() {
    }
}
