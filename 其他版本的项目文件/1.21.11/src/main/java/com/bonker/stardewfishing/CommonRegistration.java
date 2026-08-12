package com.bonker.stardewfishing;

import com.bonker.stardewfishing.data.reload.FishBehaviorReloadListener;
import com.bonker.stardewfishing.data.reload.MinigameModifiersReloadListener;
import com.bonker.stardewfishing.network.SFNetworking;
import com.bonker.stardewfishing.server.command.SFCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Common event wiring: reload listeners, command registration, join/reload
 * modifier sync and the C2S minigame completion handler.
 */
public final class CommonRegistration {

    private CommonRegistration() {
    }

    public static void initialize() {
        SFNetworking.registerTypes();
        SFNetworking.registerCommonReceivers();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FishBehaviorReloadListener.create());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MinigameModifiersReloadListener.getOrCreate());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SFCommands.register(dispatcher, registryAccess));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SFNetworking.syncModifiersToPlayer(handler.player));

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    SFNetworking.syncModifiersToPlayer(player);
                }
            }
        });

    }
}
