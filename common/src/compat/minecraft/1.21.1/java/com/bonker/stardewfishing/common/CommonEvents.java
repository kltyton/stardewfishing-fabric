package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.networking.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.server.SFCommands;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.MinigameModifiersReloadListener;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class CommonEvents {
    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(S2CStartMinigamePacket.TYPE, S2CStartMinigamePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncModifiersPacket.TYPE, S2CSyncModifiersPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCompleteMinigamePacket.TYPE, C2SCompleteMinigamePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePacket.TYPE,
                (packet, context) -> packet.handle(context.player()));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FishBehaviorReloadListener.create());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(MinigameModifiersReloadListener.getOrCreate());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SFCommands.register(dispatcher, registryAccess));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncModifiers(handler.player));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> syncModifiers(player));
    }

    private static void syncModifiers(net.minecraft.server.level.ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, S2CSyncModifiersPacket.TYPE)) {
            ServerPlayNetworking.send(player,
                    new S2CSyncModifiersPacket(MinigameModifiersReloadListener.getOrCreate().getData()));
        } else {
            StardewFishing.LOGGER.debug("Client {} cannot receive modifier sync payload", player.getScoreboardName());
        }
    }

    private CommonEvents() {
    }
}
