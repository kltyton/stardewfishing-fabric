package com.bonker.stardewfishing.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Divider;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;
import com.bonker.stardewfishing.client.animation.Shake;

public class RenderUtil {
    private static final int defaultColor = 0xFFFFFFFF;
    private static int color = defaultColor;

    public static void blitF(DrawContext guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(x - (int) x, y - (int) y);
        guiGraphics.drawTexture(pipeline, texture, (int) x, (int) y, uOffset, vOffset, uWidth, vHeight, 256, 256, color);
        guiGraphics.getMatrices().popMatrix();
    }

    public static void fillF(DrawContext guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
        if (minX < maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            float j = minY;
            minY = maxY;
            maxY = j;
        }

        guiGraphics.fill(MathHelper.floor(minX), MathHelper.floor(minY),
                MathHelper.ceil(maxX), MathHelper.ceil(maxY), color);
    }

    public static void drawRotatedAround(Matrix3x2fStack poseStack, float radians, float pivotX, float pivotY, Runnable runnable) {
        poseStack.pushMatrix();
        poseStack.rotateAbout(radians, pivotX, pivotY);
        runnable.run();
        poseStack.popMatrix();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        color = ColorHelper.getWhite(alpha);
        runnable.run();
        color = defaultColor;
    }

    public static void drawWithBlend(Runnable runnable) {
//        RenderSystem.enableBlend();
        runnable.run();
//        RenderSystem.disableBlend();
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
        int i = MathHelper.ceilDiv(pTarget, pTotal);
        return new Divider(pTarget, i);
    }

    public static void blitRepeatingF(DrawContext guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight, int sourceWidth, int sourceHeight) {
        int width;
        for (IntIterator intiterator = slices(uWidth, sourceWidth); intiterator.hasNext(); x += width) {
            width = intiterator.nextInt();
            int du = (sourceWidth - width) / 2;

            int height;
            for(IntIterator iterator = slices(vHeight, sourceHeight); iterator.hasNext(); y += height) {
                height = iterator.nextInt();
                int dv = (sourceHeight - height) / 2;
                blitF(guiGraphics, pipeline, texture, x, y, uOffset + du, vOffset + dv, width, height);
            }
        }
    }

    public static void renderItemF(DrawContext guiGraphics, ItemStack item, float x, float y) {
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(x - (int) x, y - (int) y);
        guiGraphics.drawItem(item, (int) x, (int) y);
        guiGraphics.drawStackOverlay(MinecraftClient.getInstance().textRenderer, item, (int) x, (int) y);
        guiGraphics.getMatrices().popMatrix();
    }

//    public record BlitFRenderState(
//            RenderPipeline pipeline,
//            TextureSetup textureSetup,
//            Matrix3x2f pose,
//            float x0,
//            float y0,
//            float x1,
//            float y1,
//            float u0,
//            float u1,
//            float v0,
//            float v1,
//            int color,
//            @Nullable ScreenRectangle scissorArea,
//            @Nullable ScreenRectangle bounds
//    ) implements GuiElementRenderState {
//        public BlitFRenderState(
//                RenderPipeline pipeline,
//                TextureSetup textureSetup,
//                Matrix3x2f pose,
//                float x0,
//                float y0,
//                float x1,
//                float y1,
//                float u0,
//                float u1,
//                float v0,
//                float v1,
//                int color,
//                @Nullable ScreenRectangle scissorArea
//        ) {
//            this(pipeline, textureSetup, pose, x0, y0, x1, y1, u0, u1, v0, v1, color, scissorArea, getBounds(Mth.floor(x0), Mth.floor(y0), Mth.ceil(x1), Mth.ceil(y1), pose, scissorArea));
//        }
//
//        @Override
//        public void buildVertices(VertexConsumer vertexConsumer) {
//            vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(color);
//            vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(color);
//            vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color);
//            vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(color);
//        }
//
//        private static @Nullable ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
//            ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
//            return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
//        }
//    }

    public record ColoredRectangleFRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int col1,
            int col2,
            @Nullable ScreenRect scissorArea,
            @Nullable ScreenRect bounds
    ) implements SimpleGuiElementRenderState {
        public ColoredRectangleFRenderState(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                Matrix3x2fc pose,
                float x0,
                float y0,
                float x1,
                float y1,
                int color,
                @Nullable ScreenRect scissorArea
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    x0,
                    y0,
                    x1,
                    y1,
                    color,
                    color,
                    scissorArea,
                    getBounds(MathHelper.floor(x0), MathHelper.floor(y0), MathHelper.ceil(x1), MathHelper.ceil(y1), pose, scissorArea)
            );
        }

        @Override
        public void setupVertices(VertexConsumer vertexConsumer) {
            vertexConsumer.vertex(this.pose(), this.x0(), this.y0()).color(this.col1());
            vertexConsumer.vertex(this.pose(), this.x0(), this.y1()).color(this.col2());
            vertexConsumer.vertex(this.pose(), this.x1(), this.y1()).color(this.col2());
            vertexConsumer.vertex(this.pose(), this.x1(), this.y0()).color(this.col1());
        }

        private static @Nullable ScreenRect getBounds(
                int x0, int y0, int x1, int y1, Matrix3x2fc pose, @Nullable ScreenRect scissorArea
        ) {
            ScreenRect screenrectangle = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
            return scissorArea != null ? scissorArea.intersection(screenrectangle) : screenrectangle;
        }
    }
}
