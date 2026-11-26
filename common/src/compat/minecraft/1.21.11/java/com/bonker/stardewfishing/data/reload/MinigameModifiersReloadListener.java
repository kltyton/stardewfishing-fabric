package com.bonker.stardewfishing.data.reload;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.MinigameModifiers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/** Loads per-item minigame modifiers from {@code data/stardew_fishing/minigame_modifiers.json}. */
public class MinigameModifiersReloadListener extends SinglePreparationResourceReloader<Map<String, JsonObject>>
        implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Identifier LOCATION = StardewFishing.identifier("minigame_modifiers.json");
    @Nullable
    private static MinigameModifiersReloadListener instance;

    private final Map<Item, MinigameModifiers> modifiers = new HashMap<>();

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager resourceManager, Profiler profiler) {
        Map<String, JsonObject> objects = new HashMap<>();
        for (Resource resource : resourceManager.getAllResources(LOCATION)) {
            try (Reader reader = resource.getReader()) {
                objects.put(resource.getPackId(), JsonHelper.deserialize(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in minigame modifiers list {} in data pack {}",
                        LOCATION, resource.getPackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> jsonObjects, ResourceManager resourceManager, Profiler profiler) {
        modifiers.clear();

        for (Map.Entry<String, JsonObject> entry : jsonObjects.entrySet()) {
            ModifiersList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(errorMsg -> StardewFishing.LOGGER.warn(
                            "Failed to decode minigame modifiers list {} in data pack {} - {}", LOCATION, entry.getKey(), errorMsg))
                    .ifPresent(behaviorList -> behaviorList.modifiers.forEach((loc, minigameModifiers) -> {
                        Item item = Registries.ITEM.get(loc);
                        if (item == Items.AIR) {
                            if (FabricLoader.getInstance().isModLoaded(loc.getNamespace())) {
                                StardewFishing.LOGGER.warn("Mod '{}' present but item not registered: {}. Is the id incorrect?",
                                        loc.getNamespace(), loc.getPath());
                            }
                        } else {
                            if (behaviorList.replace || !modifiers.containsKey(item)) {
                                modifiers.put(item, minigameModifiers);
                            } else {
                                modifiers.computeIfPresent(item, (i, m) -> m.merge(minigameModifiers));
                            }
                        }
                    }));
        }
    }

    public static MinigameModifiersReloadListener getOrCreate() {
        if (instance == null) {
            instance = new MinigameModifiersReloadListener();
        }
        return instance;
    }

    public Map<Item, MinigameModifiers> getData() {
        if (modifiers.isEmpty()) {
            StardewFishing.LOGGER.error("No minigame modifiers data is present. Was it accessed before it was loaded?");
        }
        return modifiers;
    }

    @Override
    public Identifier getFabricId() {
        return LOCATION;
    }

    private record ModifiersList(boolean replace, Map<Identifier, MinigameModifiers> modifiers) {
        private static final Codec<ModifiersList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(ModifiersList::replace),
                Codec.unboundedMap(Identifier.CODEC, MinigameModifiers.CODEC).fieldOf("modifiers").forGetter(ModifiersList::modifiers)
        ).apply(inst, ModifiersList::new));
    }
}
