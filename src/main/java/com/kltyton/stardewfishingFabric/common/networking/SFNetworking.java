package com.kltyton.stardewfishingFabric.common.networking;

import com.kltyton.stardewfishingFabric.common.FishBehavior;
import com.kltyton.stardewfishingFabric.common.FishingDataStorage;
import com.kltyton.stardewfishingFabric.common.FishingHookLogic;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SFNetworking {
    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(S2CStartMinigamePacket.ID, S2CStartMinigamePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCompleteMinigamePacket.ID, C2SCompleteMinigamePacket.PACKET_CODEC);
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(S2CStartMinigamePacket.ID, (payload, context) -> {
            context.client().execute(() -> com.kltyton.stardewfishingFabric.client.ClientEvents.openFishingScreen(payload.behavior(), payload.previewItem().orElse(null)));
        });
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            FishingBobberEntity hook = FishingDataStorage.getHookForPlayer(player);
            context.server().execute(() -> {
                if (hook != null) {
                    FishingHookLogic.endMinigame(player, payload.success(), payload.accuracy(), hook, FishingDataStorage.getItemsForPlayer(player));
                }
            });
        });
    }

    public static void sendToPlayer(ServerPlayerEntity player, net.minecraft.network.packet.CustomPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToServer(net.minecraft.network.packet.CustomPayload packet) {
        ClientPlayNetworking.send(packet);
    }
}
