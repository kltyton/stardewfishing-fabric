package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

import java.util.function.Supplier;

public final class SFParticles {
    public static final Supplier<SimpleParticleType> SPARKLE = register();

    private static Supplier<SimpleParticleType> register() {
        SimpleParticleType type = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                StardewFishing.resource("sparkle"), FabricParticleTypes.simple());
        return () -> type;
    }

    public static void initialize() {
    }

    private SFParticles() {
    }
}
