package com.bonker.stardewfishing.server.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.compat.tide.TideCompat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinigameModifiersReloadListener extends SimplePreparableReloadListener<Map<String, JsonObject>>
        implements IdentifiableResourceReloadListener, MinigameModifiersSupplier {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation LOCATION = StardewFishing.resource("minigame_modifiers.json");
    private static MinigameModifiersReloadListener instance;
    private final Map<Item, MinigameModifiers> modifiers = new LinkedHashMap<>();

    private MinigameModifiersReloadListener() {
    }

    @Override
    public ResourceLocation getFabricId() {
        return StardewFishing.resource("minigame_modifiers");
    }

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, JsonObject> objects = new LinkedHashMap<>();
        for (Resource resource : manager.getResourceStack(LOCATION)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                objects.put(resource.sourcePackId(), GsonHelper.fromJson(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid minigame modifiers {} in data pack {}", LOCATION,
                        resource.sourcePackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> objects, ResourceManager manager, ProfilerFiller profiler) {
        modifiers.clear();
        for (Map.Entry<String, JsonObject> entry : objects.entrySet()) {
            ModifiersList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> StardewFishing.LOGGER.warn(
                            "Failed to decode minigame modifiers {} in data pack {}: {}", LOCATION, entry.getKey(), error))
                    .ifPresent(list -> list.modifiers.forEach((id, loaded) -> {
                        Item item = TideCompat.resolveRenamedItem(id);
                        if (item == Items.AIR) {
                            if (FabricLoader.getInstance().isModLoaded(id.getNamespace())) {
                                StardewFishing.LOGGER.warn("Mod '{}' is loaded but modifier item '{}' is missing",
                                        id.getNamespace(), id);
                            }
                        } else if (list.replace || !modifiers.containsKey(item)) {
                            modifiers.put(item, loaded);
                        } else {
                            modifiers.computeIfPresent(item, (ignored, existing) -> existing.merge(loaded));
                        }
                    }));
        }
    }

    public static MinigameModifiersReloadListener getOrCreate() {
        if (instance == null) instance = new MinigameModifiersReloadListener();
        return instance;
    }

    @Override
    public Map<Item, MinigameModifiers> getData() {
        return modifiers;
    }

    private record ModifiersList(boolean replace, Map<ResourceLocation, MinigameModifiers> modifiers) {
        private static final Codec<ModifiersList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(ModifiersList::replace),
                Codec.unboundedMap(ResourceLocation.CODEC, MinigameModifiers.CODEC).fieldOf("modifiers").forGetter(ModifiersList::modifiers)
        ).apply(instance, ModifiersList::new));
    }
}
