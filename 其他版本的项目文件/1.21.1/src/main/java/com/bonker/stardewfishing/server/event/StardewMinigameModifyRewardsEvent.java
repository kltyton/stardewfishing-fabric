package com.bonker.stardewfishing.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StardewMinigameModifyRewardsEvent extends StardewMinigameEvent {
    private final List<ItemStack> rewards;

    public StardewMinigameModifyRewardsEvent(ServerPlayer player, FishingHook hook, ItemStack fishingRod, List<ItemStack> rewards) {
        super(player, hook, fishingRod);
        this.rewards = rewards;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }
}
