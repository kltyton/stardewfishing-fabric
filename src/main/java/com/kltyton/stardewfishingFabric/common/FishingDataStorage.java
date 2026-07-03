package com.kltyton.stardewfishingFabric.common;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishingDataStorage {
    private static final Map<ServerPlayerEntity, FishingBobberEntity> playerHookMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, List<ItemStack>> playerItemMap = new HashMap<>();

    public static void storeData(ServerPlayerEntity player, FishingBobberEntity hook, List<ItemStack> items) {
        playerHookMap.put(player, hook);
        playerItemMap.put(player, items == null ? List.of() : items.stream().map(ItemStack::copy).toList());
    }

    public static FishingBobberEntity getHookForPlayer(ServerPlayerEntity player) {
        return playerHookMap.get(player);
    }

    public static List<ItemStack> getItemsForPlayer(ServerPlayerEntity player) {
        return playerItemMap.getOrDefault(player, List.of());
    }

    public static void clearDataForPlayer(ServerPlayerEntity player) {
        playerHookMap.remove(player);
        playerItemMap.remove(player);
    }
}
