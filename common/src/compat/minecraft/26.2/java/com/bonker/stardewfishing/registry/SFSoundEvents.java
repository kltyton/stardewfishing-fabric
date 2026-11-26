package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SFSoundEvents {
    public static final SoundEvent CAST = registerSound("cast");
    public static final SoundEvent COMPLETE = registerSound("complete");
    public static final SoundEvent DWOP = registerSound("dwop");
    public static final SoundEvent DWOP_REVERSE = registerSound("dwop_reverse");
    public static final SoundEvent EQUIP = registerSound("equip");
    public static final SoundEvent UNEQUIP = registerSound("unequip");
    public static final SoundEvent FISH_ESCAPE = registerSound("fish_escape");
    public static final SoundEvent FISH_BITE = registerSound("fish_bite");
    public static final SoundEvent FISH_HIT = registerSound("fish_hit");
    public static final SoundEvent PULL_ITEM = registerSound("pull_item");
    public static final SoundEvent REEL_CREAK = registerSound("reel_creak");
    public static final SoundEvent REEL_FAST = registerSound("reel_fast");
    public static final SoundEvent REEL_SLOW = registerSound("reel_slow");
    public static final SoundEvent OPEN_CHEST = registerSound("open_chest");
    public static final SoundEvent OPEN_CHEST_GOLDEN = registerSound("open_chest_golden");
    public static final SoundEvent CHEST_GET = registerSound("chest_get");

    /** Triggers class initialization so all sound events register. */
    public static void register() {
    }

    private static SoundEvent registerSound(String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, StardewFishing.identifier(name),
                SoundEvent.createVariableRangeEvent(StardewFishing.identifier(name)));
    }
}
