package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.common.config.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFBlocks;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.common.block.FishDisplayBlock;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClientEvents {
    private static final Logger LOGGER = StardewFishing.LOGGER;

    private ClientEvents() {
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.gui.screen() instanceof AbstractContainerScreen<?> containerScreen) {
                RodTooltipHandler.tick(containerScreen.hoveredSlot, containerScreen.getMenu().getCarried());
            } else {
                RodTooltipHandler.clear();
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenEvents.afterExtract(screen).register((renderedScreen, guiGraphics, mouseX, mouseY, partialTick) -> {
                    if (SFConfig.isInventoryEquippingEnabled()) {
                        RodTooltipHandler.render(guiGraphics, StardewFishingClient.getPartialTick(),
                                containerScreen.leftPos, containerScreen.topPos, mouseX, mouseY);
                    }
                });
            }
        });

        ItemTooltipCallback.EVENT.register(ClientEvents::appendTooltip);

        BlockEntityRendererRegistry.register(SFBlockEntities.FISH_DISPLAY, FishDisplayBER::new);
        ParticleProviderRegistry.getInstance().register(SFParticles.SPARKLE, SparkleParticle.Provider::new);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(DimensionTextureManager.getOrCreate());
    }

    private static void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> tooltip) {
        if (FishingItemSupport.isLegendaryFish(stack)) {
            tooltip.add(1, SFItems.LEGENDARY_FISH_TOOLTIP.copy().withStyle(ChatFormatting.BOLD));
            FishingItemSupport.addCatchTooltip(stack, tooltip);
        }

        if (stack.is(SFBlocks.FISH_DISPLAY.asItem())) {
            tooltip.add(1, FishDisplayBlock.TOOLTIP);
        }

        if (FishingItemSupport.isFishingRod(stack)) {
            if (!StardewFishing.TIDE_INSTALLED && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
                ItemStack bobber = FishingItemSupport.getBobber(stack, Minecraft.getInstance().level.registryAccess());
                if (bobber.isEmpty()) {
                    tooltip.add(Component.translatable("tooltip.stardew_fishing." + (SFConfig.isInventoryEquippingEnabled() ? "no_bobber" : "no_bobber_attach_disabled"))
                            .withStyle(StardewFishing.LIGHT_COLOR));
                } else {
                    tooltip.add(Component.translatable("tooltip.stardew_fishing.bobber", bobber.getDisplayName().copy().withStyle(StardewFishing.LIGHT_COLOR))
                            .withStyle(StardewFishing.DARK_COLOR));
                }
            }
        }

        StardewFishing.getModifiers(stack).ifPresent(modifiers -> {
            if (!tooltip.getLast().getString().isEmpty()) {
                tooltip.add(Component.empty());
            }

            if (isShiftDown()) {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier").withStyle(StardewFishing.LIGHT_COLOR));
                modifiers.appendTooltip(tooltip);
            } else {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier_shift").withStyle(StardewFishing.LIGHT_COLOR));
            }
        });
    }

    /**
     * Replaces vanilla fishing sounds with the mod's sound events and implements audio isolation
     * while the fishing minigame screen is open. Invoked from the sound engine mixin.
     */
    public static SoundInstance replaceSound(SoundInstance instance) {
        try {
            SoundEvent replacement = null;
            if (instance instanceof SimpleSoundInstance && instance.getIdentifier().getNamespace().equals("minecraft")) {
                replacement = switch (instance.getIdentifier().getPath()) {
                    case "entity.fishing_bobber.throw" -> SFSoundEvents.CAST;
                    case "entity.fishing_bobber.retrieve" -> retrieveSound(instance);
                    case "entity.fishing_bobber.splash" -> SFSoundEvents.FISH_BITE;
                    default -> null;
                };
            }

            if (replacement != null) {
                return new SimpleSoundInstance(
                        replacement,
                        SoundSource.MASTER,
                        1.0F,
                        1.0F,
                        SoundInstance.createUnseededRandom(),
                        instance.getX(),
                        instance.getY(),
                        instance.getZ());
            }

            if (SFConfig.isolateAudioCues()
                    && !instance.getIdentifier().getNamespace().equals(StardewFishing.MODID)
                    && Minecraft.getInstance().gui.screen() instanceof FishingScreen) {
                return null;
            }
        } catch (RuntimeException exception) {
            LOGGER.error("An exception occurred while trying to replace a sound event.", exception);
        }
        return instance;
    }

    private static SoundEvent retrieveSound(SoundInstance instance) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Player player = Minecraft.getInstance().level.getNearestPlayer(instance.getX(), instance.getY(), instance.getZ(), 1, false);
        return player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM : SFSoundEvents.FISH_HIT;
    }

    private static boolean isShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
    }
}
