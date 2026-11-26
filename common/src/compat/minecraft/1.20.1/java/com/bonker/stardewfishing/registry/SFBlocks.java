package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.block.FishDisplayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SFBlocks {
    public static final Block FISH_DISPLAY = Registry.register(BuiltInRegistries.BLOCK,
            StardewFishing.resource("fish_display"),
            new FishDisplayBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN)));

    private SFBlocks() {
    }

    public static void initialize() {
    }
}
