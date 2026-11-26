package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

public final class LegendaryFishModifier {
    private static final List<Vector2i> BIOME_CHECK_OFFSETS = List.of(
            new Vector2i(16, 0), new Vector2i(-16, 0), new Vector2i(0, 16), new Vector2i(0, -16),
            new Vector2i(16, 16), new Vector2i(16, -16), new Vector2i(-16, -16), new Vector2i(-16, 16),
            new Vector2i(32, 0), new Vector2i(-32, 0), new Vector2i(0, 32), new Vector2i(0, -32),
            new Vector2i(32, 32), new Vector2i(32, -32), new Vector2i(-32, -32), new Vector2i(-32, 32));

    public static void applyFishingLoot(List<ItemStack> loot, ServerLevel level, BlockPos pos, float luck) {
        if (loot.isEmpty() || level.random.nextFloat() >= SFConfig.getLegendaryFishChance(luck)) return;
        for (int index = 0; index < loot.size(); index++) {
            if (!loot.get(index).is(ItemTags.FISHES)) continue;
            LegendaryCategory category = findCategory(level, pos);
            if (category != null) loot.set(index, new ItemStack(category.chooseFish(level)));
            return;
        }
    }

    @Nullable
    private static LegendaryCategory findCategory(Level level, BlockPos pos) {
        LegendaryCategory direct = match(level.getBiome(pos));
        if (direct == LegendaryCategory.JUNGLE || direct == LegendaryCategory.ARID) return direct;
        for (Vector2i offset : BIOME_CHECK_OFFSETS) {
            LegendaryCategory nearby = match(level.getBiome(pos.offset(offset.x, 0, offset.y)));
            if (nearby == LegendaryCategory.JUNGLE || nearby == LegendaryCategory.ARID) return nearby;
        }
        return direct;
    }

    @Nullable
    private static LegendaryCategory match(Holder<Biome> biome) {
        if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
        if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        if (LegendaryCategory.WARM_OCEAN.matches(biome)) return LegendaryCategory.WARM_OCEAN;
        if (LegendaryCategory.NORMAL_OCEAN.matches(biome)) return LegendaryCategory.NORMAL_OCEAN;
        if (LegendaryCategory.RIVER.matches(biome)) return LegendaryCategory.RIVER;
        return null;
    }

    private enum LegendaryCategory {
        JUNGLE(StardewFishing.HAS_JUNGLE_FISH), ARID(StardewFishing.HAS_ARID_FISH),
        NORMAL_OCEAN(StardewFishing.HAS_NORMAL_OCEAN_FISH), WARM_OCEAN(StardewFishing.HAS_WARM_OCEAN_FISH),
        RIVER(StardewFishing.HAS_RIVER_FISH);

        private final TagKey<Biome> tag;
        LegendaryCategory(TagKey<Biome> tag) { this.tag = tag; }
        boolean matches(Holder<Biome> biome) { return biome.is(tag); }
        Item chooseFish(Level level) {
            boolean alternate = level.random.nextBoolean();
            return (switch (this) {
                case NORMAL_OCEAN -> alternate ? SFItems.STORM_TARPON : SFItems.GOLIATH_GROUPER;
                case WARM_OCEAN -> alternate ? SFItems.BLAZING_OARFISH : SFItems.CYCLOPS_MAHIMAHI;
                case RIVER -> alternate ? SFItems.DEMON_GAR : SFItems.CRYSTALLINE_SNAKEHEAD;
                case JUNGLE -> alternate ? SFItems.CHROMATIC_ARAPAIMA : SFItems.VAMPIRE_PAYARA;
                case ARID -> alternate ? SFItems.SABRETOOTHED_TIGERFISH : SFItems.GOLDEN_SNOOK;
            }).get();
        }
    }

    private LegendaryFishModifier() {
    }
}
