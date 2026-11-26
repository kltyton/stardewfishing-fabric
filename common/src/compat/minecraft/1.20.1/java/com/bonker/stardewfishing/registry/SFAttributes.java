package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class SFAttributes {
    public static final RangedAttribute LINE_STRENGTH = register("line_strength", 1, 0, 2);
    public static final RangedAttribute BAR_SIZE = register("bar_size", 36, 16, 142);
    public static final RangedAttribute TREASURE_CHANCE_BONUS = register("treasure_chance_bonus", 0, 0, 1);
    public static final RangedAttribute GOLDEN_CHEST_BONUS = register("golden_chest_bonus", 0, 0, 1);
    public static final RangedAttribute EXP_MULTIPLIER = register("exp_multiplier", 1, 0, 1000);

    private SFAttributes() {
    }

    public static void initialize() {
    }

    private static RangedAttribute register(String name, double defaultValue, double minimum, double maximum) {
        return Registry.register(BuiltInRegistries.ATTRIBUTE, StardewFishing.resource(name),
                new RangedAttribute("attribute.name." + StardewFishing.MODID + "." + name,
                        defaultValue, minimum, maximum));
    }
}
