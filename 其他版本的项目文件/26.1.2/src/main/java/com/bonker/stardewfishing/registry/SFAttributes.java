package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class SFAttributes {
    public static final Holder.Reference<Attribute> LINE_STRENGTH = register("line_strength", 1, 0, 2);
    public static final Holder.Reference<Attribute> BAR_SIZE = register("bar_size", 36, 16, 142);
    public static final Holder.Reference<Attribute> TREASURE_CHANCE_BONUS = register("treasure_chance_bonus", 0, 0, 1);
    public static final Holder.Reference<Attribute> GOLDEN_CHEST_BONUS = register("golden_chest_bonus", 0, 0, 1);
    public static final Holder.Reference<Attribute> EXP_MULTIPLIER = register("exp_multiplier", 1, 0, 1000);

    private SFAttributes() {
    }

    private static Holder.Reference<Attribute> register(String id, double defaultValue, double minimum, double maximum) {
        return Registry.registerForHolder(
                BuiltInRegistries.ATTRIBUTE,
                StardewFishing.identifier(id),
                new RangedAttribute("attribute.name." + StardewFishing.MODID + "." + id, defaultValue, minimum, maximum)
        );
    }
}
