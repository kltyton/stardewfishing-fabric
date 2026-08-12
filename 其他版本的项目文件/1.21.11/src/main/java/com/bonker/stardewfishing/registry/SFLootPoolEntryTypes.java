package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.loot.OptionalLootItem;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Custom loot entry type used by the hand-written treasure chest loot tables. */
public final class SFLootPoolEntryTypes {
    public static final LootPoolEntryType MOD_LOADED =
            Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, StardewFishing.identifier("optional"),
                    new LootPoolEntryType(OptionalLootItem.CODEC));

    private SFLootPoolEntryTypes() {
    }
}
