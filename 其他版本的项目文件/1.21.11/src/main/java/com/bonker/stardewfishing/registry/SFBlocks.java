package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.blocks.FishDisplayBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

/** Block and block item registrations. */
public final class SFBlocks {
    public static final FishDisplayBlock FISH_DISPLAY = registerFishDisplay();

    private SFBlocks() {
    }

    private static FishDisplayBlock registerFishDisplay() {
        var id = StardewFishing.identifier("fish_display");
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        FishDisplayBlock block = new FishDisplayBlock(
                Block.Settings.copy(Blocks.OAK_SIGN).registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        BlockItem blockItem = new BlockItem(block,
                new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
        blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem);
        Registry.register(Registries.ITEM, itemKey, blockItem);
        return block;
    }
}
