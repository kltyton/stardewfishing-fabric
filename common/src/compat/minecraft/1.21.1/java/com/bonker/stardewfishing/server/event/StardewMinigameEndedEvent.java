package com.bonker.stardewfishing.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public class StardewMinigameEndedEvent extends StardewMinigameEvent {
    private boolean success;
    private double accuracy;
    private boolean gotChest;

    public StardewMinigameEndedEvent(ServerPlayer player, FishingHook hook, ItemStack fishingRod,
                                     boolean success, double accuracy, boolean gotChest) {
        super(player, hook, fishingRod);
        this.success = success;
        this.accuracy = accuracy;
        this.gotChest = gotChest;
    }

    public boolean wasSuccessful() {
        return success;
    }

    public void setSuccess(boolean wasSuccessful) {
        this.success = wasSuccessful;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public boolean gotChest() {
        return gotChest;
    }

    public void setGotChest(boolean gotChest) {
        this.gotChest = gotChest;
    }
}
