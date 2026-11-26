package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public final class SFBlocks {
    public static final Supplier<FishDisplayBlock> FISH_DISPLAY = register("fish_display",
            FishDisplayBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN));

    private static <T extends Block> Supplier<T> register(String name,
                                                           Function<BlockBehaviour.Properties, T> factory,
                                                           BlockBehaviour.Properties properties) {
        T block = Registry.register(BuiltInRegistries.BLOCK, StardewFishing.resource(name), factory.apply(properties));
        Registry.register(BuiltInRegistries.ITEM, StardewFishing.resource(name), new BlockItem(block, new Item.Properties()));
        return () -> block;
    }

    public static void initialize() {
    }

    private SFBlocks() {
    }
}
