package com.bonker.stardewfishing;

import com.bonker.stardewfishing.client.StardewFishingClient;
import com.bonker.stardewfishing.common.config.SFConfig;
import com.bonker.stardewfishing.registry.SFAttributes;
import com.bonker.stardewfishing.registry.SFAttachmentTypes;
import com.bonker.stardewfishing.registry.SFBlockEntities;
import com.bonker.stardewfishing.registry.SFBlocks;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFLootPoolEntryTypes;
import com.bonker.stardewfishing.registry.SFParticles;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.common.networking.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.server.resource.MinigameModifiersSupplier;
import com.bonker.stardewfishing.server.command.SFCommands;
import com.bonker.stardewfishing.server.resource.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.resource.MinigameModifiers;
import com.bonker.stardewfishing.server.resource.MinigameModifiersReloadListener;
import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public class StardewFishing implements ModInitializer {
    public static final String MODID = "stardew_fishing";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean QUALITY_FOOD_INSTALLED = FabricLoader.getInstance().isModLoaded("quality_food");
    public static final boolean AQUACULTURE_INSTALLED = FabricLoader.getInstance().isModLoaded("aquaculture");
    public static final boolean TIDE_INSTALLED = FabricLoader.getInstance().isModLoaded("tide");

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.create(Registries.ITEM, identifier("starts_minigame"));
    public static final TagKey<Item> MODIFIABLE_RODS = TagKey.create(Registries.ITEM, identifier("modifiable_rods"));
    public static final TagKey<Item> BOBBERS = TagKey.create(Registries.ITEM, identifier("bobbers"));
    public static final TagKey<Item> LEGENDARY_FISH = TagKey.create(Registries.ITEM, identifier("legendary_fish"));
    public static final TagKey<Item> IN_FISH_DISPLAY = TagKey.create(Registries.ITEM, identifier("in_fish_display"));

    public static final TagKey<Biome> HAS_NORMAL_OCEAN_FISH = TagKey.create(Registries.BIOME, identifier("has_normal_ocean_fish"));
    public static final TagKey<Biome> HAS_WARM_OCEAN_FISH = TagKey.create(Registries.BIOME, identifier("has_warm_ocean_fish"));
    public static final TagKey<Biome> HAS_RIVER_FISH = TagKey.create(Registries.BIOME, identifier("has_river_fish"));
    public static final TagKey<Biome> HAS_ARID_FISH = TagKey.create(Registries.BIOME, identifier("has_arid_fish"));
    public static final TagKey<Biome> HAS_JUNGLE_FISH = TagKey.create(Registries.BIOME, identifier("has_jungle_fish"));

    public static final ResourceKey<LootTable> TREASURE_CHEST_LOOT = resourceKey(Registries.LOOT_TABLE, "treasure_chest");
    public static final ResourceKey<LootTable> TREASURE_CHEST_NETHER_LOOT = resourceKey(Registries.LOOT_TABLE, "treasure_chest_nether");

    public static String MOD_NAME;

    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(SFItems.LEGENDARY_FISH_COLOR);

    @Override
    public void onInitialize() {
        MOD_NAME = FabricLoader.getInstance()
                .getModContainer(MODID)
                .map(container -> container.getMetadata().getName() + " " + container.getMetadata().getVersion().getFriendlyString())
                .orElse("Stardew Fishing");

        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, SFConfig.SERVER_SPEC);

        SFItems.registerCreativeTab();
        SFBlocks.register();
        SFCommands.registerArgumentType();
        SFParticles.register();
        SFAttributes.register();
        SFSoundEvents.register();
        SFBlockEntities.register();
        SFAttachmentTypes.register();
        SFComponentTypes.register();
        SFLootPoolEntryTypes.register();

        PayloadTypeRegistry.serverboundPlay().register(
                C2SCompleteMinigamePacket.TYPE, C2SCompleteMinigamePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                S2CStartMinigamePacket.TYPE, S2CStartMinigamePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                S2CSyncModifiersPacket.TYPE, S2CSyncModifiersPacket.STREAM_CODEC);

        CreativeModeTabEvents.modifyOutputEvent(SFItems.TAB_KEY).register(SFItems::addToCreativeTab);

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> SFCommands.register(dispatcher, buildContext));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FishBehaviorReloadListener.create());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(MinigameModifiersReloadListener.getOrCreate());

        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePacket.TYPE, C2SCompleteMinigamePacket::handle);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(handler.player, new S2CSyncModifiersPacket(MinigameModifiersReloadListener.getOrCreate().getData())));
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static <T> ResourceKey<T> resourceKey(ResourceKey<Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, identifier(path));
    }

    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        MinigameModifiersSupplier modifiersSupplier;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            modifiersSupplier = MinigameModifiersReloadListener.getOrCreate();
        } else {
            modifiersSupplier = StardewFishingClient.modifiersSupplier;
        }

        if (modifiersSupplier != null) {
            Map<Item, MinigameModifiers> data = modifiersSupplier.getData();
            if (data.containsKey(stack.getItem())) {
                return Optional.of(data.get(stack.getItem()));
            }
        }
        return Optional.empty();
    }
}
