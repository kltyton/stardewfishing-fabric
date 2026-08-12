package com.bonker.stardewfishing.client.render;

import com.bonker.stardewfishing.client.animation.Shake;
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

public final class GuiRenderSupport {
    private static final int defaultColor = 0xFFFFFFFF;
    private static int color = defaultColor;

    private GuiRenderSupport() {
    }

    public static void blitF(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x - (int) x, y - (int) y);
        guiGraphics.blit(pipeline, texture, (int) x, (int) y, uOffset, vOffset, uWidth, vHeight, 256, 256, color);
        guiGraphics.pose().popMatrix();
    }

    public static void fillF(GuiGraphicsExtractor guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
        if (minX > maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY > maxY) {
            float j = minY;
            minY = maxY;
            maxY = j;
        }

        guiGraphics.fill(RenderPipelines.GUI, Mth.floor(minX), Mth.floor(minY), Mth.ceil(maxX), Mth.ceil(maxY), color);
    }

    public static void drawRotatedAround(Matrix3x2fStack poseStack, float radians, float pivotX, float pivotY, Runnable runnable) {
        poseStack.pushMatrix();
        poseStack.rotateAbout(radians, pivotX, pivotY);
        runnable.run();
        poseStack.popMatrix();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        color = ARGB.white(alpha);
        runnable.run();
        color = defaultColor;
    }

    public static void drawWithBlend(Runnable runnable) {
        runnable.run();
    }

    public static void drawWithShake(Matrix3x2fStack poseStack, Shake shake, float partialTick, boolean doShake, Runnable runnable) {
        if (doShake) {
            poseStack.pushMatrix();
            poseStack.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick));
        }

        runnable.run();

        if (doShake) {
            poseStack.popMatrix();
        }
    }

    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }

    public static void blitRepeatingF(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight, int sourceWidth, int sourceHeight) {
        int width;
        for (IntIterator intiterator = slices(uWidth, sourceWidth); intiterator.hasNext(); x += width) {
            width = intiterator.nextInt();
            int du = (sourceWidth - width) / 2;

            int height;
            for (IntIterator iterator = slices(vHeight, sourceHeight); iterator.hasNext(); y += height) {
                height = iterator.nextInt();
                int dv = (sourceHeight - height) / 2;
                blitF(guiGraphics, pipeline, texture, x, y, uOffset + du, vOffset + dv, width, height);
            }
        }
    }

    public static void renderItemF(GuiGraphicsExtractor guiGraphics, ItemStack item, float x, float y) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x - (int) x, y - (int) y);
        guiGraphics.item(item, (int) x, (int) y);
        guiGraphics.itemDecorations(Minecraft.getInstance().font, item, (int) x, (int) y);
        guiGraphics.pose().popMatrix();
    }
}
