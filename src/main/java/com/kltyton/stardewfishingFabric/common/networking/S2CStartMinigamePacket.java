package com.kltyton.stardewfishingFabric.common.networking;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.common.FishBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.Optional;

public record S2CStartMinigamePacket(FishBehavior behavior, Optional<ItemStack> previewItem) implements CustomPayload {
    public static final CustomPayload.Id<S2CStartMinigamePacket> ID = new CustomPayload.Id<>(Identifier.of(StardewfishingFabric.MODID, "start_minigame"));
    public static final PacketCodec<RegistryByteBuf, S2CStartMinigamePacket> PACKET_CODEC =
            PacketCodec.tuple(
                    FishBehavior.PACKET_CODEC, S2CStartMinigamePacket::behavior,
                    PacketCodecs.optional(ItemStack.PACKET_CODEC), S2CStartMinigamePacket::previewItem,
                    S2CStartMinigamePacket::new
            );
    public static final CustomPayload.Type<RegistryByteBuf, S2CStartMinigamePacket> CODEC = new CustomPayload.Type<>(ID, PACKET_CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
