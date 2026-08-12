package com.bonker.stardewfishing.compat;

import koala.fishingreal.FishingReal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public final class FishingRealCompat {
    private FishingRealCompat() {
    }

    public static Entity convert(ItemEntity item, Player player) {
        return FishingReal.convertItemEntity(item, player);
    }
}
