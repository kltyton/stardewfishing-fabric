package com.kltyton.stardewfishingFabric.server;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.common.FishBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class FishBehaviorReloadListener extends JsonDataLoader<FishBehaviorReloadListener.FishBehaviorList> {

    public static final Identifier ID = Identifier.of(StardewfishingFabric.MODID, "fish_behavior_reload");
    private static final Logger LOGGER = LogManager.getLogger();
    private static FishBehaviorReloadListener INSTANCE;

    // 存储物品与其对应鱼类行为的映射
    private final Map<Item, FishBehavior> fishBehaviors = new HashMap<>();
    private FishBehavior defaultBehavior;

    private FishBehaviorReloadListener() {
        super(FishBehaviorList.CODEC, ResourceFinder.json("data/" + StardewfishingFabric.MODID));
    }


    @Override
    protected void apply(Map<Identifier, FishBehaviorList> jsonObjects, ResourceManager resourceManager, Profiler profiler) {
        fishBehaviors.clear();
        defaultBehavior = null;

        for (Map.Entry<Identifier, FishBehaviorList> entry : jsonObjects.entrySet()) {
            FishBehaviorList behaviorList = entry.getValue();
            behaviorList.behaviors().forEach((identifier, fishBehavior) -> {
                Item item = Registries.ITEM.get(identifier);
                if (behaviorList.replace() || !fishBehaviors.containsKey(item)) {
                    fishBehaviors.put(item, fishBehavior);
                }
            });

            behaviorList.defaultBehavior().ifPresent(behavior -> defaultBehavior = behavior);
        }
    }

    public static FishBehaviorReloadListener create() {
        INSTANCE = new FishBehaviorReloadListener();
        return INSTANCE;
    }

    public static FishBehavior getBehavior(@Nullable ItemStack stack) {
        if (stack == null) return INSTANCE.defaultBehavior;
        return INSTANCE.fishBehaviors.getOrDefault(stack.getItem(), INSTANCE.defaultBehavior);
    }

    record FishBehaviorList(boolean replace, Map<Identifier, FishBehavior> behaviors, Optional<FishBehavior> defaultBehavior) {
        private static final Codec<FishBehaviorList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(FishBehaviorList::replace),
                Codec.unboundedMap(Identifier.CODEC, FishBehavior.CODEC).fieldOf("behaviors").forGetter(FishBehaviorList::behaviors),
                FishBehavior.CODEC.optionalFieldOf("defaultBehavior").forGetter(FishBehaviorList::defaultBehavior)
        ).apply(inst, FishBehaviorList::new));
    }
}
