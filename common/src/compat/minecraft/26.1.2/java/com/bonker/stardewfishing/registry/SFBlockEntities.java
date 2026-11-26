package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.block.FishDisplayBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public final class SFBlockEntities {
    public static final BlockEntityType<FishDisplayBlockEntity> FISH_DISPLAY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            StardewFishing.identifier("fish_display"),
            FabricBlockEntityTypeBuilder.create(FishDisplayBlockEntity::new, SFBlocks.FISH_DISPLAY).build()
    );

    private SFBlockEntities() {
    }
}
