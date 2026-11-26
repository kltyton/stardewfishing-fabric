package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.Supplier;

public final class SFComponentTypes {
    public static final DataComponentType<CompoundTag> BOBBER = register("bobber",
            DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());
    public static final DataComponentType<LegendaryCatch> LEGENDARY_CATCH = register("legendary_catch",
            DataComponentType.<LegendaryCatch>builder().persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC).build());
    public static final DataComponentType<String> POKEMON_SPECIES = register("pokemon_species",
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    private static <T> DataComponentType<T> register(String id, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, StardewFishing.resource(id), type);
    }

    public static void initialize() {
    }

    private SFComponentTypes() {
    }
}
