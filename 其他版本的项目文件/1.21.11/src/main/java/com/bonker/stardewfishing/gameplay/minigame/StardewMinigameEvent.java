package com.bonker.stardewfishing.gameplay.minigame;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Base event for addon-visible minigame lifecycle events. On Fabric these are
 * plain observer events; see the registered listeners in
 * {@code StardewMinigameEvents}.
 */
public abstract class StardewMinigameEvent {
    private final ServerPlayerEntity player;
    private final FishingBobberEntity hook;
    private final ItemStack fishingRod;

    public StardewMinigameEvent(ServerPlayerEntity player, FishingBobberEntity hook, ItemStack fishingRod) {
        this.player = player;
        this.hook = hook;
        this.fishingRod = fishingRod;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public FishingBobberEntity getHook() {
        return hook;
    }

    public ItemStack getFishingRod() {
        return fishingRod;
    }
}
