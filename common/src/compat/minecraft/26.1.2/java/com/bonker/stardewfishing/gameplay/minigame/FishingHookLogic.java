package com.bonker.stardewfishing.gameplay.minigame;

import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.StardewFishingEvents;
import com.bonker.stardewfishing.common.item.LegendaryCatch;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.server.fishing.LockableList;
import com.bonker.stardewfishing.server.resource.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.fishing.FishingHookAttachment;
import com.bonker.stardewfishing.server.persistence.MinigameDisabledPlayers;
import com.bonker.stardewfishing.api.event.StardewMinigameEndedEvent;
import com.bonker.stardewfishing.api.event.StardewMinigameModifyRewardsEvent;
import com.bonker.stardewfishing.api.event.StardewMinigameStartedEvent;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.world.InteractionHand;
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

public class FishingHookLogic {
    public static boolean startStardewMinigame(ServerPlayer player) {
        if (player.fishing == null || player instanceof FakePlayer ||
                MinigameDisabledPlayers.get(player.level().getServer()).isMinigameDisabled(player)) return false;

        FishingHookAttachment data = FishingHookAttachment.get(player.fishing);

        // A minigame is already in progress
        if (data.getEvent() != null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame while playing one", player.getScoreboardName());
            return true;
        }

        data.getRewards().lock();

        ItemStack fish = data.getRewards().stream()
                .filter(stack -> stack.is(StardewFishing.STARTS_MINIGAME))
                .findFirst()
                .orElseThrow();

        InteractionHand rodHand = FishingItemSupport.getRodHand(player);
        if (rodHand == null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame without a fishing rod", player.getScoreboardName());
            return false;
        }

        BlockPos pos = BlockPos.containing(player.fishing.position());
        FluidState fluid = player.level().getBlockState(pos).getFluidState();
        if (fluid.isEmpty()) {
            fluid = player.level().getBlockState(pos.below()).getFluidState();
        }

        ItemStack fishingRod = player.getItemInHand(rodHand);
        StardewMinigameStartedEvent startEvent = new StardewMinigameStartedEvent(player, player.fishing, fishingRod, fish, FishBehaviorReloadListener.getBehavior(fish), fluid.is(FluidTags.LAVA));

        FishingItemSupport.getAllModifierItems(fishingRod, player.registryAccess()).forEach(stack ->
                StardewFishing.getModifiers(stack)
                        .ifPresent(modifiers -> modifiers.apply(startEvent)));

        data.setEvent(startEvent);
        StardewFishingEvents.MINIGAME_STARTED.invoker().onMinigameStarted(startEvent);

        double chestChance = SFConfig.getTreasureChestChance() + startEvent.getTreasureChanceBonus();
        double goldenChance = SFConfig.getGoldenChestChance() + startEvent.getGoldenChanceBonus();
        if (startEvent.isForcedTreasureChest() || player.getRandom().nextFloat() < chestChance) {
            data.setTreasureChest(true);
            if (startEvent.isForcedGoldenChest() || player.getRandom().nextFloat() < goldenChance) {
                data.setGoldenChest(true);
            }
        }

        ServerPlayNetworking.send(player, new S2CStartMinigamePacket(data));
        return true;
    }

    public static void endMinigame(ServerPlayer player, boolean success, double accuracy, boolean gotChest, int qualityBoost, @Nullable ItemStack fishingRod) {
        if (player.fishing == null) {
            return;
        }

        StardewMinigameEndedEvent endEvent = new StardewMinigameEndedEvent(player, player.fishing, fishingRod, success, accuracy, gotChest);
        if (fishingRod != null) {
            StardewFishingEvents.MINIGAME_ENDED.invoker().onMinigameEnded(endEvent);
        }

        if (endEvent.wasSuccessful() && !player.level().isClientSide()) {
            modifyRewards(player, endEvent.getAccuracy(), qualityBoost);
            giveRewards(player, endEvent.getAccuracy(), endEvent.gotChest(), fishingRod);
        }

        if (player.fishing != null) {
            player.fishing.discard();
        }
    }

    public static void modifyRewards(ServerPlayer player, double accuracy, int qualityBoost) {
        if (player.fishing == null) return;
        modifyRewards(FishingHookAttachment.get(player.fishing).getRewards(), accuracy, qualityBoost);
    }

    public static void modifyRewards(List<ItemStack> rewards, double accuracy, int qualityBoost) {
        // The NeoForge source only forwards this value to its optional Quality Food bridge.
        // That bridge is an empty stub in the 26.1.2 source, and no compatible Fabric API is available.
    }

    public static void giveRewards(ServerPlayer player, double accuracy, boolean gotChest, ItemStack fishingRod) {
        if (player.fishing == null) return;

        FishingHook hook = player.fishing;

        FishingHookAttachment data = FishingHookAttachment.get(hook);
        LockableList<ItemStack> rewards = data.getRewards();
        rewards.unlock();

        if (data.hasTreasureChest() && gotChest) {
            rewards.addAll(getTreasureChestLoot(player.level(), data.hasGoldenChest()));
        }

        StardewMinigameModifyRewardsEvent modifyRewardsEvent = new StardewMinigameModifyRewardsEvent(player, hook, fishingRod, rewards);
        StardewFishingEvents.MODIFY_REWARDS.invoker().modifyRewards(modifyRewardsEvent);

        if (rewards.isEmpty()) {
            hook.discard();
        }

        if (!StardewFishingEvents.ALLOW_REWARD_DELIVERY.invoker().allowRewardDelivery(modifyRewardsEvent)) {
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
            hook.discard();
            return;
        }

        ServerLevel level = player.level();
        InteractionHand hand = FishingItemSupport.getRodHand(player);
        ItemStack handItem = hand != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
        CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, handItem, hook, rewards);

        for (ItemStack reward : rewards) {
            int exp = (int) ((player.getRandom().nextInt(6) + 1) * SFConfig.getMultiplier(accuracy, data.getEvent().getExpMultiplier()));
            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp));

            if (reward.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT);
            }
            if (reward.is(StardewFishing.LEGENDARY_FISH) && !reward.has(SFComponentTypes.LEGENDARY_CATCH)) {
                reward.set(SFComponentTypes.LEGENDARY_CATCH, new LegendaryCatch(player));
            }

            ItemEntity itemEntity;
            if (data.getEvent().isLavaFishing()) {
                itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward) {
                    @Override
                    public boolean displayFireAnimation() {
                        return false;
                    }

                    @Override
                    public void lavaHurt() {
                    }
                };
            } else {
                itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward);
            }
            double scale = 0.1;
            double dx = player.getX() - hook.getX();
            double dy = player.getY() - hook.getY();
            double dz = player.getZ() - hook.getZ();
            itemEntity.setDeltaMovement(dx * scale, dy * scale + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * scale);
            level.addFreshEntity(itemEntity);
        }

        player.level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static List<ItemStack> getTreasureChestLoot(ServerLevel level, boolean isGolden) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(level.dimension() == Level.NETHER ? StardewFishing.TREASURE_CHEST_NETHER_LOOT : StardewFishing.TREASURE_CHEST_LOOT);
        List<ItemStack> items = new ArrayList<>();

        int rolls;
        if (isGolden) {
            rolls = 2; // 100% for at least 2
            if (level.getRandom().nextFloat() < 0.25F) {
                rolls++; // 1 in 4 chance to get 3

                if (level.getRandom().nextFloat() < 0.5F) {
                    rolls++; // 1 in 8 chance to get 4
                }
            }
        } else {
            rolls = 1;
        }

        for (int i = 0; i < rolls; i++) {
            items.addAll(lootTable.getRandomItems((new LootParams.Builder(level)).create(LootContextParamSets.EMPTY)));
        }

        return items;
    }
}
