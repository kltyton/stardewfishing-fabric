package com.bonker.stardewfishing.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OptionalModMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, String> REQUIRED_MODS = Map.of(
            "com.bonker.stardewfishing.mixin.LavaFishingBobberEntityMixin", "netherdepthsupgrade",
            "com.bonker.stardewfishing.mixin.PokeRodFishingBobberEntityMixin", "cobblemon"
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String requiredMod = REQUIRED_MODS.get(mixinClassName);
        return requiredMod == null || FabricLoader.getInstance().isModLoaded(requiredMod);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
