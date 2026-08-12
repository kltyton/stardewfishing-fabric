package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Supplier;

public final class SFAttributes {
    public static final Supplier<RangedAttribute> LINE_STRENGTH = register("line_strength", 1, 0, 2);
    public static final Supplier<RangedAttribute> BAR_SIZE = register("bar_size", 36, 16, 142);
    public static final Supplier<RangedAttribute> TREASURE_CHANCE_BONUS = register("treasure_chance_bonus", 0, 0, 1);
    public static final Supplier<RangedAttribute> GOLDEN_CHEST_BONUS = register("golden_chest_bonus", 0, 0, 1);
    public static final Supplier<RangedAttribute> EXP_MULTIPLIER = register("exp_multiplier", 1, 0, 1000);
    public static final List<Supplier<RangedAttribute>> ALL = List.of(
            LINE_STRENGTH, BAR_SIZE, TREASURE_CHANCE_BONUS, GOLDEN_CHEST_BONUS, EXP_MULTIPLIER);

    private static Supplier<RangedAttribute> register(String id, double fallback, double min, double max) {
        RangedAttribute attribute = Registry.register(BuiltInRegistries.ATTRIBUTE, StardewFishing.resource(id),
                new RangedAttribute("attribute.name." + StardewFishing.MODID + "." + id, fallback, min, max));
        return () -> attribute;
    }

    public static double value(Player player, Supplier<RangedAttribute> attribute) {
        return player.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get()));
    }

    public static void initialize() {
    }

    private SFAttributes() {
    }
}
