package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.registry.SFComponentTypes;
import com.bonker.stardewfishing.common.item.LegendaryCatch;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CommonEvents {
    private CommonEvents() {
    }

    /**
     * Marks legendary fish rewards with the catching player and time, mirroring the NeoForge
     * {@code ItemFishedEvent} handler. Called from the fishing hook retrieve hook and reward giving.
     */
    public static void markLegendaryCatches(FishingHook hook, List<ItemStack> rewards) {
        for (ItemStack stack : rewards) {
            if (FishingItemSupport.isLegendaryFish(stack)) {
                stack.set(SFComponentTypes.LEGENDARY_CATCH, new LegendaryCatch(hook.getPlayerOwner()));
            }
        }
    }

    /**
     * Drops the attached bobber when a fishing rod breaks, mirroring the NeoForge
     * {@code PlayerDestroyItemEvent} handler.
     */
    public static void onPlayerDestroyItem(ServerPlayer player, ItemStack original) {
        if (FishingItemSupport.isFishingRod(original)) {
            ItemStack bobber = FishingItemSupport.getBobber(original, player.registryAccess());
            if (!bobber.isEmpty()) {
                player.spawnAtLocation((ServerLevel) player.level(), bobber);
            }
        }
    }
}
