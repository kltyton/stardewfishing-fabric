package com.bonker.stardewfishing.server.loot;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.registry.SFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LegendaryFishSelector {
    private static final int[][] BIOME_OFFSETS = {
            {16, 0}, {-16, 0}, {0, 16}, {0, -16}, {16, 16}, {16, -16}, {-16, -16}, {-16, 16},
            {32, 0}, {-32, 0}, {0, 32}, {0, -32}, {32, 32}, {32, -32}, {-32, -32}, {-32, 32}
    };

    private LegendaryFishSelector() {
    }

    public static void replaceFirstFish(List<ItemStack> rewards, FishingHook hook, net.minecraft.world.entity.player.Player player) {
        if (rewards.isEmpty() || !(hook.level() instanceof ServerLevel level)) return;
        float luck = FishingItemSupport.getLuck(hook) + player.getLuck();
        if (level.random.nextFloat() >= SFConfig.getLegendaryFishChance(luck)) return;

        int fishIndex = -1;
        for (int i = 0; i < rewards.size(); i++) {
            if (rewards.get(i).is(ItemTags.FISHES)) {
                fishIndex = i;
                break;
            }
        }
        if (fishIndex < 0) return;

        Category category = findCategory(level, hook.blockPosition());
        if (category != null) rewards.set(fishIndex, new ItemStack(category.choose(level)));
    }

    private static @Nullable Category findCategory(ServerLevel level, BlockPos pos) {
        Category direct = directCategory(level.getBiome(pos));
        if (direct == Category.JUNGLE || direct == Category.ARID) return direct;
        for (int[] offset : BIOME_OFFSETS) {
            Category nearby = directCategory(level.getBiome(pos.offset(offset[0], 0, offset[1])));
            if (nearby == Category.JUNGLE || nearby == Category.ARID) return nearby;
        }
        return direct;
    }

    private static @Nullable Category directCategory(Holder<Biome> biome) {
        if (Category.JUNGLE.matches(biome)) return Category.JUNGLE;
        if (Category.ARID.matches(biome)) return Category.ARID;
        if (Category.WARM_OCEAN.matches(biome)) return Category.WARM_OCEAN;
        if (Category.NORMAL_OCEAN.matches(biome)) return Category.NORMAL_OCEAN;
        if (Category.RIVER.matches(biome)) return Category.RIVER;
        return null;
    }

    private enum Category {
        JUNGLE(StardewFishing.HAS_JUNGLE_FISH),
        ARID(StardewFishing.HAS_ARID_FISH),
        NORMAL_OCEAN(StardewFishing.HAS_NORMAL_OCEAN_FISH),
        WARM_OCEAN(StardewFishing.HAS_WARM_OCEAN_FISH),
        RIVER(StardewFishing.HAS_RIVER_FISH);

        private final TagKey<Biome> tag;

        Category(TagKey<Biome> tag) {
            this.tag = tag;
        }

        boolean matches(Holder<Biome> biome) {
            return biome.is(tag);
        }

        Item choose(ServerLevel level) {
            boolean alternate = level.random.nextBoolean();
            return switch (this) {
                case NORMAL_OCEAN -> alternate ? SFItems.STORM_TARPON : SFItems.GOLIATH_GROUPER;
                case WARM_OCEAN -> alternate ? SFItems.BLAZING_OARFISH : SFItems.CYCLOPS_MAHIMAHI;
                case RIVER -> alternate ? SFItems.DEMON_GAR : SFItems.CRYSTALLINE_SNAKEHEAD;
                case JUNGLE -> alternate ? SFItems.CHROMATIC_ARAPAIMA : SFItems.VAMPIRE_PAYARA;
                case ARID -> alternate ? SFItems.SABRETOOTHED_TIGERFISH : SFItems.GOLDEN_SNOOK;
            };
        }
    }
}
