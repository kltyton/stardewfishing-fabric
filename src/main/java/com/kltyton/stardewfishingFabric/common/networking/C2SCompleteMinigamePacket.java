package com.kltyton.stardewfishingFabric.common.networking;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record C2SCompleteMinigamePacket(boolean success, double accuracy) implements CustomPayload {
    public static final CustomPayload.Id<C2SCompleteMinigamePacket> ID = new CustomPayload.Id<>(Identifier.of(StardewfishingFabric.MODID, "complete_minigame"));
    public static final PacketCodec<RegistryByteBuf, C2SCompleteMinigamePacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, C2SCompleteMinigamePacket::success,
            PacketCodecs.DOUBLE, C2SCompleteMinigamePacket::accuracy,
            C2SCompleteMinigamePacket::new
    );
    public static final CustomPayload.Type<RegistryByteBuf, C2SCompleteMinigamePacket> CODEC = new CustomPayload.Type<>(ID, PACKET_CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
