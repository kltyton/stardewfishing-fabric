package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.loot.OptionalLootItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public final class SFLootPoolEntryTypes {
    public static final MapCodec<OptionalLootItem> MOD_LOADED = Registry.register(
            BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
            StardewFishing.identifier("optional"),
            OptionalLootItem.CODEC
    );

    private SFLootPoolEntryTypes() {
    }
}
