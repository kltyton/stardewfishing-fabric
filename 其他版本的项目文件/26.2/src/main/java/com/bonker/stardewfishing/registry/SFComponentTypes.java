package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.Bobber;
import com.bonker.stardewfishing.common.item.LegendaryCatch;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SFComponentTypes {
    public static final DataComponentType<Bobber> BOBBER = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, StardewFishing.identifier("bobber"),
            DataComponentType.<Bobber>builder().persistent(Bobber.CODEC).networkSynchronized(Bobber.STREAM_CODEC).build());

    public static final DataComponentType<LegendaryCatch> LEGENDARY_CATCH = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, StardewFishing.identifier("legendary_catch"),
            DataComponentType.<LegendaryCatch>builder().persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC).build());

    public static final DataComponentType<String> POKEMON_SPECIES = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, StardewFishing.identifier("pokemon_species"),
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    /** Triggers class initialization so all data components register. */
    public static void register() {
    }
}
