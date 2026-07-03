package com.kltyton.stardewfishingFabric.client.util;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class Shake {
    private final Random random = new Random();

    private float strength;
    private float strengthSqr;
    private int interval;

    private float lastX = 0, lastY = 0;
    private float x = 0, y = 0;
    private int timer = 0;

    public Shake(float strength, int interval) {
        setValues(strength, interval);
    }

    public void setValues(float strength, int interval) {
        this.strength = strength;
        this.strengthSqr = (strength * 0.5F) * (strength * 0.5F);
        this.interval = interval;
    }

    public void tick() {
        if (++timer >= interval) {
            timer = 0;

            lastX = x;
            lastY = y;

            while (distSqr() < strengthSqr) {
                x = MathHelper.clamp(lastX + random.nextFloat(-strength, strength), -strength, strength);
                y = MathHelper.clamp(lastY + random.nextFloat(-strength, strength), -strength, strength);
            }
        }
    }

    public float getXOffset(float partialTick) {
        return lastX + partialTick * (x - lastX) * ((float) timer / interval);
    }

    public float getYOffset(float partialTick) {
        return lastY + partialTick * (y - lastX) * ((float) timer / interval);
    }

    private float distSqr() {
        return (x - lastX) * (x - lastX) + (y - lastY) * (y - lastY);
    }
}
