package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.block.FishDisplayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class SFBlocks {
    public static final FishDisplayBlock FISH_DISPLAY = register("fish_display",
            FishDisplayBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN));

    /** Triggers class initialization so all blocks register. */
    public static void register() {
    }

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties props) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, StardewFishing.identifier(name));
        T block = Registry.register(BuiltInRegistries.BLOCK, blockKey, function.apply(props.setId(blockKey)));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, StardewFishing.identifier(name));
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        return block;
    }
}
