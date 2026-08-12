package com.bonker.stardewfishing;

import com.bonker.stardewfishing.common.event.CommonEvents;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFAttributes;
import com.bonker.stardewfishing.registry.SFBlocks;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFLootPoolEntryTypes;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.server.resource.MinigameModifiers;
import com.bonker.stardewfishing.server.resource.MinigameModifiersReloadListener;
import com.bonker.stardewfishing.server.resource.MinigameModifiersSupplier;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public final class StardewFishing implements ModInitializer {
    public static final String MODID = "stardew_fishing";
    public static final Logger LOGGER = LogUtils.getLogger();

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

    public static final ResourceLocation TREASURE_CHEST_LOOT = resource("treasure_chest");
    public static final ResourceLocation TREASURE_CHEST_NETHER_LOOT = resource("treasure_chest_nether");

    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(0xffffbe);

    public static String MOD_NAME = "Stardew Fishing";
    public static MinigameModifiersSupplier clientModifiersSupplier;

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getModContainer(MODID).ifPresent(container ->
                MOD_NAME = container.getMetadata().getName() + " " + container.getMetadata().getVersion().getFriendlyString());

        SFAttributes.initialize();
        SFBlocks.initialize();
        SFItems.initialize();
        SFBlockEntities.initialize();
        SFParticles.initialize();
        SFLootPoolEntryTypes.initialize();
        SFSoundEvents.initialize();
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, SFConfig.SERVER_SPEC);
        CommonEvents.initialize();
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static ResourceLocation resource(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, resource(path));
    }

    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        MinigameModifiersSupplier supplier = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
                ? MinigameModifiersReloadListener.getOrCreate()
                : clientModifiersSupplier;
        return getModifiers(stack, supplier);
    }

    public static Optional<MinigameModifiers> getServerModifiers(ItemStack stack) {
        return getModifiers(stack, MinigameModifiersReloadListener.getOrCreate());
    }

    public static Optional<MinigameModifiers> getClientModifiers(ItemStack stack) {
        return getModifiers(stack, clientModifiersSupplier);
    }

    private static Optional<MinigameModifiers> getModifiers(ItemStack stack, MinigameModifiersSupplier supplier) {
        if (supplier == null) {
            return Optional.empty();
        }
        Map<Item, MinigameModifiers> data = supplier.getData();
        return Optional.ofNullable(data.get(stack.getItem()));
    }
}
