package com.bonker.stardewfishing.client.network;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.ClientFishingScreenOpener;
import com.bonker.stardewfishing.network.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import com.bonker.stardewfishing.network.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.network.SFNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class ClientNetworking {
    private static boolean registered;

    private ClientNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(SFNetworking.START_MINIGAME,
                (client, handler, buffer, responseSender) -> {
                    S2CStartMinigamePacket packet = new S2CStartMinigamePacket(buffer);
                    client.execute(() -> ClientFishingScreenOpener.openFishingScreen(packet));
                });
        ClientPlayNetworking.registerGlobalReceiver(SFNetworking.SYNC_MODIFIERS,
                (client, handler, buffer, responseSender) -> {
                    S2CSyncModifiersPacket packet = new S2CSyncModifiersPacket(buffer);
                    client.execute(() -> StardewFishing.clientModifiersSupplier = packet::data);
                });
    }

    public static void sendComplete(C2SCompleteMinigamePacket packet) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        packet.encode(buffer);
        ClientPlayNetworking.send(SFNetworking.COMPLETE_MINIGAME, buffer);
    }
}
