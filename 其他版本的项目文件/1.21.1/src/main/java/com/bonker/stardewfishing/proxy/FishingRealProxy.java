package com.bonker.stardewfishing.proxy;

import koala.fishingreal.FishingReal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class FishingRealProxy {
    public static boolean fishUpEntity(ItemStack reward, FishingHook hook, ServerPlayer player) {
        Entity converted = FishingReal.convertItemStack(reward, player, hook.position());
        if (converted == null) return false;
        for (int i = 0; i < reward.getCount(); i++) {
            FishingReal.fishUpEntity(converted, hook, reward, player);
        }
        return true;
    }

    private FishingRealProxy() {
    }
}
