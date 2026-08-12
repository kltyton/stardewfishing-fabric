package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.network.SFNetworking;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class StardewFishingClient implements ClientModInitializer {
    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(StardewFishing.identifier("keys"));
    public static final KeyBinding MINIGAME_BUTTON = new KeyBinding(
            "key.stardew_fishing.minigame_button",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            CATEGORY);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(MINIGAME_BUTTON);

        ClientPlayNetworking.registerGlobalReceiver(SFNetworking.S2CStartMinigamePayload.ID,
                (payload, context) -> context.client().setScreen(new FishingScreen(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SFNetworking.S2CSyncModifiersPayload.ID,
                (payload, context) -> StardewFishing.ModifiersAccess.install(payload::data));

        ItemTooltipCallback.EVENT.register(StardewFishingClient::addTooltip);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.currentScreen instanceof HandledScreen<?> screen) {
                HandledScreenAccess access = (HandledScreenAccess) screen;
                RodTooltipHandler.tick(access.stardewFishing$getFocusedSlot(),
                        screen.getScreenHandler().getCursorStack());
            } else {
                RodTooltipHandler.clear();
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof HandledScreen<?> handledScreen) {
                ScreenEvents.afterRender(screen).register((rendered, context, mouseX, mouseY, tickDelta) -> {
                    if (SFConfig.isInventoryEquippingEnabled()) {
                        HandledScreenAccess access = (HandledScreenAccess) handledScreen;
                        RodTooltipHandler.render(context, tickDelta,
                                access.stardewFishing$getX(), access.stardewFishing$getY(), mouseX, mouseY);
                    }
                });
            }
        });

        BlockEntityRendererRegistry.register(SFBlockEntities.FISH_DISPLAY, FishDisplayBER::new);
        ParticleFactoryRegistry.getInstance().register(SFParticles.SPARKLE, SparkleParticle.Factory::new);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(DimensionTextureManager.getOrCreate());
    }

    private static void addTooltip(ItemStack stack, Item.TooltipContext context,
                                   TooltipType type, List<Text> tooltip) {
        if (ItemUtils.isLegendaryFish(stack)) {
            tooltip.add(Math.min(1, tooltip.size()),
                    SFItems.LEGENDARY_FISH_TOOLTIP.copy().formatted(Formatting.BOLD));
            ItemUtils.addCatchTooltip(stack, tooltip);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (ItemUtils.isFishingRod(stack) && !StardewFishing.TIDE_INSTALLED && client.world != null) {
            ItemStack bobber = ItemUtils.getBobber(stack, client.world.getRegistryManager());
            if (bobber.isEmpty()) {
                tooltip.add(Text.translatable("tooltip.stardew_fishing."
                        + (SFConfig.isInventoryEquippingEnabled() ? "no_bobber" : "no_bobber_attach_disabled"))
                        .setStyle(StardewFishing.LIGHT_COLOR));
            } else {
                tooltip.add(Text.translatable("tooltip.stardew_fishing.bobber",
                                bobber.getName().copy().setStyle(StardewFishing.LIGHT_COLOR))
                        .setStyle(StardewFishing.DARK_COLOR));
            }
        }

        StardewFishing.getModifiers(stack).ifPresent(modifiers -> {
            if (!tooltip.isEmpty() && !tooltip.getLast().getString().isEmpty()) {
                tooltip.add(Text.empty());
            }
            if (hasShiftDown()) {
                tooltip.add(Text.translatable("tooltip.stardew_fishing.rod_modifier")
                        .setStyle(StardewFishing.LIGHT_COLOR));
                modifiers.appendTooltip(tooltip);
            } else {
                tooltip.add(Text.translatable("tooltip.stardew_fishing.rod_modifier_shift")
                        .setStyle(StardewFishing.LIGHT_COLOR));
            }
        });
    }

    public static boolean hasShiftDown() {
        var window = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
