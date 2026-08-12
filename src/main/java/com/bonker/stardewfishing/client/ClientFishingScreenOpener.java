package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.client.minigame.FishingScreen;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import net.minecraft.client.Minecraft;

public final class ClientFishingScreenOpener {
    private ClientFishingScreenOpener() {
    }

    public static void openFishingScreen(S2CStartMinigamePacket packet) {
        Minecraft.getInstance().setScreen(new FishingScreen(packet));
    }
}
