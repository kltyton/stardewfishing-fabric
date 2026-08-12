package com.bonker.stardewfishing.client.particle;

import com.bonker.stardewfishing.registry.SFItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SparkleParticle extends TextureSheetParticle {
    private static final Vector3f COLOR = new Vector3f();
    private static final float SIZE = 0.1F;

    static {
        COLOR.x = FastColor.ARGB32.red(SFItems.LEGENDARY_FISH_COLOR) / 255F;
        COLOR.y = FastColor.ARGB32.green(SFItems.LEGENDARY_FISH_COLOR) / 255F;
        COLOR.z = FastColor.ARGB32.blue(SFItems.LEGENDARY_FISH_COLOR) / 255F;
    }

    private final SpriteSet sprites;

    private SparkleParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ);
        this.sprites = sprites;
        quadSize = 0;
        setSpriteFromAge(sprites);
        setLifetime(120);
        setAlpha(0.8F);
        setColor(COLOR.x, COLOR.y, COLOR.z);
    }

    @Override
    public void tick() {
        super.tick();

        float x = age / (lifetime / 2F);
        if (x > 1) {
            x = 2 - x;
        }
        quadSize = Mth.sin((x * Mth.PI) / 2) * SIZE;

        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            return new SparkleParticle(pLevel, pX, pY, pZ, sprite);
        }
    }
}
