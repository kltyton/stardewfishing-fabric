package com.kltyton.stardewfishingFabric.client.util;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public class RenderUtil {
    private static float alpha = 1.0F;

    public static void blitF(DrawContext context, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        int argb = ((int) (alpha * 255.0F) << 24) | 0x00FFFFFF;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, (int) x, (int) y, (float) uOffset, (float) vOffset, uWidth, vHeight, 256, 256, argb);
    }

    public static void fillF(DrawContext context, float pMinX, float pMinY, float pMaxX, float pMaxY, float pZ, int pColor) {
        if (pMinX > pMaxX) {
            float temp = pMinX;
            pMinX = pMaxX;
            pMaxX = temp;
        }

        if (pMinY > pMaxY) {
            float temp = pMinY;
            pMinY = pMaxY;
            pMaxY = temp;
        }

        context.fill((int) pMinX, (int) pMinY, (int) pMaxX, (int) pMaxY, pColor);
    }

    public static void drawRotatedAround(Matrix3x2fStack matrices, float radians, float pivotX, float pivotY, Runnable runnable) {
        matrices.pushMatrix();
        matrices.translate(pivotX, pivotY);
        matrices.rotate(radians);
        matrices.translate(-pivotX, -pivotY);
        runnable.run();
        matrices.popMatrix();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        RenderUtil.alpha = alpha;
        runnable.run();
        RenderUtil.alpha = 1.0F;
    }

    public static void drawWithBlend(Runnable runnable) {
        runnable.run();
    }

    public static void drawWithShake(Matrix3x2fStack matrices, Shake shake, float partialTick, boolean doShake, Runnable runnable) {
        if (doShake) {
            matrices.pushMatrix();
            matrices.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick));
        }

        runnable.run();

        if (doShake) {
            matrices.popMatrix();
        }
    }
}
