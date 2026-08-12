package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class SFBlockEntities {
    public static final Supplier<BlockEntityType<FishDisplayBlockEntity>> FISH_DISPLAY = register();

    private static Supplier<BlockEntityType<FishDisplayBlockEntity>> register() {
        BlockEntityType<FishDisplayBlockEntity> type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                StardewFishing.resource("fish_display"),
                BlockEntityType.Builder.of(FishDisplayBlockEntity::new, SFBlocks.FISH_DISPLAY.get()).build(null));
        return () -> type;
    }

    public static void initialize() {
    }

    private SFBlockEntities() {
    }
}
