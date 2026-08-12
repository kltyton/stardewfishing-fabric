package com.kltyton.stardewfishingFabric.common.networking;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.client.ClientFishingScreenOpener;
import com.kltyton.stardewfishingFabric.common.FishingHookLogic;
import com.kltyton.stardewfishingFabric.common.item.FishingItemSupport;
import com.kltyton.stardewfishingFabric.server.fishing.FishingHookState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class SFNetworking {
    public static final ResourceLocation START_MINIGAME = StardewfishingFabric.id("start_minigame");
    public static final ResourceLocation COMPLETE_MINIGAME = StardewfishingFabric.id("complete_minigame");
    public static final ResourceLocation SYNC_MODIFIERS = StardewfishingFabric.id("sync_modifiers");
    private static boolean commonRegistered;
    private static boolean clientRegistered;

    private SFNetworking() {
    }

    public static void registerCommon() {
        if (commonRegistered) return;
        commonRegistered = true;
        ServerPlayNetworking.registerGlobalReceiver(COMPLETE_MINIGAME, (server, player, handler, buf, responseSender) -> {
            C2SCompleteMinigamePacket packet = new C2SCompleteMinigamePacket(buf);
            server.execute(() -> handleComplete(player, packet));
        });
    }

    /** Client registration alias retained for the original Fabric entry point. */
    public static void register() {
        registerClient();
    }

    public static void registerClient() {
        if (clientRegistered) return;
        clientRegistered = true;
        ClientPlayNetworking.registerGlobalReceiver(START_MINIGAME, (client, handler, buf, responseSender) -> {
            S2CStartMinigamePacket packet = new S2CStartMinigamePacket(buf);
            client.execute(() -> ClientFishingScreenOpener.openFishingScreen(packet));
        });
        ClientPlayNetworking.registerGlobalReceiver(SYNC_MODIFIERS, (client, handler, buf, responseSender) -> {
            S2CSyncModifiersPacket packet = new S2CSyncModifiersPacket(buf);
            client.execute(() -> StardewfishingFabric.clientModifiersSupplier = packet::data);
        });
    }

    public static void sendToPlayer(ServerPlayer player, S2CStartMinigamePacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ServerPlayNetworking.send(player, START_MINIGAME, buf);
    }

    public static void sendModifiers(ServerPlayer player, S2CSyncModifiersPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ServerPlayNetworking.send(player, SYNC_MODIFIERS, buf);
    }

    /** Legacy overload used by older integrations. */
    public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf packet) {
        ServerPlayNetworking.send(player, START_MINIGAME, packet);
    }

    public static void sendToServer(C2SCompleteMinigamePacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ClientPlayNetworking.send(COMPLETE_MINIGAME, buf);
    }

    /** Legacy overload used by the original client screen. */
    public static void sendToServer(FriendlyByteBuf packet) {
        ClientPlayNetworking.send(COMPLETE_MINIGAME, packet);
    }

    private static void handleComplete(ServerPlayer player, C2SCompleteMinigamePacket packet) {
        FishingHook hook = player.fishing;
        if (hook == null) {
            StardewfishingFabric.LOGGER.warn("{} tried to complete a minigame that does not exist", player.getScoreboardName());
            return;
        }
        FishingHookState state = FishingHookState.get(hook);
        if (state.stardewFishing$getRewards().isEmpty()) {
            StardewfishingFabric.LOGGER.warn("{} tried to complete a minigame that does not exist", player.getScoreboardName());
            return;
        }

        boolean finiteAccuracy = Double.isFinite(packet.accuracy());
        double accuracy = finiteAccuracy ? Math.max(0.0, Math.min(1.0, packet.accuracy())) : 0.0;
        boolean success = packet.success() && finiteAccuracy;
        boolean gotChest = packet.gotChest() && state.stardewFishing$hasTreasureChest();

        InteractionHand hand = FishingItemSupport.getRodHand(player);
        if (hand == null) {
            FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
            StardewfishingFabric.LOGGER.warn("{} tried to complete a minigame without a fishing rod", player.getScoreboardName());
            return;
        }

        ItemStack rod = player.getItemInHand(hand);
        FishingItemSupport.damageAttachedBobber(rod, player);
        int qualityBoost = state.stardewFishing$getEvent() == null
                ? 0 : state.stardewFishing$getEvent().getQualityBoost();
        FishingHookLogic.endMinigame(player, success, accuracy, gotChest, qualityBoost, rod);

        ItemStack bobber = FishingItemSupport.getBobber(rod).copy();
        rod.hurtAndBreak(1, player, broken -> {
            if (!bobber.isEmpty()) player.spawnAtLocation(bobber);
            player.broadcastBreakEvent(hand);
        });
    }
}
