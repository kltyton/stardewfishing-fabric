package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.client.minigame.FishingScreen;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import net.minecraft.client.Minecraft;

public class ClientFishingScreenOpener {
    public static void openFishingScreen(S2CStartMinigamePacket packet) {
        Minecraft.getInstance().setScreen(new FishingScreen(packet));
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
