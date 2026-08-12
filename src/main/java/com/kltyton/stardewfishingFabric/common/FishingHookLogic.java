package com.kltyton.stardewfishingFabric.common;

import com.bonker.stardewfishing.compat.tide.TideCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Binary compatibility facade for Tide 1.20.1. All behavior lives in the new port.
 */
public final class FishingHookLogic {
    private FishingHookLogic() {
    }

    public static void startMinigame(ServerPlayer player, ItemStack item) {
        TideCompat.startMinigame(player, item);
    }
}
