package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.block.FishDisplayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SFBlocks {
    public static final FishDisplayBlock FISH_DISPLAY = registerFishDisplay();

    private SFBlocks() {
    }

    private static FishDisplayBlock registerFishDisplay() {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, StardewFishing.identifier("fish_display"));
        FishDisplayBlock block = new FishDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(key));
        SFItems.registerBlockItem("fish_display", block);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }
}
