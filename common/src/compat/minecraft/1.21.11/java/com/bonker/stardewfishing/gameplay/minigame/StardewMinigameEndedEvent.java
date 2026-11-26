package com.bonker.stardewfishing.gameplay.minigame;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** Mutable success/accuracy/chest result; listeners may adjust the outcome. */
public class StardewMinigameEndedEvent extends StardewMinigameEvent {
    private boolean success;
    private double accuracy;
    private boolean gotChest;

    public StardewMinigameEndedEvent(ServerPlayerEntity player, FishingBobberEntity hook, ItemStack fishingRod,
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
