package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.List;
import java.util.function.Function;

public class SFAttributes {
    public static final Holder.Reference<Attribute> LINE_STRENGTH = register("line_strength",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 2));

    public static final Holder.Reference<Attribute> BAR_SIZE = register("bar_size",
            descriptionId -> new RangedAttribute(descriptionId, 36, 16, 142));

    public static final Holder.Reference<Attribute> TREASURE_CHANCE_BONUS = register("treasure_chance_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final Holder.Reference<Attribute> GOLDEN_CHEST_BONUS = register("golden_chest_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final Holder.Reference<Attribute> EXP_MULTIPLIER = register("exp_multiplier",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 1000));

    public static final List<Holder.Reference<Attribute>> ALL = List.of(
            LINE_STRENGTH, BAR_SIZE, TREASURE_CHANCE_BONUS, GOLDEN_CHEST_BONUS, EXP_MULTIPLIER);

    /** Triggers class initialization so all attributes register. */
    public static void register() {
    }

    public static double minValue(Holder<Attribute> attribute) {
        return ((RangedAttribute) attribute.value()).getMinValue();
    }

    public static double maxValue(Holder<Attribute> attribute) {
        return ((RangedAttribute) attribute.value()).getMaxValue();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Attribute> Holder.Reference<Attribute> register(String id, Function<String, T> constructor) {
        return (Holder.Reference<Attribute>) (Holder.Reference<?>) Registry.registerForHolder(
                BuiltInRegistries.ATTRIBUTE, StardewFishing.identifier(id),
                constructor.apply("attribute.name." + StardewFishing.MODID + "." + id));
    }
}
