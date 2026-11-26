package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.loot.OptionalLootItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

public final class SFLootPoolEntryTypes {
    public static final LootPoolEntryType OPTIONAL = Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
            StardewFishing.resource("optional"), new LootPoolEntryType(new OptionalLootItem.Serializer()));

    private SFLootPoolEntryTypes() {
    }

    public static void initialize() {
    }
}
