package com.bonker.stardewfishing.server.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.minigame.FishBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FishBehaviorReloadListener extends SimplePreparableReloadListener<Map<String, JsonObject>>
        implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation LOCATION = StardewFishing.resource("fish_behaviors.json");
    private static final ResourceLocation OLD_LOCATION = StardewFishing.resource("data.json");
    private static FishBehaviorReloadListener instance;
    private final Map<Item, FishBehavior> behaviors = new HashMap<>();
    private final List<ResourceLocation> keys = new ArrayList<>();
    private FishBehavior defaultBehavior;

    private FishBehaviorReloadListener() {
    }

    @Override
    public ResourceLocation getFabricId() {
        return StardewFishing.resource("fish_behaviors");
    }

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, JsonObject> objects = new LinkedHashMap<>();
        for (Resource resource : manager.getResourceStack(LOCATION)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                objects.put(resource.sourcePackId(), GsonHelper.fromJson(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid fish behavior list {} in data pack {}", LOCATION,
                        resource.sourcePackId(), exception);
            }
        }
        for (Resource resource : manager.getResourceStack(OLD_LOCATION)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                StardewFishing.LOGGER.error("Fish behaviors in data pack {} still use {}. Move the file to {}.",
                        resource.sourcePackId(), OLD_LOCATION, LOCATION);
                objects.putIfAbsent(resource.sourcePackId(), GsonHelper.fromJson(GSON, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid legacy fish behavior list {} in data pack {}", OLD_LOCATION,
                        resource.sourcePackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> objects, ResourceManager manager, ProfilerFiller profiler) {
        behaviors.clear();
        keys.clear();
        defaultBehavior = null;
        for (Map.Entry<String, JsonObject> entry : objects.entrySet()) {
            FishBehaviorList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> StardewFishing.LOGGER.warn(
                            "Failed to decode fish behaviors {} in data pack {}: {}", LOCATION, entry.getKey(), error))
                    .ifPresent(list -> {
                        list.behaviors.forEach((id, behavior) -> {
                            Item item = BuiltInRegistries.ITEM.get(id);
                            if (item == Items.AIR) {
                                if (FabricLoader.getInstance().isModLoaded(id.getNamespace())) {
                                    StardewFishing.LOGGER.warn("Mod '{}' is loaded but behavior item '{}' is missing",
                                            id.getNamespace(), id);
                                }
                            } else if (list.replace || !behaviors.containsKey(item)) {
                                behaviors.put(item, behavior);
                                if (!keys.contains(id)) keys.add(id);
                            }
                        });
                        if (list.replace || defaultBehavior == null) list.defaultBehavior.ifPresent(value -> defaultBehavior = value);
                    });
        }
        Collections.sort(keys);
    }

    public static FishBehaviorReloadListener create() {
        instance = new FishBehaviorReloadListener();
        return instance;
    }

    public static @Nullable FishBehavior getBehavior(@Nullable ItemStack stack) {
        if (instance == null) return null;
        return stack == null ? instance.defaultBehavior
                : instance.behaviors.getOrDefault(stack.getItem(), instance.defaultBehavior);
    }

    public static List<ResourceLocation> getKeys() {
        return instance == null ? List.of() : List.copyOf(instance.keys);
    }

    private record FishBehaviorList(boolean replace, Map<ResourceLocation, FishBehavior> behaviors,
                                    Optional<FishBehavior> defaultBehavior) {
        private static final Codec<FishBehaviorList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(FishBehaviorList::replace),
                Codec.unboundedMap(ResourceLocation.CODEC, FishBehavior.CODEC).fieldOf("behaviors").forGetter(FishBehaviorList::behaviors),
                FishBehavior.CODEC.optionalFieldOf("defaultBehavior").forGetter(FishBehaviorList::defaultBehavior)
        ).apply(instance, FishBehaviorList::new));
    }
}
