package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.client.minigame.FishingScreen;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

public class ClientFishingScreenOpener {
    public static void openFishingScreen(S2CStartMinigamePacket packet) {
        Minecraft.getInstance().setScreen(new FishingScreen(packet));
    }

    public static float getPartialTick() {
        DeltaTracker tracker = Minecraft.getInstance().getDeltaTracker();
        return tracker instanceof DeltaTracker.Timer timer ? timer.deltaTickResidual : tracker.getGameTimeDeltaPartialTick(false);
    }
}
