package com.bonker.stardewfishing.compat;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class JobsAddonCompat {
    private JobsAddonCompat() {
    }

    public static void awardFishingExperience(Player player, ItemStack reward) {
        JobHelper.addFisherXp(player, reward);
    }
}
