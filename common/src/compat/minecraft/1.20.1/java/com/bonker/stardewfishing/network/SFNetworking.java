package com.bonker.stardewfishing.network;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.StardewMinigameStartedEvent;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookLogic;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.server.fishing.FishingHookState;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class SFNetworking {
    public static final ResourceLocation START_MINIGAME = StardewFishing.resource("start_minigame");
    public static final ResourceLocation COMPLETE_MINIGAME = StardewFishing.resource("complete_minigame");
    public static final ResourceLocation SYNC_MODIFIERS = StardewFishing.resource("sync_modifiers");
    private static boolean commonRegistered;

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

    private static void handleComplete(ServerPlayer player, C2SCompleteMinigamePacket packet) {
        FishingHook hook = player.fishing;
        if (hook == null) {
            StardewFishing.LOGGER.warn("{} tried to complete a minigame that does not exist", player.getScoreboardName());
            return;
        }
        FishingHookState state = FishingHookState.get(hook);
        StardewMinigameStartedEvent event = state.stardewFishing$getEvent();
        if (hook.isRemoved() || hook.getPlayerOwner() != player || event == null
                || state.stardewFishing$getRewards().isEmpty()
                || !state.stardewFishing$getRewards().isLocked()) {
            StardewFishing.LOGGER.warn("{} tried to complete a minigame that does not exist", player.getScoreboardName());
            return;
        }

        boolean finiteAccuracy = Double.isFinite(packet.accuracy());
        double accuracy = finiteAccuracy ? Math.max(0.0, Math.min(1.0, packet.accuracy())) : 0.0;
        boolean success = packet.success() && finiteAccuracy;
        boolean gotChest = packet.gotChest() && state.stardewFishing$hasTreasureChest();

        InteractionHand hand = FishingItemSupport.getRodHand(player);
        if (hand == null) {
            FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
            StardewFishing.LOGGER.warn("{} tried to complete a minigame without a fishing rod", player.getScoreboardName());
            return;
        }

        ItemStack rod = player.getItemInHand(hand);
        if (!ItemStack.matches(rod, event.getFishingRod())) {
            FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
            StardewFishing.LOGGER.warn("{} tried to complete a minigame with a different fishing rod",
                    player.getScoreboardName());
            return;
        }

        FishingItemSupport.damageAttachedBobber(rod, player);
        FishingHookLogic.endMinigame(player, success, accuracy, gotChest, event.getQualityBoost(), rod);

        ItemStack bobber = FishingItemSupport.getBobber(rod).copy();
        rod.hurtAndBreak(1, player, broken -> {
            if (!bobber.isEmpty()) player.spawnAtLocation(bobber);
            player.broadcastBreakEvent(hand);
        });
    }
}
