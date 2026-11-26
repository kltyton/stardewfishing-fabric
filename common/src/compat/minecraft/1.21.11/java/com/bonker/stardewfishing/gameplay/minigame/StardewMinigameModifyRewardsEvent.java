package com.bonker.stardewfishing.gameplay.minigame;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/** Exposes the mutable reward list right before rewards are given to the player. */
public class StardewMinigameModifyRewardsEvent extends StardewMinigameEvent {
    private final List<ItemStack> rewards;

    public StardewMinigameModifyRewardsEvent(ServerPlayerEntity player, FishingBobberEntity hook,
                                             ItemStack fishingRod, List<ItemStack> rewards) {
        super(player, hook, fishingRod);
        this.rewards = rewards;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }
}
