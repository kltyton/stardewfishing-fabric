package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.animation.Animation;
import com.bonker.stardewfishing.client.ui.layout.ContainerOverlayLayout;
import com.bonker.stardewfishing.client.render.RenderUtil;
import com.bonker.stardewfishing.client.animation.Shake;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class RodTooltipHandler {
    private static final Identifier TEXTURE = StardewFishing.identifier("textures/gui/tooltip.png");
    private static final Multimap<Slot, Tooltip> MAP = HashMultimap.create();
    private static int soundTimer = 0;

    public static void tick(Slot hovered, ItemStack carried) {
        if (soundTimer > 0) {
            soundTimer--;
        }

        if (SFConfig.isInventoryEquippingEnabled() && hovered != null && !MAP.containsKey(hovered)) {
            ItemStack stack = hovered.getStack();
            if (ItemUtils.isFishingRod(stack)) {
                MAP.put(hovered, new Tooltip(hovered));
            }
        }

        MAP.entries().removeIf(entry -> entry.getValue().tick(hovered, carried));
    }

    public static void clear() {
        MAP.clear();
    }

    public static void render(DrawContext guiGraphics, float partialTick,
                              int containerX, int containerY, int mouseX, int mouseY) {
        MAP.values().forEach(tooltip ->
                tooltip.render(guiGraphics, partialTick, containerX, containerY, mouseX, mouseY));
    }

    public static void addShake(Slot slot, boolean equip) {
        for (Map.Entry<Slot, Tooltip> entry : MAP.entries()) {
            if (entry.getKey().id == slot.id) {
                entry.getValue().setShake(equip ? 6 : 2);
            }
        }

        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(equip ? SFSoundEvents.EQUIP : SFSoundEvents.UNEQUIP, 1.0F));
    }

    public static class Tooltip {
        private final Animation slotAnim = new Animation(0);
        private final Animation mouseAnim = new Animation(0);
        private final Shake shake = new Shake(2F, 1);
        private final Slot slot;
        private ItemStack stack;
        private int shakeDuration = 1;
        private int shakeTicks = 0;
        private boolean hoveredLastTick = false;

        public Tooltip(Slot slot) {
            this.slot = slot;
            stack = slot.getStack().copy();
        }

        public boolean tick(Slot hovered, ItemStack carried) {
            boolean isRod = true;
            if (!slot.getStack().equals(stack)) {
                if (ItemUtils.isFishingRod(slot.getStack())) {
                    stack = slot.getStack().copy();
                } else {
                    isRod = false;
                }
            }

            boolean showMouse = shakeTicks == 0;
            if (isRod && hovered == slot) {
                slotAnim.addValue(1/3F, 0, 1);
                showMouse = showMouse && ItemUtils.isBobber(carried);

                if (!hoveredLastTick) {
                    if (soundTimer == 0) {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SFSoundEvents.DWOP, 1.0F));
                        soundTimer = 4;
                    }
                    hoveredLastTick = true;
                }
            } else {
                slotAnim.addValue(-1/3F, 0, 1);
                showMouse = false;
                if (slotAnim.getInterpolated(0) == 0) {
                    return true;
                }

                if (hoveredLastTick) {
                    if (soundTimer == 0) {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SFSoundEvents.DWOP_REVERSE, 1.0F));
                    }
                    hoveredLastTick = false;
                }
            }

            shake.setValues(2.5F * shakeTicks / shakeDuration, 1);
            shakeTicks = Math.max(0, shakeTicks - 1);
            shake.tick();

            if (showMouse) {
                mouseAnim.addValue(1 / 3F, 0, 1);
            } else {
                mouseAnim.addValue(-1 / 3F, 0, 1);
            }

            return false;
        }

        public void setShake(int duration) {
            shakeDuration = duration;
            shakeTicks = duration;
        }

        public void render(DrawContext guiGraphics, float partialTick,
                           int containerX, int containerY, int mouseX, int mouseY) {
            guiGraphics.getMatrices().pushMatrix();
            guiGraphics.getMatrices().translate(0, 0/*, 370*/);

            RenderUtil.drawWithShake(guiGraphics.getMatrices(), shake, partialTick, true, () -> {
                RenderUtil.drawWithBlend(() -> renderSlot(guiGraphics, partialTick, containerX, containerY));
            });
            RenderUtil.drawWithBlend(() -> renderMouse(guiGraphics, partialTick, mouseX, mouseY));

            guiGraphics.getMatrices().popMatrix();
        }

        private void renderSlot(DrawContext guiGraphics, float partialTick, int containerX, int containerY) {
            if (MinecraftClient.getInstance().world == null) return;

            float anim = slotAnim.getInterpolated(partialTick);
            float x = (1 / anim) * ContainerOverlayLayout.screenX(slot.x + 8, containerX);
            float y = (1 / anim) * ContainerOverlayLayout.screenY(slot.y + 8, containerY);

            guiGraphics.getMatrices().pushMatrix();
            guiGraphics.getMatrices().scale(anim, anim);

            RenderUtil.blitF(guiGraphics, RenderPipelines.GUI_TEXTURED, TEXTURE, x - 35, y - 12, 0, 0, 29, 27);
            RenderUtil.renderItemF(guiGraphics, ItemUtils.getBobber(stack, MinecraftClient.getInstance().world.getRegistryManager()), x - 30, y - 8);

            guiGraphics.getMatrices().popMatrix();
        }

        private void renderMouse(DrawContext guiGraphics, float partialTick, int mouseX, int mouseY) {
            float anim = mouseAnim.getInterpolated(partialTick);
            float x = (1 / anim) * (mouseX);
            float y = (1 / anim) * (mouseY);

            guiGraphics.getMatrices().pushMatrix();
            guiGraphics.getMatrices().scale(anim, anim);

            RenderUtil.blitF(guiGraphics, RenderPipelines.GUI_TEXTURED, TEXTURE, x + 3, y + 3, 33, 0, 11, 19);

            guiGraphics.getMatrices().popMatrix();
        }
    }
}
