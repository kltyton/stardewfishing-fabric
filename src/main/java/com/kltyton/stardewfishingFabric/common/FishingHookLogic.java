package com.kltyton.stardewfishingFabric.common;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.api.event.StardewFishingEvents;
import com.kltyton.stardewfishingFabric.api.event.StardewMinigameEndedEvent;
import com.kltyton.stardewfishingFabric.api.event.StardewMinigameModifyRewardsEvent;
import com.kltyton.stardewfishingFabric.api.event.StardewMinigameStartedEvent;
import com.kltyton.stardewfishingFabric.common.config.SFConfig;
import com.kltyton.stardewfishingFabric.common.item.FishingItemSupport;
import com.kltyton.stardewfishingFabric.common.networking.S2CStartMinigamePacket;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import com.kltyton.stardewfishingFabric.compat.FishingRealCompat;
import com.kltyton.stardewfishingFabric.compat.JobsAddonCompat;
import com.kltyton.stardewfishingFabric.registry.SFSoundEvents;
import com.kltyton.stardewfishingFabric.server.FishBehaviorReloadListener;
import com.kltyton.stardewfishingFabric.server.fishing.FishingHookState;
import com.kltyton.stardewfishingFabric.server.fishing.LockableList;
import com.kltyton.stardewfishingFabric.server.persistence.MinigameDisabledPlayers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FishingHookLogic {
    private FishingHookLogic() {
    }

    /** Legacy Tide compatibility entry point. */
    public static void startMinigame(ServerPlayer player, ItemStack item) {
        if (player.fishing == null) return;
        LockableList<ItemStack> rewards = FishingHookState.get(player.fishing).stardewFishing$getRewards();
        if (rewards.isEmpty()) rewards.add(item.copy());
        startStardewMinigame(player);
    }

    public static Optional<ArrayList<ItemStack>> getStoredRewards(FishingHook hook) {
        LockableList<ItemStack> rewards = FishingHookState.get(hook).stardewFishing$getRewards();
        return Optional.of(rewards);
    }

    public static boolean startStardewMinigame(ServerPlayer player) {
        FishingHook hook = player.fishing;
        if (hook == null || MinigameDisabledPlayers.get(player.server).isMinigameDisabled(player)) return false;

        FishingHookState state = FishingHookState.get(hook);
        if (state.stardewFishing$getEvent() != null) {
            StardewfishingFabric.LOGGER.warn("{} tried to start a second minigame", player.getScoreboardName());
            return true;
        }

        LockableList<ItemStack> rewards = state.stardewFishing$getRewards();
        ItemStack fish = rewards.stream().filter(stack -> stack.is(StardewfishingFabric.STARTS_MINIGAME))
                .findFirst().orElse(null);
        if (fish == null) return false;

        InteractionHand hand = FishingItemSupport.getRodHand(player);
        if (hand == null) {
            StardewfishingFabric.LOGGER.warn("{} tried to start a minigame without a fishing rod", player.getScoreboardName());
            return false;
        }

        FishBehavior behavior = FishBehaviorReloadListener.getBehavior(fish);
        if (behavior == null) {
            StardewfishingFabric.LOGGER.error("No fish behavior is available for {}", fish.getItem());
            return false;
        }

        rewards.lock();
        BlockPos pos = hook.blockPosition();
        FluidState fluid = player.level().getBlockState(pos).getFluidState();
        if (fluid.isEmpty()) fluid = player.level().getBlockState(pos.below()).getFluidState();

        ItemStack rod = player.getItemInHand(hand);
        StardewMinigameStartedEvent event = new StardewMinigameStartedEvent(
                player, hook, rod, fish, behavior, fluid.is(FluidTags.LAVA));
        FishingItemSupport.getAllModifierItems(rod).forEach(modifier ->
                StardewfishingFabric.getModifiers(modifier).ifPresent(value -> value.apply(event)));
        state.stardewFishing$setEvent(event);
        StardewFishingEvents.MINIGAME_STARTED.invoker().onMinigameStarted(event);

        double chestChance = SFConfig.getTreasureChestChance() + event.getTreasureChanceBonus();
        double goldenChance = SFConfig.getGoldenChestChance() + event.getGoldenChanceBonus();
        if (event.isForcedTreasureChest() || player.getRandom().nextDouble() < chestChance) {
            state.stardewFishing$setTreasureChest(true);
            if (event.isForcedGoldenChest() || player.getRandom().nextDouble() < goldenChance) {
                state.stardewFishing$setGoldenChest(true);
            }
        }

        SFNetworking.sendToPlayer(player, new S2CStartMinigamePacket(state));
        return true;
    }

    public static void endMinigame(ServerPlayer player, boolean success, double accuracy, boolean gotChest,
                                   int qualityBoost, @Nullable ItemStack fishingRod) {
        FishingHook hook = player.fishing;
        if (hook == null) return;

        StardewMinigameEndedEvent event = new StardewMinigameEndedEvent(player, hook, fishingRod, success, accuracy, gotChest);
        if (fishingRod != null) StardewFishingEvents.MINIGAME_ENDED.invoker().onMinigameEnded(event);
        if (event.wasSuccessful()) {
            modifyRewards(player, event.getAccuracy(), qualityBoost);
            giveRewards(player, event.getAccuracy(), event.gotChest(), fishingRod);
        }
        if (player.fishing != null) player.fishing.discard();
    }

    /** Legacy completion signature retained for older integrations. */
    public static boolean endMinigame(ServerPlayer player, boolean success, double accuracy,
                                      FishingHook hook, ItemStack items) {
        if (player.fishing == null && hook != null) player.fishing = hook;
        endMinigame(player, success, accuracy, false, 0, FishingItemSupport.getRodHand(player) == null
                ? null : player.getItemInHand(FishingItemSupport.getRodHand(player)));
        return success;
    }

    @Deprecated(forRemoval = true, since = "3.7")
    public static void modifyRewards(ServerPlayer player, double accuracy, @Nullable ItemStack fishingRod) {
        modifyRewards(player, accuracy, 0);
    }

    @Deprecated(forRemoval = true, since = "3.7")
    public static void modifyRewards(List<ItemStack> rewards, double accuracy, @Nullable ItemStack fishingRod) {
        modifyRewards(rewards, accuracy, 0);
    }

    public static void modifyRewards(ServerPlayer player, double accuracy, int qualityBoost) {
        if (player.fishing != null) modifyRewards(FishingHookState.get(player.fishing).stardewFishing$getRewards(), accuracy, qualityBoost);
    }

    public static void modifyRewards(List<ItemStack> rewards, double accuracy, int qualityBoost) {
        // Quality Food has no Fabric 1.20.1 release. The quality tier remains available to API listeners.
    }

    public static void giveRewards(ServerPlayer player, double accuracy, boolean gotChest, @Nullable ItemStack fishingRod) {
        FishingHook hook = player.fishing;
        if (hook == null) return;
        FishingHookState state = FishingHookState.get(hook);
        LockableList<ItemStack> rewards = state.stardewFishing$getRewards();
        rewards.unlock();
        if (state.stardewFishing$hasTreasureChest() && gotChest) {
            rewards.addAll(getTreasureChestLoot(player.serverLevel(), state.stardewFishing$hasGoldenChest()));
        }

        StardewMinigameModifyRewardsEvent modifyEvent = new StardewMinigameModifyRewardsEvent(player, hook, fishingRod, rewards);
        StardewFishingEvents.MODIFY_REWARDS.invoker().modifyRewards(modifyEvent);
        if (!StardewFishingEvents.ALLOW_REWARD_DELIVERY.invoker().allowRewardDelivery(modifyEvent)) {
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        InteractionHand hand = FishingItemSupport.getRodHand(player);
        ItemStack handItem = hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, handItem, hook, rewards);
        double expMultiplier = state.stardewFishing$getEvent() == null ? 1.0 : state.stardewFishing$getEvent().getExpMultiplier();

        for (ItemStack reward : rewards) {
            if (reward.isEmpty()) continue;
            if (reward.is(ItemTags.FISHES)) player.awardStat(Stats.FISH_CAUGHT, 1);
            if (FishingItemSupport.isLegendaryFish(reward)) {
                CompoundCatch.recordIfAbsent(reward, player);
            }

            ItemEntity itemEntity = createRewardEntity(player.serverLevel(), hook, reward,
                    state.stardewFishing$getEvent() != null && state.stardewFishing$getEvent().isLavaFishing());
            double dx = player.getX() - hook.getX();
            double dy = player.getY() - hook.getY();
            double dz = player.getZ() - hook.getZ();
            itemEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);

            Entity delivered = FabricLoader.getInstance().isModLoaded("fishingreal")
                    ? FishingRealCompat.convert(itemEntity, player) : itemEntity;
            player.serverLevel().addFreshEntity(delivered);
            if (FabricLoader.getInstance().isModLoaded("jobsaddon")) JobsAddonCompat.awardFishingExperience(player, reward);

            int experience = (int) ((player.getRandom().nextInt(6) + 1) * SFConfig.getMultiplier(accuracy, expMultiplier));
            player.serverLevel().addFreshEntity(new ExperienceOrb(player.serverLevel(), player.getX(),
                    player.getY() + 0.5, player.getZ() + 0.5, experience));
        }
        player.level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static ItemEntity createRewardEntity(ServerLevel level, FishingHook hook, ItemStack reward, boolean lavaFishing) {
        if (!lavaFishing) return new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward);
        return new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward) {
            @Override public boolean displayFireAnimation() { return false; }
            @Override public void lavaHurt() { }
        };
    }

    private static List<ItemStack> getTreasureChestLoot(ServerLevel level, boolean golden) {
        LootTable table = level.getServer().getLootData().getLootTable(level.dimension() == Level.NETHER
                ? StardewfishingFabric.TREASURE_CHEST_NETHER_LOOT : StardewfishingFabric.TREASURE_CHEST_LOOT);
        List<ItemStack> items = new ArrayList<>();
        int rolls = golden ? 2 : 1;
        if (golden && level.random.nextFloat() < 0.25F) {
            rolls++;
            if (level.random.nextFloat() < 0.5F) rolls++;
        }
        for (int i = 0; i < rolls; i++) {
            items.addAll(table.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY)));
        }
        return items;
    }

    private static final class CompoundCatch {
        private static void recordIfAbsent(ItemStack reward, ServerPlayer player) {
            if (!reward.hasTag() || !reward.getOrCreateTag().contains("legendary_catch")) {
                FishingItemSupport.recordLegendaryCatch(reward, player);
            }
        }
    }
}
