package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.input.ScreenKeyMapping;
import com.bonker.stardewfishing.common.init.SFBlockEntities;
import com.bonker.stardewfishing.common.init.SFItems;
import com.bonker.stardewfishing.common.init.SFParticles;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.proxy.MinigameModifiersSupplier;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class StardewFishingClient implements ClientModInitializer {
    public static final KeyMapping MINIGAME_BUTTON = new ScreenKeyMapping(
            "key.stardew_fishing.minigame_button",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            "key.categories.stardew_fishing");

    @Nullable
    public static MinigameModifiersSupplier modifiersSupplier;

    @Override
    public void onInitializeClient() {
        StardewFishing.installClientModifiersSupplier(() -> modifiersSupplier == null ? java.util.Map.of() : modifiersSupplier.getData());
        KeyBindingHelper.registerKeyBinding(MINIGAME_BUTTON);

        ClientPlayNetworking.registerGlobalReceiver(S2CStartMinigamePacket.TYPE, (packet, context) ->
                context.client().execute(() -> Minecraft.getInstance().setScreen(new FishingScreen(packet))));
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncModifiersPacket.TYPE, (packet, context) ->
                context.client().execute(() -> StardewFishingClient.modifiersSupplier = () -> packet.data()));

        ItemTooltipCallback.EVENT.register(StardewFishingClient::addTooltip);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.screen instanceof AbstractContainerScreen<?> containerScreen) {
                RodTooltipHandler.tick(containerScreen.hoveredSlot, containerScreen.getMenu().getCarried());
            } else {
                RodTooltipHandler.clear();
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> {
                    if (SFConfig.isInventoryEquippingEnabled()) {
                        RodTooltipHandler.render(graphics, ClientProxy.getPartialTick(),
                                containerScreen.leftPos, containerScreen.topPos, mouseX, mouseY);
                    }
                });
            }
        });

        BlockEntityRenderers.register(SFBlockEntities.FISH_DISPLAY.get(), FishDisplayBER::new);
        ParticleFactoryRegistry.getInstance().register(SFParticles.SPARKLE.get(), SparkleParticle.Provider::new);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(DimensionTextureManager.getOrCreate());
    }

    private static void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> tooltip) {
        // HIGHEST priority: legendary fish data
        if (ItemUtils.isLegendaryFish(stack)) {
            tooltip.add(1, SFItems.LEGENDARY_FISH_TOOLTIP.copy().withStyle(ChatFormatting.BOLD));
            ItemUtils.addCatchTooltip(stack, tooltip);
        }

        // LOWEST priority: rod bobber and rod modifiers
        if (ItemUtils.isFishingRod(stack)) {
            if (!StardewFishing.TIDE_INSTALLED && Minecraft.getInstance().player != null) {
                ItemStack bobber = ItemUtils.getBobber(stack, Minecraft.getInstance().player.registryAccess());

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

            if (ClientProxy.isShiftDown()) {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier").withStyle(StardewFishing.LIGHT_COLOR));
                modifiers.appendTooltip(tooltip);
            } else {
                tooltip.add(Component.translatable("tooltip.stardew_fishing.rod_modifier_shift").withStyle(StardewFishing.LIGHT_COLOR));
            }
        });
    }
}
