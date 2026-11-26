package com.bonker.stardewfishing.client.event;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.BobberEquipEvents;
import com.bonker.stardewfishing.client.ClientFishingScreenOpener;
import com.bonker.stardewfishing.client.minigame.FishingScreen;
import com.bonker.stardewfishing.client.particle.SparkleParticle;
import com.bonker.stardewfishing.client.render.blockentity.FishDisplayBER;
import com.bonker.stardewfishing.client.resource.DimensionTextureManager;
import com.bonker.stardewfishing.client.tooltip.RodTooltipHandler;
import com.bonker.stardewfishing.common.block.FishDisplayBlock;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import com.bonker.stardewfishing.network.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.mixin.client.AbstractContainerScreenAccessor;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFBlocks;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.screen instanceof AbstractContainerScreen<?> containerScreen) {
                AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
                RodTooltipHandler.tick(accessor.stardewFishing$getHoveredSlot(), containerScreen.getMenu().getCarried());
            } else {
                RodTooltipHandler.clear();
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenEvents.afterExtract(screen).register((renderedScreen, graphics, mouseX, mouseY, partialTick) -> {
                    if (SFConfig.isInventoryEquippingEnabled()) {
                        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
                        RodTooltipHandler.render(graphics, ClientFishingScreenOpener.getPartialTick(),
                                accessor.stardewFishing$getLeftPos(), accessor.stardewFishing$getTopPos(), mouseX, mouseY);
                    }
                });
            }
        });

        ItemTooltipCallback.EVENT.register((stack, context, flag, tooltip) -> appendTooltip(stack, isShiftDown(), tooltip));
        BobberEquipEvents.CHANGED.register(RodTooltipHandler::addShake);

        BlockEntityRenderers.register(SFBlockEntities.FISH_DISPLAY, FishDisplayBER::new);
        ParticleProviderRegistry.getInstance().register(SFParticles.SPARKLE, SparkleParticle.Provider::new);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                StardewFishing.identifier("dimension_texture_manager"), DimensionTextureManager.getOrCreate());

        ClientPlayNetworking.registerGlobalReceiver(S2CStartMinigamePacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncModifiersPacket.TYPE, (payload, context) -> payload.handle());
    }

    private static void appendTooltip(ItemStack stack, boolean shiftDown, java.util.List<Component> tooltip) {
        if (FishingItemSupport.isLegendaryFish(stack)) {
            tooltip.add(1, SFItems.LEGENDARY_FISH_TOOLTIP.copy().withStyle(ChatFormatting.BOLD));
            FishingItemSupport.addCatchTooltip(stack, tooltip);
        }
        if (stack.is(SFBlocks.FISH_DISPLAY.asItem())) {
            tooltip.add(1, FishDisplayBlock.TOOLTIP);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (FishingItemSupport.isFishingRod(stack) && minecraft.level != null) {
            ItemStack bobber = FishingItemSupport.getBobber(stack, minecraft.level.registryAccess());
            if (bobber.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.stardew_fishing."
                                + (SFConfig.isInventoryEquippingEnabled() ? "no_bobber" : "no_bobber_attach_disabled"))
                        .withStyle(StardewFishing.LIGHT_COLOR));
            } else {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.bobber",
                                bobber.getDisplayName().copy().withStyle(StardewFishing.LIGHT_COLOR))
                        .withStyle(StardewFishing.DARK_COLOR));
            }
        }

        StardewFishing.getModifiers(stack).ifPresent(modifiers -> {
            if (!tooltip.isEmpty() && !tooltip.getLast().getString().isEmpty()) {
                tooltip.add(Component.empty());
            }
            if (shiftDown) {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier").withStyle(StardewFishing.LIGHT_COLOR));
                modifiers.appendTooltip(tooltip);
            } else {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier_shift").withStyle(StardewFishing.LIGHT_COLOR));
            }
        });
    }

    private static boolean isShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static @Nullable SoundInstance replaceSound(SoundInstance instance) {
        try {
            SoundEvent replacement = null;
            if (instance instanceof SimpleSoundInstance && instance.getIdentifier().getNamespace().equals("minecraft")) {
                replacement = switch (instance.getIdentifier().getPath()) {
                    case "entity.fishing_bobber.throw" -> SFSoundEvents.CAST;
                    case "entity.fishing_bobber.splash" -> SFSoundEvents.FISH_BITE;
                    case "entity.fishing_bobber.retrieve" -> retrieveSound(instance);
                    default -> null;
                };
            }

            if (replacement != null) {
                return new SimpleSoundInstance(replacement, SoundSource.MASTER, 1.0F, 1.0F,
                        SoundInstance.createUnseededRandom(), instance.getX(), instance.getY(), instance.getZ());
            }
            if (SFConfig.isolateAudioCues()
                    && !instance.getIdentifier().getNamespace().equals(StardewFishing.MODID)
                    && Minecraft.getInstance().screen instanceof FishingScreen) {
                return null;
            }
        } catch (RuntimeException exception) {
            StardewFishing.LOGGER.error("An exception occurred while trying to replace a sound event.", exception);
        }
        return instance;
    }

    private static @Nullable SoundEvent retrieveSound(SoundInstance instance) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Player player = Minecraft.getInstance().level.getNearestPlayer(
                instance.getX(), instance.getY(), instance.getZ(), 1, false);
        return player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM : SFSoundEvents.FISH_HIT;
    }
}
