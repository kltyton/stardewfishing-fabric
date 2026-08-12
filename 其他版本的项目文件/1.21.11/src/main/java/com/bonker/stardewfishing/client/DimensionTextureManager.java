package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DimensionTextureManager extends SinglePreparationResourceReloader<Set<Identifier>>
        implements IdentifiableResourceReloadListener {
    private static final Identifier FALLBACK =
            StardewFishing.identifier("textures/gui/minigame_backgrounds/minecraft/overworld.png");
    private static DimensionTextureManager instance;
    private final Map<Identifier, Identifier> dimensionTextures = new HashMap<>();

    public static DimensionTextureManager getOrCreate() {
        if (instance == null) {
            instance = new DimensionTextureManager();
        }
        return instance;
    }

    @Override
    protected Set<Identifier> prepare(ResourceManager manager, Profiler profiler) {
        return manager.findResources("textures/gui/minigame_backgrounds",
                id -> id.getNamespace().equals(StardewFishing.MODID) && id.getPath().endsWith(".png")).keySet();
    }

    @Override
    protected void apply(Set<Identifier> candidates, ResourceManager manager, Profiler profiler) {
        dimensionTextures.clear();
        for (Identifier texture : candidates) {
            String base = texture.getPath().substring(0, texture.getPath().length() - 4);
            String[] parts = base.split("/");
            if (parts.length != 5) {
                StardewFishing.LOGGER.warn("Invalid minigame dimension texture: {}", texture);
                continue;
            }
            Identifier dimensionId = Identifier.tryParse(parts[3] + ":" + parts[4]);
            if (dimensionId != null) {
                dimensionTextures.put(dimensionId, texture);
            }
        }
    }

    public Identifier getMinigameTexture(@Nullable ClientWorld world) {
        return world == null ? FALLBACK
                : dimensionTextures.getOrDefault(world.getRegistryKey().getValue(), FALLBACK);
    }

    @Override
    public Identifier getFabricId() {
        return StardewFishing.identifier("dimension_texture_manager");
    }
}
