package com.bonker.stardewfishing.server.loot;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.LegendaryCatch;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import com.bonker.stardewfishing.registry.SFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

public final class LegendaryFishModifier {
    private static final List<Identifier> FISHING_LOOT_TABLES = List.of(BuiltInLootTables.FISHING.identifier());
    private static final List<Vector2i> BIOME_CHECK_OFFSETS = List.of(
            new Vector2i(16, 0), new Vector2i(-16, 0), new Vector2i(0, 16), new Vector2i(0, -16),
            new Vector2i(16, 16), new Vector2i(16, -16), new Vector2i(-16, -16), new Vector2i(-16, 16),
            new Vector2i(32, 0), new Vector2i(-32, 0), new Vector2i(0, 32), new Vector2i(0, -32),
            new Vector2i(32, 32), new Vector2i(32, -32), new Vector2i(-32, -32), new Vector2i(-32, 32));

    private LegendaryFishModifier() {
    }

    public static void modify(Holder<LootTable> table, LootContext context, List<ItemStack> generatedLoot) {
        if (generatedLoot.isEmpty()
                || context.getRandom().nextFloat() >= SFConfig.getLegendaryFishChance(context.getLuck())
                || LootContextParamSets.FISHING.required().stream().anyMatch(param -> !context.hasParameter(param))) {
            return;
        }

        int fishIndex = -1;
        for (int index = 0; index < generatedLoot.size(); index++) {
            if (generatedLoot.get(index).is(ItemTags.FISHES)) {
                fishIndex = index;
                break;
            }
        }
        if (fishIndex < 0) {
            return;
        }

        Identifier lootId = table.unwrapKey().map(key -> key.identifier()).orElse(null);
        if (lootId == null || FISHING_LOOT_TABLES.stream().noneMatch(id -> id.equals(lootId))) {
            return;
        }

        ServerLevel level = context.getLevel();
        BlockPos pos = BlockPos.containing(context.getParameter(LootContextParams.ORIGIN));
        LegendaryCategory category = findLegendaryCategory(level, pos);
        if (category == null) {
            return;
        }

        ItemStack legendaryFish = new ItemStack(category.chooseFish(level));
        Entity source = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (source instanceof FishingHook hook && hook.getPlayerOwner() instanceof Player player) {
            legendaryFish.set(SFComponentTypes.LEGENDARY_CATCH, new LegendaryCatch(player));
        }
        generatedLoot.set(fishIndex, legendaryFish);
    }

    private static @Nullable LegendaryCategory findLegendaryCategory(Level level, BlockPos pos) {
        LegendaryCategory direct = null;
        Holder<Biome> biome = level.getBiome(pos);
        if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
        if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        if (LegendaryCategory.WARM_OCEAN.matches(biome)) direct = LegendaryCategory.WARM_OCEAN;
        else if (LegendaryCategory.NORMAL_OCEAN.matches(biome)) direct = LegendaryCategory.NORMAL_OCEAN;
        else if (LegendaryCategory.RIVER.matches(biome)) direct = LegendaryCategory.RIVER;

        for (Vector2i offset : BIOME_CHECK_OFFSETS) {
            biome = level.getBiome(pos.offset(offset.x, 0, offset.y));
            if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
            if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        }
        return direct;
    }

    private enum LegendaryCategory {
        JUNGLE(StardewFishing.HAS_JUNGLE_FISH),
        ARID(StardewFishing.HAS_ARID_FISH),
        NORMAL_OCEAN(StardewFishing.HAS_NORMAL_OCEAN_FISH),
        WARM_OCEAN(StardewFishing.HAS_WARM_OCEAN_FISH),
        RIVER(StardewFishing.HAS_RIVER_FISH);

        private final TagKey<Biome> tag;

        LegendaryCategory(TagKey<Biome> tag) {
            this.tag = tag;
        }

        private boolean matches(Holder<Biome> biome) {
            return biome.is(tag);
        }

        private Item chooseFish(Level level) {
            boolean alternate = level.getRandom().nextBoolean();
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
