package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.event.ClientEvents;
import com.bonker.stardewfishing.config.SFConfig;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;

public final class StardewFishingClient implements ClientModInitializer {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(StardewFishing.identifier("keys"));
    public static final KeyMapping MINIGAME_BUTTON = new KeyMapping(
            "key.stardew_fishing.minigame_button",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            CATEGORY
    );

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(MINIGAME_BUTTON);
        ClientEvents.register();
        ConfigRegistry.INSTANCE.register(StardewFishing.MODID, ModConfig.Type.CLIENT, SFConfig.CLIENT_SPEC);
    }

    public static InputConstants.Key minigameKey() {
        return KeyMappingHelper.getBoundKeyOf(MINIGAME_BUTTON);
    }
}
