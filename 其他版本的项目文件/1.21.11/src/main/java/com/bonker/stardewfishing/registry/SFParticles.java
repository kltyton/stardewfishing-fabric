package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Particle type registrations. */
public final class SFParticles {
    public static final SimpleParticleType SPARKLE =
            Registry.register(Registries.PARTICLE_TYPE, StardewFishing.identifier("sparkle"),
                    FabricParticleTypes.simple(false));

    private SFParticles() {
    }
}
