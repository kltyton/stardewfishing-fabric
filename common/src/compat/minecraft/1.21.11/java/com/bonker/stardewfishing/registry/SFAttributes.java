package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.Function;

/** Player attributes registered for the minigame; attached to players by {@code PlayerEntityMixin}. */
public final class SFAttributes {
    public static final ClampedEntityAttribute LINE_STRENGTH = register("line_strength",
            id -> new ClampedEntityAttribute(id, 1, 0, 2));
    public static final ClampedEntityAttribute BAR_SIZE = register("bar_size",
            id -> new ClampedEntityAttribute(id, 36, 16, 142));
    public static final ClampedEntityAttribute TREASURE_CHANCE_BONUS = register("treasure_chance_bonus",
            id -> new ClampedEntityAttribute(id, 0, 0, 1));
    public static final ClampedEntityAttribute GOLDEN_CHEST_BONUS = register("golden_chest_bonus",
            id -> new ClampedEntityAttribute(id, 0, 0, 1));
    public static final ClampedEntityAttribute EXP_MULTIPLIER = register("exp_multiplier",
            id -> new ClampedEntityAttribute(id, 1, 0, 1000));

    private SFAttributes() {
    }

    private static <T extends EntityAttribute> T register(String id, Function<String, T> constructor) {
        return Registry.register(Registries.ATTRIBUTE, StardewFishing.identifier(id),
                constructor.apply("attribute.name." + StardewFishing.MODID + "." + id));
    }
}
