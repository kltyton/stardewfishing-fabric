package com.bonker.stardewfishing.network;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.FishingHookLogic;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.gameplay.MinigameModifiers;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookAttachment;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class SFNetworking {
    private static final int MAX_MODIFIERS = 16_384;

    private SFNetworking() {
    }

    public static void registerTypes() {
        PayloadTypeRegistry.playC2S().register(C2SCompleteMinigamePayload.ID, C2SCompleteMinigamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CStartMinigamePayload.ID, S2CStartMinigamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncModifiersPayload.ID, S2CSyncModifiersPayload.CODEC);
    }

    public static void registerCommonReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePayload.ID,
                (payload, context) -> payload.handle(context.player()));
    }

    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void syncModifiersToPlayer(ServerPlayerEntity player) {
        sendToPlayer(player, new S2CSyncModifiersPayload(
                StardewFishing.ModifiersAccess.getData() == null
                        ? Map.of()
                        : StardewFishing.ModifiersAccess.getData()));
    }

    public record C2SCompleteMinigamePayload(boolean success, double accuracy, boolean gotChest)
            implements CustomPayload {
        public static final Id<C2SCompleteMinigamePayload> ID =
                new Id<>(StardewFishing.identifier("c2s_complete_minigame"));
        public static final PacketCodec<PacketByteBuf, C2SCompleteMinigamePayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    buf.writeBoolean(payload.success);
                    buf.writeDouble(payload.accuracy);
                    buf.writeBoolean(payload.gotChest);
                },
                buf -> new C2SCompleteMinigamePayload(buf.readBoolean(), buf.readDouble(), buf.readBoolean()));

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        private void handle(ServerPlayerEntity player) {
            if (player.fishHook == null || FishingHookAttachment.get(player.fishHook).getRewards().isEmpty()) {
                StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame that doesn't exist",
                        player.getNameForScoreboard());
                return;
            }

            Hand hand = ItemUtils.getRodHand(player);
            if (hand == null) {
                FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
                StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame without a fishing rod",
                        player.getNameForScoreboard());
                return;
            }

            ItemStack fishingRod = player.getStackInHand(hand);
            ItemUtils.damageAttachedBobber(fishingRod, player);
            int qualityBoost = FishingHookAttachment.get(player.fishHook).getEvent().getQualityBoost();
            FishingHookLogic.endMinigame(player, success, accuracy, gotChest, qualityBoost, fishingRod);

            ItemStack bobber = ItemUtils.getBobber(fishingRod, player.getRegistryManager()).copy();
            fishingRod.damage(1, player.getEntityWorld(), player, item -> {
                if (!bobber.isEmpty()) {
                    player.dropItem(bobber, false);
                }
                player.sendEquipmentBreakStatus(item, hand.getEquipmentSlot());
            });
        }
    }

    public record S2CStartMinigamePayload(int idleTime, float topSpeed, float upAcceleration,
                                         float downAcceleration, int avgDistance, int moveVariation,
                                         ItemStack fish, boolean treasureChest, boolean goldenChest,
                                         float lineStrength, int barSize) implements CustomPayload {
        public static final Id<S2CStartMinigamePayload> ID =
                new Id<>(StardewFishing.identifier("s2c_start_minigame"));
        public static final PacketCodec<RegistryByteBuf, S2CStartMinigamePayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    buf.writeShort(payload.idleTime);
                    buf.writeFloat(payload.topSpeed);
                    buf.writeFloat(payload.upAcceleration);
                    buf.writeFloat(payload.downAcceleration);
                    buf.writeShort(payload.avgDistance);
                    buf.writeShort(payload.moveVariation);
                    ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, payload.fish);
                    buf.writeBoolean(payload.treasureChest);
                    buf.writeBoolean(payload.goldenChest);
                    buf.writeFloat(payload.lineStrength);
                    buf.writeShort(payload.barSize);
                },
                buf -> new S2CStartMinigamePayload(
                        buf.readShort(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readShort(), buf.readShort(), ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
                        buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readShort()));

        public S2CStartMinigamePayload(FishingHookAttachment data) {
            this(data.getEvent().getIdleTime(), data.getEvent().getTopSpeed(),
                    data.getEvent().getUpAcceleration(), data.getEvent().getDownAcceleration(),
                    data.getEvent().getAvgDistance(), data.getEvent().getMoveVariation(),
                    data.getEvent().getFish(), data.hasTreasureChest(), data.hasGoldenChest(),
                    (float) data.getEvent().getLineStrength(), data.getEvent().getBarSize());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record S2CSyncModifiersPayload(Map<Item, MinigameModifiers> data) implements CustomPayload {
        public static final Id<S2CSyncModifiersPayload> ID =
                new Id<>(StardewFishing.identifier("s2c_sync_modifiers"));
        public static final PacketCodec<RegistryByteBuf, S2CSyncModifiersPayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    buf.writeVarInt(payload.data.size());
                    for (Map.Entry<Item, MinigameModifiers> entry : payload.data.entrySet()) {
                        buf.writeIdentifier(Registries.ITEM.getId(entry.getKey()));
                        entry.getValue().write(buf);
                    }
                },
                buf -> {
                    int size = buf.readVarInt();
                    if (size < 0 || size > MAX_MODIFIERS) {
                        throw new DecoderException("Invalid Stardew Fishing modifier count: " + size);
                    }
                    Map<Item, MinigameModifiers> data = new HashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        Identifier id = buf.readIdentifier();
                        data.put(Registries.ITEM.get(id), MinigameModifiers.read(buf));
                    }
                    return new S2CSyncModifiersPayload(data);
                });

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
