package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.blocks.FishDisplayBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/** Block entity type registrations. */
public final class SFBlockEntities {
    public static final BlockEntityType<FishDisplayBlockEntity> FISH_DISPLAY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, StardewFishing.identifier("fish_display"),
                    FabricBlockEntityTypeBuilder.create(FishDisplayBlockEntity::new, SFBlocks.FISH_DISPLAY).build());

    private SFBlockEntities() {
    }
}
