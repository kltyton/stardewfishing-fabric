package com.bonker.stardewfishing.gameplay.minigame;

import com.bonker.stardewfishing.gameplay.LockableList;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-hook minigame state. NeoForge stores this in an attachment; on Fabric a
 * side-car identity map keyed by the hook entity is used, matching the
 * reference lifecycle (state is created lazily and dropped with the hook).
 */
public final class FishingHookAttachment {
    private static final Map<FishingBobberEntity, FishingHookAttachment> BY_HOOK = new WeakHashMap<>();

    // Lockable list because the canonical contract requires addons to not mutate
    // rewards while the minigame is running.
    private final LockableList<ItemStack> rewards = new LockableList<>();
    private boolean hasTreasureChest = false;
    private boolean hasGoldenChest = false;
    private StardewMinigameStartedEvent event = null;

    public static FishingHookAttachment get(FishingBobberEntity entity) {
        return BY_HOOK.computeIfAbsent(entity, hook -> new FishingHookAttachment());
    }

    public static void remove(FishingBobberEntity entity) {
        BY_HOOK.remove(entity);
    }

    public LockableList<ItemStack> getRewards() {
        return rewards;
    }

    public boolean hasTreasureChest() {
        return hasTreasureChest;
    }

    public void setTreasureChest(boolean hasTreasureChest) {
        this.hasTreasureChest = hasTreasureChest;
    }

    public boolean hasGoldenChest() {
        return hasGoldenChest;
    }

    public void setGoldenChest(boolean hasGoldenChest) {
        this.hasGoldenChest = hasGoldenChest;
    }

    public StardewMinigameStartedEvent getEvent() {
        return event;
    }

    public void setEvent(StardewMinigameStartedEvent event) {
        this.event = event;
    }
}
