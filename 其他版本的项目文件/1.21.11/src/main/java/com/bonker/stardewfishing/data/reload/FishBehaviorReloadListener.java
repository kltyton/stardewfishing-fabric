package com.bonker.stardewfishing.data.reload;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.FishBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Loads per-item fish behaviors from {@code data/stardew_fishing/fish_behaviors.json}. */
public class FishBehaviorReloadListener extends SinglePreparationResourceReloader<Map<String, JsonObject>>
        implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Identifier LOCATION = StardewFishing.identifier("fish_behaviors.json");
    private static final Identifier OLD_LOCATION = StardewFishing.identifier("data.json");
    private static FishBehaviorReloadListener instance;

    private final Map<Item, FishBehavior> fishBehaviors = new HashMap<>();
    private final List<Identifier> keys = new ArrayList<>();
    private FishBehavior defaultBehavior;

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager resourceManager, Profiler profiler) {
        Map<String, JsonObject> objects = new HashMap<>();
        for (Resource resource : resourceManager.getAllResources(LOCATION)) {
            try (Reader reader = resource.getReader()) {
                objects.put(resource.getPackId(), JsonHelper.deserialize(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in fish behavior list {} in data pack {}",
                        LOCATION, resource.getPackId(), exception);
            }
        }

        for (Resource resource : resourceManager.getAllResources(OLD_LOCATION)) {
            try (Reader reader = resource.getReader()) {
                StardewFishing.LOGGER.error("Error in datapack {}: Fish behavior list found at stardew_fishing/data.json. "
                                + "As of 3.0, fish behavior has been moved to stardew_fishing/fish_behaviors.json. "
                                + "This file has been loaded, but it will not be in the future.",
                        resource.getPackId());
                objects.put(resource.getPackId(), JsonHelper.deserialize(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in fish behavior list {} in data pack {}",
                        LOCATION, resource.getPackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> jsonObjects, ResourceManager resourceManager, Profiler profiler) {
        fishBehaviors.clear();
        keys.clear();
        defaultBehavior = null;

        for (Map.Entry<String, JsonObject> entry : jsonObjects.entrySet()) {
            FishBehaviorList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(errorMsg -> StardewFishing.LOGGER.warn(makeError(entry.getKey(), errorMsg)))
                    .ifPresent(behaviorList -> behaviorList.behaviors.forEach((loc, fishBehavior) -> {
                        Item item = Registries.ITEM.get(loc);
                        if (item == Items.AIR) {
                            if (FabricLoader.getInstance().isModLoaded(loc.getNamespace())) {
                                StardewFishing.LOGGER.warn("Mod '{}' present but item not registered: {}. Is the id incorrect?",
                                        loc.getNamespace(), loc.getPath());
                            }
                        } else {
                            if (behaviorList.replace || !fishBehaviors.containsKey(item)) {
                                fishBehaviors.put(item, fishBehavior);
                                keys.add(loc);
                            }

                            if (behaviorList.replace || defaultBehavior == null) {
                                behaviorList.defaultBehavior.ifPresent(behavior -> defaultBehavior = behavior);
                            }
                        }
                    }));
        }

        Collections.sort(keys);
    }

    private static String makeError(String datapackId, String description) {
        return "Failed to decode fish behavior list " + LOCATION + " in data pack " + datapackId + " - " + description;
    }

    public static FishBehaviorReloadListener create() {
        instance = new FishBehaviorReloadListener();
        return instance;
    }

    public static FishBehavior getBehavior(@Nullable ItemStack stack) {
        if (stack == null) return instance.defaultBehavior;
        return instance.fishBehaviors.getOrDefault(stack.getItem(), instance.defaultBehavior);
    }

    public static List<Identifier> getKeys() {
        return instance.keys;
    }

    @Override
    public Identifier getFabricId() {
        return LOCATION;
    }

    private record FishBehaviorList(boolean replace, Map<Identifier, FishBehavior> behaviors,
                                    Optional<FishBehavior> defaultBehavior) {
        private static final Codec<FishBehaviorList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(FishBehaviorList::replace),
                Codec.unboundedMap(Identifier.CODEC, FishBehavior.CODEC).fieldOf("behaviors").forGetter(FishBehaviorList::behaviors),
                FishBehavior.CODEC.optionalFieldOf("defaultBehavior").forGetter(FishBehaviorList::defaultBehavior)
        ).apply(inst, FishBehaviorList::new));
    }
}
