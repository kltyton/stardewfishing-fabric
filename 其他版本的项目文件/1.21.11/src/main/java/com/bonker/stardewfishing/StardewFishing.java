package com.bonker.stardewfishing;

import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.gameplay.MinigameModifiers;
import com.bonker.stardewfishing.registry.SFAttributes;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFBlocks;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFLootPoolEntryTypes;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.data.reload.MinigameModifiersReloadListener;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * Fabric entrypoint. All registrations and event wiring happen here so that
 * common code never class-loads client-only classes.
 */
public final class StardewFishing implements ModInitializer {
    public static final String MODID = "stardew_fishing";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean QUALITY_FOOD_INSTALLED = FabricLoader.getInstance().isModLoaded("quality_food");
    public static final boolean AQUACULTURE_INSTALLED = FabricLoader.getInstance().isModLoaded("aquaculture");
    public static final boolean TIDE_INSTALLED = FabricLoader.getInstance().isModLoaded("tide");
    public static final boolean COBBLEMON_INSTALLED = FabricLoader.getInstance().isModLoaded("cobblemon");

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.of(RegistryKeys.ITEM, identifier("starts_minigame"));
    public static final TagKey<Item> MODIFIABLE_RODS = TagKey.of(RegistryKeys.ITEM, identifier("modifiable_rods"));
    public static final TagKey<Item> BOBBERS = TagKey.of(RegistryKeys.ITEM, identifier("bobbers"));
    public static final TagKey<Item> LEGENDARY_FISH = TagKey.of(RegistryKeys.ITEM, identifier("legendary_fish"));
    public static final TagKey<Item> IN_FISH_DISPLAY = TagKey.of(RegistryKeys.ITEM, identifier("in_fish_display"));

    public static final TagKey<Biome> HAS_NORMAL_OCEAN_FISH = TagKey.of(RegistryKeys.BIOME, identifier("has_normal_ocean_fish"));
    public static final TagKey<Biome> HAS_WARM_OCEAN_FISH = TagKey.of(RegistryKeys.BIOME, identifier("has_warm_ocean_fish"));
    public static final TagKey<Biome> HAS_RIVER_FISH = TagKey.of(RegistryKeys.BIOME, identifier("has_river_fish"));
    public static final TagKey<Biome> HAS_ARID_FISH = TagKey.of(RegistryKeys.BIOME, identifier("has_arid_fish"));
    public static final TagKey<Biome> HAS_JUNGLE_FISH = TagKey.of(RegistryKeys.BIOME, identifier("has_jungle_fish"));

    public static final RegistryKey<net.minecraft.loot.LootTable> TREASURE_CHEST_LOOT =
            RegistryKey.of(RegistryKeys.LOOT_TABLE, identifier("treasure_chest"));
    public static final RegistryKey<net.minecraft.loot.LootTable> TREASURE_CHEST_NETHER_LOOT =
            RegistryKey.of(RegistryKeys.LOOT_TABLE, identifier("treasure_chest_nether"));

    public static String MOD_NAME;

    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(SFItems.LEGENDARY_FISH_COLOR);

    @Override
    public void onInitialize() {
        MOD_NAME = "Stardew Fishing " + FabricLoader.getInstance().getModContainer(MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("3.7-fabric");

        forceRegistryInitialization();
        SFItems.initialize();
        SFConfig.load();
        CommonRegistration.initialize();
    }

    private static void forceRegistryInitialization() {
        SFBlocks.FISH_DISPLAY.toString();
        SFBlockEntities.FISH_DISPLAY.toString();
        SFComponentTypes.BOBBER.toString();
        SFAttributes.LINE_STRENGTH.toString();
        SFParticles.SPARKLE.toString();
        SFLootPoolEntryTypes.MOD_LOADED.toString();
        SFSoundEvents.CAST.toString();
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MODID, path);
    }

    /**
     * Resolves the minigame modifiers for a stack, mirroring the NeoForge reference:
     * dedicated servers read the reloaded data, clients read the synced snapshot.
     */
    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        Map<Item, MinigameModifiers> data = ModifiersAccess.getData();
        if (data != null) {
            MinigameModifiers modifiers = data.get(stack.getItem());
            if (modifiers != null) {
                return Optional.of(modifiers);
            }
        }
        return Optional.empty();
    }

    /**
     * Loader-side switch for {@link #getModifiers}. The client initializer installs
     * the packet-synced supplier; servers use the reload listener.
     */
    public static final class ModifiersAccess {
        private static volatile ModifiersSupplier installed;

        private ModifiersAccess() {
        }

        public static void install(ModifiersSupplier supplier) {
            installed = supplier;
        }

        public static Map<Item, MinigameModifiers> getData() {
            ModifiersSupplier supplier = installed;
            if (supplier != null) {
                return supplier.getData();
            }
            if (FabricLoader.getInstance().isDevelopmentEnvironment()
                    || FabricLoader.getInstance().getEnvironmentType() != net.fabricmc.api.EnvType.CLIENT) {
                return MinigameModifiersReloadListener.getOrCreate().getData();
            }
            return null;
        }
    }

    @FunctionalInterface
    public interface ModifiersSupplier {
        Map<Item, MinigameModifiers> getData();
    }

    /** Attribute helpers used by the minigame logic. */
    public static double getAttributeValue(net.minecraft.entity.player.PlayerEntity player, EntityAttribute attribute) {
        return player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(attribute));
    }

    public StardewFishing() {
    }
}
