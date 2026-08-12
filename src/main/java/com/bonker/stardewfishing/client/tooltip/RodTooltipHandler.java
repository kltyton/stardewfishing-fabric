package com.bonker.stardewfishing.client.tooltip;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.ui.animation.Animation;
import com.bonker.stardewfishing.client.ui.layout.ContainerOverlayLayout;
import com.bonker.stardewfishing.client.ui.animation.Shake;
import com.bonker.stardewfishing.client.ui.render.RenderUtil;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class RodTooltipHandler {
    private static final ResourceLocation TEXTURE = StardewFishing.resource("textures/gui/tooltip.png");
    private static final Multimap<Slot, Tooltip> MAP = HashMultimap.create();
    private static int soundTimer = 0;

    public static void tick(Slot hovered, ItemStack carried) {
        if (soundTimer > 0) {
            soundTimer--;
        }

        if (SFConfig.isInventoryEquippingEnabled() && hovered != null && !MAP.containsKey(hovered)) {
            ItemStack stack = hovered.getItem();
            if (FishingItemSupport.isFishingRod(stack)) {
                MAP.put(hovered, new Tooltip(hovered));
            }
        }

        MAP.entries().removeIf(entry -> entry.getValue().tick(hovered, carried));
    }

    public static void clear() {
        MAP.clear();
    }

    public static void render(GuiGraphics guiGraphics, float partialTick,
                              int leftPos, int topPos, int mouseX, int mouseY) {
        MAP.values().forEach(tooltip -> tooltip.render(guiGraphics, partialTick, leftPos, topPos, mouseX, mouseY));
    }

    public static void addShake(Slot slot, boolean equip) {
        for (Map.Entry<Slot, Tooltip> entry : MAP.entries()) {
            if (entry.getKey().index == slot.index) {
                entry.getValue().setShake(equip ? 6 : 2);
            }
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(equip ? SFSoundEvents.EQUIP : SFSoundEvents.UNEQUIP, 1.0F));
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
            stack = slot.getItem().copy();
        }

        public boolean tick(Slot hovered, ItemStack carried) {
            boolean isRod = true;
            if (!ItemStack.matches(slot.getItem(), stack)) {
                if (FishingItemSupport.isFishingRod(slot.getItem())) {
                    stack = slot.getItem().copy();
                } else {
                    isRod = false;
                }
            }

            boolean showMouse = shakeTicks == 0;
            if (isRod && hovered == slot) {
                slotAnim.addValue(1/3F, 0, 1);
                showMouse = showMouse && FishingItemSupport.isBobber(carried);

                if (!hoveredLastTick) {
                    if (soundTimer == 0) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SFSoundEvents.getDwop(true), 1.0F));
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
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SFSoundEvents.getDwop(false), 1.0F));
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

        public void render(GuiGraphics guiGraphics, float partialTick,
                           int leftPos, int topPos, int mouseX, int mouseY) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 370);

            RenderUtil.drawWithShake(guiGraphics.pose(), shake, partialTick, true, () -> {
                RenderUtil.drawWithBlend(() -> renderSlot(guiGraphics, partialTick, leftPos, topPos));
            });
            RenderUtil.drawWithBlend(() -> renderMouse(guiGraphics, partialTick, mouseX, mouseY));

            guiGraphics.pose().popPose();
        }

        private void renderSlot(GuiGraphics guiGraphics, float partialTick, int leftPos, int topPos) {
            float anim = slotAnim.getInterpolated(partialTick);
            if (anim <= 0) return;
            float x = (1 / anim) * ContainerOverlayLayout.screenX(slot.x + 8, leftPos);
            float y = (1 / anim) * ContainerOverlayLayout.screenY(slot.y + 8, topPos);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(anim, anim, 1);

            RenderUtil.blitF(guiGraphics, TEXTURE, x - 35, y - 12, 0, 0, 29, 27);
            RenderUtil.renderItemF(guiGraphics, FishingItemSupport.getBobber(stack), x - 30, y - 8);

            guiGraphics.pose().popPose();
        }

        private void renderMouse(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            float anim = mouseAnim.getInterpolated(partialTick);
            if (anim <= 0) return;
            float x = (1 / anim) * (mouseX);
            float y = (1 / anim) * (mouseY);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(anim, anim, 1);

            RenderUtil.blitF(guiGraphics, TEXTURE, x + 3, y + 3, 33, 0, 11, 19);

            guiGraphics.pose().popPose();
        }
    }
}
