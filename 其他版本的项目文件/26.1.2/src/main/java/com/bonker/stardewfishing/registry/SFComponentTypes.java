package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.Bobber;
import com.bonker.stardewfishing.common.item.LegendaryCatch;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SFComponentTypes {
    public static final DataComponentType<Bobber> BOBBER = register("bobber",
            DataComponentType.<Bobber>builder().persistent(Bobber.CODEC).networkSynchronized(Bobber.STREAM_CODEC).build());
    public static final DataComponentType<LegendaryCatch> LEGENDARY_CATCH = register("legendary_catch",
            DataComponentType.<LegendaryCatch>builder().persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC).build());
    private SFComponentTypes() {
    }

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, StardewFishing.identifier(name), type);
    }
}
