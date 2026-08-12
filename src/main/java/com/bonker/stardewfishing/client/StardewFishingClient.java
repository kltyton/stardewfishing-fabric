package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.event.ClientEvents;
import com.bonker.stardewfishing.config.SFConfig;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;

public final class StardewFishingClient implements ClientModInitializer {
    public static final KeyMapping MINIGAME_BUTTON = new KeyMapping(
            "key.stardew_fishing.minigame_button",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            "key.categories.stardew_fishing");

    @Override
    public void onInitializeClient() {
        ClientEvents.register();
        ForgeConfigRegistry.INSTANCE.register(StardewFishing.MODID, ModConfig.Type.CLIENT, SFConfig.CLIENT_SPEC);
    }
}
