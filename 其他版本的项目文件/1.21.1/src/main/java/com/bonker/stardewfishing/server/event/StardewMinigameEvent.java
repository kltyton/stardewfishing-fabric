package com.bonker.stardewfishing.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public abstract class StardewMinigameEvent {
    private final ServerPlayer player;
    private final FishingHook hook;
    private final ItemStack fishingRod;

    public StardewMinigameEvent(ServerPlayer player, FishingHook hook, ItemStack fishingRod) {
        this.player = player;
        this.hook = hook;
        this.fishingRod = fishingRod;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public FishingHook getHook() {
        return hook;
    }

    public ItemStack getFishingRod() {
        return fishingRod;
    }
}
