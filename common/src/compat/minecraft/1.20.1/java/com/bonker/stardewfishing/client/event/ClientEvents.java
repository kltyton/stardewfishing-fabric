package com.bonker.stardewfishing.client.event;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.BobberEquipEvents;
import com.bonker.stardewfishing.client.StardewFishingClient;
import com.bonker.stardewfishing.client.minigame.FishingScreen;
import com.bonker.stardewfishing.client.network.ClientNetworking;
import com.bonker.stardewfishing.client.particle.SparkleParticle;
import com.bonker.stardewfishing.client.render.blockentity.FishDisplayBER;
import com.bonker.stardewfishing.client.resource.DimensionTextureManager;
import com.bonker.stardewfishing.client.tooltip.RodTooltipHandler;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.mixin.client.AbstractContainerScreenAccessor;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(StardewFishingClient.MINIGAME_BUTTON);

        ClientNetworking.register();
        BobberEquipEvents.CHANGED.register(RodTooltipHandler::addShake);

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
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> {
                    if (SFConfig.isInventoryEquippingEnabled()) {
                        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
                        RodTooltipHandler.render(graphics, tickDelta,
                                accessor.stardewFishing$getLeftPos(), accessor.stardewFishing$getTopPos(),
                                mouseX, mouseY);
                    }
                });
            }
        });

        ItemTooltipCallback.EVENT.register(ClientEvents::appendRodTooltip);

        BlockEntityRenderers.register(SFBlockEntities.FISH_DISPLAY, FishDisplayBER::new);
        ParticleFactoryRegistry.getInstance().register(SFParticles.SPARKLE, SparkleParticle.Provider::new);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(DimensionTextureManager.getOrCreate());
    }

    private static void appendRodTooltip(ItemStack stack, TooltipFlag context, List<Component> tooltip) {
        if (FishingItemSupport.isLegendaryFish(stack)) {
            tooltip.add(Math.min(1, tooltip.size()), SFItems.LEGENDARY_FISH_TOOLTIP.copy());
            FishingItemSupport.addCatchTooltip(stack, tooltip);
        }

        if (FishingItemSupport.isFishingRod(stack) && Minecraft.getInstance().level != null) {
            ItemStack bobber = FishingItemSupport.getBobber(stack);
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

        StardewFishing.getClientModifiers(stack).ifPresent(modifiers -> {
            if (!tooltip.isEmpty() && !tooltip.get(tooltip.size() - 1).getString().isEmpty()) tooltip.add(Component.empty());
            if (Screen.hasShiftDown()) {
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
            if (instance instanceof SimpleSoundInstance && instance.getLocation().getNamespace().equals("minecraft")) {
                replacement = switch (instance.getLocation().getPath()) {
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
                    && !instance.getLocation().getNamespace().equals(StardewFishing.MODID)
                    && Minecraft.getInstance().screen instanceof FishingScreen) {
                return null;
            }
        } catch (RuntimeException exception) {
            StardewFishing.LOGGER.error("An exception occurred while trying to replace a sound event.", exception);
        }
        return instance;
    }

    private static SoundEvent retrieveSound(SoundInstance instance) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Player player = Minecraft.getInstance().level.getNearestPlayer(
                instance.getX(), instance.getY(), instance.getZ(), 1, false);
        return player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM : SFSoundEvents.FISH_HIT;
    }
}
