package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.common.config.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.BobberEquipEvents;
import com.bonker.stardewfishing.common.networking.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.server.resource.MinigameModifiersSupplier;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;

import org.jspecify.annotations.Nullable;

public class StardewFishingClient implements ClientModInitializer {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(StardewFishing.identifier("keys"));

    public static final KeyMapping MINIGAME_BUTTON = new KeyMapping(
            "key.stardew_fishing.minigame_button",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            CATEGORY);

    @Nullable
    public static MinigameModifiersSupplier modifiersSupplier;

    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(StardewFishing.MODID, ModConfig.Type.CLIENT, SFConfig.CLIENT_SPEC);

        KeyMappingHelper.registerKeyMapping(MINIGAME_BUTTON);

        ClientPlayNetworking.registerGlobalReceiver(S2CStartMinigamePacket.TYPE, S2CStartMinigamePacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncModifiersPacket.TYPE, S2CSyncModifiersPacket::handle);

        ClientEvents.register();

        BobberEquipEvents.CLIENT_HANDLER = (player, event) -> RodTooltipHandler.addShake(event.slot(), event.equipped());
    }

    public static void openFishingScreen(S2CStartMinigamePacket packet) {
        Minecraft.getInstance().gui.setScreen(new FishingScreen(packet));
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
