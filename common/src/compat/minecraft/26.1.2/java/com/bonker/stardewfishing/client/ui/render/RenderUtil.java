package com.bonker.stardewfishing.client.ui.render;

import com.bonker.stardewfishing.client.ui.animation.Shake;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class RenderUtil {
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static int color = DEFAULT_COLOR;

    private RenderUtil() {
    }

    public static void blitF(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture,
                             float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x - (int) x, y - (int) y);
        graphics.blit(pipeline, texture, (int) x, (int) y, uOffset, vOffset, uWidth, vHeight, 256, 256, color);
        graphics.pose().popMatrix();
    }

    public static void fillF(GuiGraphicsExtractor graphics, float minX, float minY, float maxX, float maxY, int fillColor) {
        float x0 = Math.min(minX, maxX);
        float y0 = Math.min(minY, maxY);
        float x1 = Math.max(minX, maxX);
        float y1 = Math.max(minY, maxY);
        int baseX = Mth.floor(x0);
        int baseY = Mth.floor(y0);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x0 - baseX, y0 - baseY);
        graphics.fill(RenderPipelines.GUI, baseX, baseY,
                baseX + Mth.ceil(x1 - x0), baseY + Mth.ceil(y1 - y0), fillColor);
        graphics.pose().popMatrix();
    }

    public static void drawRotatedAround(Matrix3x2fStack poseStack, float radians, float pivotX, float pivotY, Runnable runnable) {
        poseStack.pushMatrix();
        poseStack.rotateAbout(radians, pivotX, pivotY);
        runnable.run();
        poseStack.popMatrix();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        color = ARGB.white(alpha);
        try {
            runnable.run();
        } finally {
            color = DEFAULT_COLOR;
        }
    }

    public static void drawWithBlend(Runnable runnable) {
        runnable.run();
    }

    public static void drawWithShake(Matrix3x2fStack poseStack, Shake shake, float partialTick,
                                     boolean doShake, Runnable runnable) {
        if (doShake) {
            poseStack.pushMatrix();
            poseStack.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick));
        }
        try {
            runnable.run();
        } finally {
            if (doShake) {
                poseStack.popMatrix();
            }
        }
    }

    private static IntIterator slices(int target, int total) {
        return new Divisor(target, Mth.positiveCeilDiv(target, total));
    }

    public static void blitRepeatingF(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture,
                                      float x, float y, int uOffset, int vOffset, int uWidth, int vHeight,
                                      int sourceWidth, int sourceHeight) {
        float startY = y;
        int width;
        for (IntIterator xSlices = slices(uWidth, sourceWidth); xSlices.hasNext(); x += width) {
            width = xSlices.nextInt();
            int du = (sourceWidth - width) / 2;
            y = startY;
            int height;
            for (IntIterator ySlices = slices(vHeight, sourceHeight); ySlices.hasNext(); y += height) {
                height = ySlices.nextInt();
                int dv = (sourceHeight - height) / 2;
                blitF(graphics, pipeline, texture, x, y, uOffset + du, vOffset + dv, width, height);
            }
        }
    }

    public static void renderItemF(GuiGraphicsExtractor graphics, ItemStack item, float x, float y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x - (int) x, y - (int) y);
        graphics.item(item, (int) x, (int) y);
        graphics.itemDecorations(Minecraft.getInstance().font, item, (int) x, (int) y);
        graphics.pose().popMatrix();
    }
}
