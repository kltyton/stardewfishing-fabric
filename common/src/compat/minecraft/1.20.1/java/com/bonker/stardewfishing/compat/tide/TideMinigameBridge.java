package com.bonker.stardewfishing.compat.tide;

import com.bonker.stardewfishing.gameplay.minigame.FishingHookLogic;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.server.fishing.FishingHookState;
import com.bonker.stardewfishing.server.fishing.LockableList;
import com.bonker.stardewfishing.server.loot.LegendaryFishSelector;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TideMinigameBridge {
    private TideMinigameBridge() {
    }

    static int getLuck(Object hook) {
        return ((TideFishingHook) hook).getLuck();
    }

    public static boolean startMinigame(ServerPlayer player, HookAccessor hook, List<ItemStack> caughtItems) {
        if (player.fishing != hook) return false;
        TideFishingHook tideHook = HookAccessor.getHook(player);
        if (tideHook == null) return false;

        FishingHookState state = FishingHookState.get(hook);
        if (state.stardewFishing$getEvent() != null) return true;

        LockableList<ItemStack> rewards = state.stardewFishing$getRewards();
        rewards.unlock();
        rewards.clear();
        caughtItems.stream().map(ItemStack::copy).forEach(rewards::add);
        LegendaryFishSelector.replaceFirstFish(rewards, hook, player);
        if (!FishingHookLogic.startStardewMinigame(player)) {
            rewards.clear();
            return false;
        }

        tideHook.setMinigameActive(true);
        player.level().playSound(null, player, SFSoundEvents.FISH_BITE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
