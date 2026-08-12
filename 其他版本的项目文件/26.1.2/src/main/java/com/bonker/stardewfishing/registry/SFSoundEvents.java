package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class SFSoundEvents {
    public static final SoundEvent CAST = register("cast");
    public static final SoundEvent COMPLETE = register("complete");
    public static final SoundEvent DWOP = register("dwop");
    public static final SoundEvent DWOP_REVERSE = register("dwop_reverse");
    public static final SoundEvent EQUIP = register("equip");
    public static final SoundEvent UNEQUIP = register("unequip");
    public static final SoundEvent FISH_ESCAPE = register("fish_escape");
    public static final SoundEvent FISH_BITE = register("fish_bite");
    public static final SoundEvent FISH_HIT = register("fish_hit");
    public static final SoundEvent PULL_ITEM = register("pull_item");
    public static final SoundEvent REEL_CREAK = register("reel_creak");
    public static final SoundEvent REEL_FAST = register("reel_fast");
    public static final SoundEvent REEL_SLOW = register("reel_slow");
    public static final SoundEvent OPEN_CHEST = register("open_chest");
    public static final SoundEvent OPEN_CHEST_GOLDEN = register("open_chest_golden");
    public static final SoundEvent CHEST_GET = register("chest_get");

    private SFSoundEvents() {
    }

    private static SoundEvent register(String name) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                StardewFishing.identifier(name),
                SoundEvent.createVariableRangeEvent(StardewFishing.identifier(name))
        );
    }
}
