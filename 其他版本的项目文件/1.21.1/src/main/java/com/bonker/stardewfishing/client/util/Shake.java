package com.bonker.stardewfishing.client.util;

public class Shake {
    private final Animation xAnim = new Animation(0);
    private final Animation yAnim = new Animation(0);

    private float strength;
    private int interval;

    private int timer = 0;

    public Shake(float strength, int interval) {
        setValues(strength, interval);
    }

    public void setValues(float strength, int interval) {
        this.strength = strength;
        this.interval = interval;
    }

    public void tick() {
        if (++timer >= interval) {
            timer = 0;

            xAnim.setValue((float) (Math.random() * strength * 2 + -strength));
            yAnim.setValue((float) (Math.random() * strength * 2 + -strength));
        }
    }

    public float getXOffset(float partialTick) {
        return xAnim.getInterpolated((timer + partialTick) / interval);
    }

    public float getYOffset(float partialTick) {
        return yAnim.getInterpolated((timer + partialTick) / interval);
    }
}
