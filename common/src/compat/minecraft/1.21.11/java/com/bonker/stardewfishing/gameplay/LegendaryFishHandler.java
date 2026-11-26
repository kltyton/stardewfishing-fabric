package com.bonker.stardewfishing.gameplay;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.registry.SFItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

/**
 * Fabric-safe port of the NeoForge legendary fish global loot modifier.
 * NeoForge GLMs do not exist on Fabric, so the identical chance/biome/replacement
 * logic runs at the earliest shared hook: right after vanilla fishing loot is
 * generated in {@code FishingBobberEntityMixin#use}.
 */
public final class LegendaryFishHandler {
    private static final List<Identifier> FISHING_LOOT_TABLES = List.of(
            Identifier.ofVanilla("gameplay/fishing"),
            Identifier.of("aquaculture", "gameplay/fishing/fish"),
            Identifier.of("aquaculture", "gameplay/fishing/lava/fish"),
            Identifier.of("aquaculture", "gameplay/fishing/nether/fish"),
            Identifier.of("tide", "gameplay/fishing/climates/"),
            Identifier.of("tide", "gameplay/fishing/special/"),
            Identifier.of("tide", "gameplay/fishing/special")
    );

    private static final List<Offset> BIOME_CHECK_OFFSETS = List.of(
            new Offset(16, 0), new Offset(-16, 0), new Offset(0, 16), new Offset(0, -16),
            new Offset(16, 16), new Offset(16, -16), new Offset(-16, -16), new Offset(-16, 16),
            new Offset(32, 0), new Offset(-32, 0), new Offset(0, 32), new Offset(0, -32),
            new Offset(32, 32), new Offset(32, -32), new Offset(-32, -32), new Offset(-32, 32));

    private LegendaryFishHandler() {
    }

    /**
     * @param generatedLoot the loot rolled by the vanilla fishing loot table
     * @param level         the fishing dimension
     * @param origin        the hook position
     * @param luck          the hook's luck bonus (same value the vanilla loot context uses)
     * @param lootTableId   the queried loot table id
     */
    public static List<ItemStack> maybeReplace(List<ItemStack> generatedLoot, ServerWorld level,
                                               BlockPos origin, float luck, Identifier lootTableId) {
        if (generatedLoot.isEmpty()) {
            // definitely no fish to replace
            return generatedLoot;
        }

        if (level.random.nextFloat() >= SFConfig.getLegendaryFishChance(luck)) {
            // didn't get lucky
            return generatedLoot;
        }

        if (lootTableId == null || FISHING_LOOT_TABLES.stream().noneMatch(id ->
                lootTableId.getNamespace().equals(id.getNamespace()) && lootTableId.getPath().startsWith(id.getPath()))) {
            // this isn't a fishing loot table
            return generatedLoot;
        }

        int index = 0;
        for (ItemStack stack : generatedLoot) {
            if (stack.isIn(ItemTags.FISHES)) {
                break;
            }
            index++;
        }

        if (index == generatedLoot.size()) {
            // no fish to replace
            return generatedLoot;
        }

        LegendaryCategory category = findLegendaryCategory(level, origin);
        if (category != null) {
            generatedLoot.set(index, new ItemStack(category.chooseFish(level)));
        }

        return generatedLoot;
    }

    private static LegendaryCategory findLegendaryCategory(World level, BlockPos pos) {
        LegendaryCategory direct = null;

        RegistryEntry<Biome> biome = level.getBiome(pos);
        if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
        else if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        else if (LegendaryCategory.WARM_OCEAN.matches(biome)) direct = LegendaryCategory.WARM_OCEAN;
        else if (LegendaryCategory.NORMAL_OCEAN.matches(biome)) direct = LegendaryCategory.NORMAL_OCEAN;
        else if (LegendaryCategory.RIVER.matches(biome)) direct = LegendaryCategory.RIVER;

        for (Offset offset : BIOME_CHECK_OFFSETS) {
            biome = level.getBiome(pos.add(offset.x(), 0, offset.z()));
            if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
            if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        }

        return direct;
    }

    private record Offset(int x, int z) {
    }

    public enum LegendaryCategory {
        JUNGLE(StardewFishing.HAS_JUNGLE_FISH),
        ARID(StardewFishing.HAS_ARID_FISH),
        NORMAL_OCEAN(StardewFishing.HAS_NORMAL_OCEAN_FISH),
        WARM_OCEAN(StardewFishing.HAS_WARM_OCEAN_FISH),
        RIVER(StardewFishing.HAS_RIVER_FISH);

        private final TagKey<Biome> tag;

        LegendaryCategory(TagKey<Biome> tag) {
            this.tag = tag;
        }

        public boolean matches(RegistryEntry<Biome> biome) {
            return biome.isIn(tag);
        }

        public Item chooseFish(World level) {
            boolean rand = level.random.nextBoolean();
            return (switch (this) {
                case NORMAL_OCEAN -> rand ? SFItems.STORM_TARPON : SFItems.GOLIATH_GROUPER;
                case WARM_OCEAN -> rand ? SFItems.BLAZING_OARFISH : SFItems.CYCLOPS_MAHIMAHI;
                case RIVER -> rand ? SFItems.DEMON_GAR : SFItems.CRYSTALLINE_SNAKEHEAD;
                case JUNGLE -> rand ? SFItems.CHROMATIC_ARAPAIMA : SFItems.VAMPIRE_PAYARA;
                case ARID -> rand ? SFItems.SABRETOOTHED_TIGERFISH : SFItems.GOLDEN_SNOOK;
            });
        }
    }
}
