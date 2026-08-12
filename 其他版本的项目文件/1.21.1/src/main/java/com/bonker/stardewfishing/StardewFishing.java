package com.bonker.stardewfishing;

import com.bonker.stardewfishing.common.CommonEvents;
import com.bonker.stardewfishing.common.init.*;
import com.bonker.stardewfishing.proxy.MinigameModifiersSupplier;
import com.bonker.stardewfishing.server.data.MinigameModifiers;
import com.bonker.stardewfishing.server.data.MinigameModifiersReloadListener;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public final class StardewFishing implements ModInitializer {
    public static final String MODID = "stardew_fishing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean QUALITY_FOOD_INSTALLED = FabricLoader.getInstance().isModLoaded("quality_food");
    public static final boolean AQUACULTURE_INSTALLED = FabricLoader.getInstance().isModLoaded("aquaculture");
    public static final boolean TIDE_INSTALLED = FabricLoader.getInstance().isModLoaded("tide");
    public static final boolean COBBLEMON_INSTALLED = FabricLoader.getInstance().isModLoaded("cobblemon");
    public static final boolean FISHING_REAL_INSTALLED = FabricLoader.getInstance().isModLoaded("fishingreal");

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.create(Registries.ITEM, resource("starts_minigame"));
    public static final TagKey<Item> MODIFIABLE_RODS = TagKey.create(Registries.ITEM, resource("modifiable_rods"));
    public static final TagKey<Item> BOBBERS = TagKey.create(Registries.ITEM, resource("bobbers"));
    public static final TagKey<Item> LEGENDARY_FISH = TagKey.create(Registries.ITEM, resource("legendary_fish"));
    public static final TagKey<Item> IN_FISH_DISPLAY = TagKey.create(Registries.ITEM, resource("in_fish_display"));
    public static final TagKey<Biome> HAS_NORMAL_OCEAN_FISH = TagKey.create(Registries.BIOME, resource("has_normal_ocean_fish"));
    public static final TagKey<Biome> HAS_WARM_OCEAN_FISH = TagKey.create(Registries.BIOME, resource("has_warm_ocean_fish"));
    public static final TagKey<Biome> HAS_RIVER_FISH = TagKey.create(Registries.BIOME, resource("has_river_fish"));
    public static final TagKey<Biome> HAS_ARID_FISH = TagKey.create(Registries.BIOME, resource("has_arid_fish"));
    public static final TagKey<Biome> HAS_JUNGLE_FISH = TagKey.create(Registries.BIOME, resource("has_jungle_fish"));

    public static final ResourceKey<LootTable> TREASURE_CHEST_LOOT = resource(Registries.LOOT_TABLE, "treasure_chest");
    public static final ResourceKey<LootTable> TREASURE_CHEST_NETHER_LOOT = resource(Registries.LOOT_TABLE, "treasure_chest_nether");
    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(SFItems.LEGENDARY_FISH_COLOR);
    public static String MOD_NAME = "Stardew Fishing 3.7";
    private static volatile MinigameModifiersSupplier clientModifiersSupplier;

    @Override
    public void onInitialize() {
        MOD_NAME = FabricLoader.getInstance().getModContainer(MODID)
                .map(container -> container.getMetadata().getName() + " " + container.getMetadata().getVersion().getFriendlyString())
                .orElse(MOD_NAME);
        SFBlocks.initialize();
        SFItems.initialize();
        SFComponentTypes.initialize();
        SFParticles.initialize();
        SFSoundEvents.initialize();
        SFLootPoolEntryTypes.initialize();
        SFBlockEntities.initialize();
        SFAttributes.initialize();
        SFConfig.load();
        CommonEvents.initialize();
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static <T> ResourceKey<T> resource(ResourceKey<Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, resource(path));
    }

    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        MinigameModifiersSupplier supplier = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                ? clientModifiersSupplier : null;
        if (supplier == null) {
            supplier = MinigameModifiersReloadListener.getOrCreate();
        }
        Map<Item, MinigameModifiers> data = supplier.getData();
        return Optional.ofNullable(data.get(stack.getItem()));
    }

    public static void installClientModifiersSupplier(MinigameModifiersSupplier supplier) {
        clientModifiersSupplier = supplier;
    }
}
