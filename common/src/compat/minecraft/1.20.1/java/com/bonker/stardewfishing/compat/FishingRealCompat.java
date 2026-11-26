package com.bonker.stardewfishing.compat;

import koala.fishingreal.FishingReal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FishingRealCompat {
    private static final Method FISH_UP_ENTITY = findFishUpEntity();

    private FishingRealCompat() {
    }

    public static int fishUpEntities(ItemStack reward, FishingHook hook, ServerPlayer player) {
        if (FISH_UP_ENTITY == null) return 0;
        for (int caught = 0; caught < reward.getCount(); caught++) {
            Entity entity = FishingReal.convertItemStack(reward, player, hook.position());
            if (entity == null) return caught;
            fishUpEntity(entity, hook, reward.copyWithCount(1), player);
        }
        return reward.getCount();
    }

    public static Entity convertLegacy(ItemEntity item, Player player) {
        return FISH_UP_ENTITY == null ? FishingReal.convertItemEntity(item, player) : item;
    }

    // The Fabric backport replaced the legacy conversion method. Resolve its public API
    // once because both releases use the same FishingReal class with different methods.
    private static Method findFishUpEntity() {
        try {
            return FishingReal.class.getMethod("fishUpEntity", Entity.class, FishingHook.class,
                    ItemStack.class, Player.class);
        } catch (NoSuchMethodException backportMethodAbsent) {
            try {
                FishingReal.class.getMethod("convertItemEntity", Entity.class, Player.class);
                return null;
            } catch (NoSuchMethodException legacyMethodAbsent) {
                legacyMethodAbsent.addSuppressed(backportMethodAbsent);
                throw new IllegalStateException("Unsupported Fishing Real fishing reward API", legacyMethodAbsent);
            }
        }
    }

    private static void fishUpEntity(Entity entity, FishingHook hook, ItemStack reward, ServerPlayer player) {
        try {
            FISH_UP_ENTITY.invoke(null, entity, hook, reward, player);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) throw runtimeException;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Fishing Real failed to deliver a fishing reward", cause);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot access Fishing Real's public fishing reward API", exception);
        }
    }
}
