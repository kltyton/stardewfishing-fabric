package com.kltyton.stardewfishingFabric;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Binary compatibility facade for Tide 1.20.1. This is not a mod entrypoint.
 */
public final class StardewfishingFabric {
    public static final TagKey<Item> STARTS_MINIGAME = StardewFishing.STARTS_MINIGAME;
    public static final SoundEvent FISH_BITE = SFSoundEvents.FISH_BITE;
    public static final SoundEvent PULL_ITEM = SFSoundEvents.PULL_ITEM;

    private StardewfishingFabric() {
    }
}
