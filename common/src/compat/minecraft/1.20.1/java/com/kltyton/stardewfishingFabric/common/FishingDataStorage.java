package com.kltyton.stardewfishingFabric.common;

import com.bonker.stardewfishing.compat.tide.TideCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

/**
 * Binary compatibility facade for Tide 1.20.1. All behavior lives in the new port.
 */
public final class FishingDataStorage {
    private FishingDataStorage() {
    }

    public static void storeData(ServerPlayer player, FishingHook hook, ItemStack item) {
        TideCompat.storeReward(player, hook, item);
    }
}
