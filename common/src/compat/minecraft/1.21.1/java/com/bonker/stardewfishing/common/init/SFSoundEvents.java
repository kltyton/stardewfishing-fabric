package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class SFSoundEvents {
    public static final Supplier<SoundEvent> CAST = register("cast");
    public static final Supplier<SoundEvent> COMPLETE = register("complete");
    public static final Supplier<SoundEvent> DWOP = register("dwop");
    public static final Supplier<SoundEvent> DWOP_REVERSE = register("dwop_reverse");
    public static final Supplier<SoundEvent> EQUIP = register("equip");
    public static final Supplier<SoundEvent> UNEQUIP = register("unequip");
    public static final Supplier<SoundEvent> FISH_ESCAPE = register("fish_escape");
    public static final Supplier<SoundEvent> FISH_BITE = register("fish_bite");
    public static final Supplier<SoundEvent> FISH_HIT = register("fish_hit");
    public static final Supplier<SoundEvent> PULL_ITEM = register("pull_item");
    public static final Supplier<SoundEvent> REEL_CREAK = register("reel_creak");
    public static final Supplier<SoundEvent> REEL_FAST = register("reel_fast");
    public static final Supplier<SoundEvent> REEL_SLOW = register("reel_slow");
    public static final Supplier<SoundEvent> OPEN_CHEST = register("open_chest");
    public static final Supplier<SoundEvent> OPEN_CHEST_GOLDEN = register("open_chest_golden");
    public static final Supplier<SoundEvent> CHEST_GET = register("chest_get");

    private static Supplier<SoundEvent> register(String name) {
        SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, StardewFishing.resource(name),
                SoundEvent.createVariableRangeEvent(StardewFishing.resource(name)));
        return () -> sound;
    }

    public static void initialize() {
    }

    private SFSoundEvents() {
    }
}
