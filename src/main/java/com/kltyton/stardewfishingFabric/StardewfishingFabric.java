package com.kltyton.stardewfishingFabric;

import com.kltyton.stardewfishingFabric.common.CommonEvents;
import com.kltyton.stardewfishingFabric.common.config.SFConfig;
import com.kltyton.stardewfishingFabric.registry.SFBlockEntities;
import com.kltyton.stardewfishingFabric.registry.SFBlocks;
import com.kltyton.stardewfishingFabric.registry.SFItems;
import com.kltyton.stardewfishingFabric.registry.SFLootPoolEntryTypes;
import com.kltyton.stardewfishingFabric.registry.SFParticles;
import com.kltyton.stardewfishingFabric.registry.SFSoundEvents;
import com.kltyton.stardewfishingFabric.server.resource.MinigameModifiers;
import com.kltyton.stardewfishingFabric.server.resource.MinigameModifiersReloadListener;
import com.kltyton.stardewfishingFabric.server.resource.MinigameModifiersSupplier;
import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public final class StardewfishingFabric implements ModInitializer {
    public static final String MODID = "stardew_fishing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.create(Registries.ITEM, id("starts_minigame"));
    public static final TagKey<Item> MODIFIABLE_RODS = TagKey.create(Registries.ITEM, id("modifiable_rods"));
    public static final TagKey<Item> BOBBERS = TagKey.create(Registries.ITEM, id("bobbers"));
    public static final TagKey<Item> LEGENDARY_FISH = TagKey.create(Registries.ITEM, id("legendary_fish"));
    public static final TagKey<Item> IN_FISH_DISPLAY = TagKey.create(Registries.ITEM, id("in_fish_display"));

    public static final TagKey<Biome> HAS_NORMAL_OCEAN_FISH = TagKey.create(Registries.BIOME, id("has_normal_ocean_fish"));
    public static final TagKey<Biome> HAS_WARM_OCEAN_FISH = TagKey.create(Registries.BIOME, id("has_warm_ocean_fish"));
    public static final TagKey<Biome> HAS_RIVER_FISH = TagKey.create(Registries.BIOME, id("has_river_fish"));
    public static final TagKey<Biome> HAS_ARID_FISH = TagKey.create(Registries.BIOME, id("has_arid_fish"));
    public static final TagKey<Biome> HAS_JUNGLE_FISH = TagKey.create(Registries.BIOME, id("has_jungle_fish"));

    public static final ResourceLocation TREASURE_CHEST_LOOT = id("treasure_chest");
    public static final ResourceLocation TREASURE_CHEST_NETHER_LOOT = id("treasure_chest_nether");

    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(0xffffbe);

    public static String MOD_NAME = "Stardew Fishing";
    public static MinigameModifiersSupplier clientModifiersSupplier;

    // Kept as public aliases for Tide and older Fabric integrations.
    public static final SoundEvent CAST = SFSoundEvents.CAST;
    public static final SoundEvent COMPLETE = SFSoundEvents.COMPLETE;
    public static final SoundEvent DWOP = SFSoundEvents.DWOP;
    public static final SoundEvent FISH_ESCAPE = SFSoundEvents.FISH_ESCAPE;
    public static final SoundEvent FISH_BITE = SFSoundEvents.FISH_BITE;
    public static final SoundEvent FISH_HIT = SFSoundEvents.FISH_HIT;
    public static final SoundEvent PULL_ITEM = SFSoundEvents.PULL_ITEM;
    public static final SoundEvent REEL_CREAK = SFSoundEvents.REEL_CREAK;
    public static final SoundEvent REEL_FAST = SFSoundEvents.REEL_FAST;
    public static final SoundEvent REEL_SLOW = SFSoundEvents.REEL_SLOW;

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getModContainer(MODID).ifPresent(container ->
                MOD_NAME = container.getMetadata().getName() + " " + container.getMetadata().getVersion().getFriendlyString());

        SFBlocks.initialize();
        SFItems.initialize();
        SFBlockEntities.initialize();
        SFParticles.initialize();
        SFLootPoolEntryTypes.initialize();
        SFSoundEvents.initialize();
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, SFConfig.SERVER_SPEC);
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, SFConfig.CLIENT_SPEC);
        CommonEvents.initialize();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, id(path));
    }

    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        MinigameModifiersSupplier supplier = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
                ? MinigameModifiersReloadListener.getOrCreate()
                : clientModifiersSupplier;
        if (supplier == null) {
            return Optional.empty();
        }
        Map<Item, MinigameModifiers> data = supplier.getData();
        return Optional.ofNullable(data.get(stack.getItem()));
    }
}
