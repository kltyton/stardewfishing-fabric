package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.registry.SFItems;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public final class SparkleParticle extends BillboardParticle {
    private static final float SIZE = 0.1F;
    private final SpriteProvider sprites;

    private SparkleParticle(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
        super(world, x, y, z, sprites.getFirst());
        this.sprites = sprites;
        scale = 0;
        setMaxAge(120);
        setAlpha(0.8F);
        setColor(ColorHelper.getRed(SFItems.LEGENDARY_FISH_COLOR) / 255F,
                ColorHelper.getGreen(SFItems.LEGENDARY_FISH_COLOR) / 255F,
                ColorHelper.getBlue(SFItems.LEGENDARY_FISH_COLOR) / 255F);
        updateSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float progress = age / (maxAge / 2F);
        if (progress > 1) {
            progress = 2 - progress;
        }
        scale = MathHelper.sin(progress * MathHelper.PI / 2) * SIZE;
        updateSprite(sprites);
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<SimpleParticleType> {
        private final FabricSpriteProvider sprites;

        public Factory(FabricSpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world,
                                                 double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ,
                                                 Random random) {
            return new SparkleParticle(world, x, y, z, sprites);
        }
    }
}
