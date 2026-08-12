package com.bonker.stardewfishing.api.event;

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
        setAccuracy(accuracy);
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
        this.accuracy = Double.isFinite(accuracy) ? Math.max(0.0, Math.min(1.0, accuracy)) : 0.0;
    }

    public boolean gotChest() {
        return gotChest;
    }

    public void setGotChest(boolean gotChest) {
        this.gotChest = gotChest;
    }
}
