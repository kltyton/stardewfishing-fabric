package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.data.OptionalLootItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

import java.util.function.Supplier;

public final class SFLootPoolEntryTypes {
    public static final Supplier<LootPoolEntryType> MOD_LOADED = register();

    private static Supplier<LootPoolEntryType> register() {
        LootPoolEntryType type = Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
                StardewFishing.resource("optional"), new LootPoolEntryType(OptionalLootItem.CODEC));
        return () -> type;
    }

    public static void initialize() {
    }

    private SFLootPoolEntryTypes() {
    }
}
