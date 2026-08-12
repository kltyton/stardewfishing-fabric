package com.kltyton.stardewfishingFabric.common;

import com.kltyton.stardewfishingFabric.server.fishing.FishingHookState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FishingDataStorage {
    private FishingDataStorage() {
    }

    /** Legacy Tide compatibility entry point. */
    public static void storeData(ServerPlayer player, FishingHook hook, ItemStack item) {
        FishingHookState state = FishingHookState.get(hook);
        state.stardewFishing$getRewards().clear();
        state.stardewFishing$getRewards().add(item.copy());
        player.fishing = hook;
    }

    public static @Nullable FishingHook getHookForPlayer(ServerPlayer player) {
        return player.fishing;
    }

    public static ItemStack getItemsForPlayer(ServerPlayer player) {
        FishingHook hook = player.fishing;
        if (hook == null || FishingHookState.get(hook).stardewFishing$getRewards().isEmpty()) return ItemStack.EMPTY;
        return FishingHookState.get(hook).stardewFishing$getRewards().get(0);
    }

    public static void clearDataForPlayer(ServerPlayer player) {
        FishingHook hook = player.fishing;
        if (hook != null) FishingHookState.get(hook).stardewFishing$getRewards().clear();
    }
}
