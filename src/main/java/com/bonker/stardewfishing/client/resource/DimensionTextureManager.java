package com.bonker.stardewfishing.client.resource;

import com.bonker.stardewfishing.StardewFishing;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.slf4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DimensionTextureManager extends SimplePreparableReloadListener<Set<ResourceLocation>> implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEXTURE_DIRECTORY = "textures/gui/minigame_backgrounds/";
    private static final ResourceLocation DEFAULT_TEXTURE = StardewFishing.resource(
            "textures/gui/minigame_backgrounds/minecraft/overworld.png");

    @Nullable
    private static DimensionTextureManager INSTANCE;

    private final Map<ResourceLocation, ResourceLocation> dimensionTextureMap = new HashMap<>();

    private DimensionTextureManager() {
    }

    public static DimensionTextureManager getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new DimensionTextureManager();
        }
        return INSTANCE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return StardewFishing.resource("dimension_texture_manager");
    }

    @Override
    protected Set<ResourceLocation> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return pResourceManager.listResources("textures/gui/minigame_backgrounds",
                        loc -> loc.getNamespace().equals(StardewFishing.MODID) && loc.getPath().endsWith(".png"))
                .keySet();
    }

    @Override
    protected void apply(Set<ResourceLocation> candidates, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.dimensionTextureMap.clear();

        for (ResourceLocation texture : candidates) {
            String path = texture.getPath();
            String relative = path.substring(TEXTURE_DIRECTORY.length(), path.length() - 4);
            int namespaceSeparator = relative.indexOf('/');
            if (namespaceSeparator <= 0 || namespaceSeparator == relative.length() - 1) {
                LOGGER.warn("Invalid location for a minigame dimension texture: {}", texture);
                continue;
            }

            ResourceLocation dimensionId = ResourceLocation.tryBuild(
                    relative.substring(0, namespaceSeparator), relative.substring(namespaceSeparator + 1));
            if (dimensionId == null) {
                LOGGER.warn("Minigame dimension texture path does not represent a valid dimension id: {}", texture);
                continue;
            }

            LOGGER.info("Found minigame texture for dimension: {}", dimensionId);
            dimensionTextureMap.put(dimensionId, texture);
        }
    }

    public ResourceLocation getMinigameTexture(@Nullable ClientLevel level) {
        if (level != null) {
            ResourceLocation dimensionId = level.dimension().location();
            if (dimensionTextureMap.containsKey(dimensionId)) {
                return dimensionTextureMap.get(dimensionId);
            }
        }
        return dimensionTextureMap.getOrDefault(BuiltinDimensionTypes.OVERWORLD.location(), DEFAULT_TEXTURE);
    }
}
