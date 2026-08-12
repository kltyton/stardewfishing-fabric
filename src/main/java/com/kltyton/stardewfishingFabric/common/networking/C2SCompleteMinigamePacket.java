package com.kltyton.stardewfishingFabric.common.networking;

import net.minecraft.network.FriendlyByteBuf;

public record C2SCompleteMinigamePacket(boolean success, double accuracy, boolean gotChest) {
    public C2SCompleteMinigamePacket(boolean success, double accuracy) {
        this(success, accuracy, false);
    }

    public C2SCompleteMinigamePacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readDouble(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeDouble(accuracy);
        buf.writeBoolean(gotChest);
    }
}
