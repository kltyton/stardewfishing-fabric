package com.bonker.stardewfishing.gameplay;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.data.reload.FishBehaviorReloadListener;
import com.bonker.stardewfishing.data.reload.MinigameDisabledPlayers;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookAttachment;
import com.bonker.stardewfishing.gameplay.minigame.StardewMinigameEndedEvent;
import com.bonker.stardewfishing.gameplay.minigame.StardewMinigameEvents;
import com.bonker.stardewfishing.gameplay.minigame.StardewMinigameModifyRewardsEvent;
import com.bonker.stardewfishing.gameplay.minigame.StardewMinigameStartedEvent;
import com.bonker.stardewfishing.network.SFNetworking;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.registry.SFItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Server-side minigame lifecycle: start, end, reward modification and reward giving. */
public final class FishingHookLogic {
    private FishingHookLogic() {
    }

    public static boolean startStardewMinigame(ServerPlayerEntity player) {
        if (player.fishHook == null
                || MinigameDisabledPlayers.get(player.getEntityWorld().getServer()).isMinigameDisabled(player)) {
            return false;
        }

        FishingHookAttachment data = FishingHookAttachment.get(player.fishHook);

        // A minigame is already in progress.
        if (data.getEvent() != null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame while playing one", player.getNameForScoreboard());
            return true;
        }

        data.getRewards().lock();

        ItemStack fish = data.getRewards().stream()
                .filter(stack -> stack.isIn(StardewFishing.STARTS_MINIGAME))
                .findFirst()
                .orElseThrow();

        Hand rodHand = ItemUtils.getRodHand(player);
        if (rodHand == null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame without a fishing rod", player.getNameForScoreboard());
            return false;
        }

        BlockPos pos = BlockPos.ofFloored(player.fishHook.getEntityPos());
        var fluid = player.getEntityWorld().getFluidState(pos);
        if (fluid.isEmpty()) {
            fluid = player.getEntityWorld().getFluidState(pos.down());
        }

        ItemStack fishingRod = player.getStackInHand(rodHand);
        StardewMinigameStartedEvent startEvent = new StardewMinigameStartedEvent(
                player, player.fishHook, fishingRod, fish,
                FishBehaviorReloadListener.getBehavior(fish), fluid.isIn(FluidTags.LAVA));

        ItemUtils.getAllModifierItems(fishingRod, player.getRegistryManager()).forEach(stack ->
                StardewFishing.getModifiers(stack)
                        .ifPresent(modifiers -> modifiers.apply(startEvent)));

        data.setEvent(startEvent);
        StardewMinigameEvents.STARTED.invoker().onStart(startEvent);

        double chestChance = SFConfig.getTreasureChestChance() + startEvent.getTreasureChanceBonus();
        double goldenChance = SFConfig.getGoldenChestChance() + startEvent.getGoldenChanceBonus();
        if (startEvent.isForcedTreasureChest() || player.getRandom().nextFloat() < chestChance) {
            data.setTreasureChest(true);
            if (startEvent.isForcedGoldenChest() || player.getRandom().nextFloat() < goldenChance) {
                data.setGoldenChest(true);
            }
        }

        SFNetworking.sendToPlayer(player, new SFNetworking.S2CStartMinigamePayload(data));
        return true;
    }

    public static void endMinigame(ServerPlayerEntity player, boolean success, double accuracy, boolean gotChest,
                                   int qualityBoost, @Nullable ItemStack fishingRod) {
        if (player.fishHook == null) {
            return;
        }

        StardewMinigameEndedEvent endEvent = new StardewMinigameEndedEvent(
                player, player.fishHook, fishingRod, success, accuracy, gotChest);
        if (fishingRod != null) {
            StardewMinigameEvents.ENDED.invoker().onEnd(endEvent);
        }

        if (endEvent.wasSuccessful() && !player.getEntityWorld().isClient()) {
            modifyRewards(player, endEvent.getAccuracy(), qualityBoost);
            giveRewards(player, endEvent.getAccuracy(), endEvent.gotChest(), fishingRod);
        }

        if (player.fishHook != null) {
            player.fishHook.discard();
        }
    }

    public static void modifyRewards(ServerPlayerEntity player, double accuracy, int qualityBoost) {
        if (player.fishHook == null) return;
        modifyRewards(FishingHookAttachment.get(player.fishHook).getRewards(), accuracy, qualityBoost);
    }

    /**
     * Quality-Food integration. The canonical NeoForge reference keeps the call
     * site but its quality application is deliberately inactive; the Fabric port
     * preserves that observable no-op.
     */
    public static void modifyRewards(List<ItemStack> rewards, double accuracy, int qualityBoost) {
        // Intentionally inactive: QualityFood integration is not activated.
    }

    public static void giveRewards(ServerPlayerEntity player, double accuracy, boolean gotChest, ItemStack fishingRod) {
        if (player.fishHook == null) return;

        FishingBobberEntity hook = player.fishHook;

        FishingHookAttachment data = FishingHookAttachment.get(hook);
        LockableList<ItemStack> rewards = data.getRewards();
        rewards.unlock();

        if (data.hasTreasureChest() && gotChest) {
            rewards.addAll(getTreasureChestLoot(player.getEntityWorld(), data.hasGoldenChest()));
        }

        StardewMinigameModifyRewardsEvent modifyRewardsEvent =
                new StardewMinigameModifyRewardsEvent(player, hook, fishingRod, rewards);
        StardewMinigameEvents.MODIFY_REWARDS.invoker().onModifyRewards(modifyRewardsEvent);

        if (rewards.isEmpty()) {
            hook.discard();
        }

        player.getEntityWorld().playSound(null, player.getBlockPos(), SFSoundEvents.PULL_ITEM, SoundCategory.PLAYERS, 1.0F, 1.0F);

        ServerWorld level = player.getEntityWorld();
        for (ItemStack reward : rewards) {
            Hand hand = ItemUtils.getRodHand(player);
            ItemStack handItem = hand != null ? player.getStackInHand(hand) : ItemStack.EMPTY;
            Criteria.FISHING_ROD_HOOKED.trigger(player, handItem, hook, rewards);

            int exp = (int) ((player.getRandom().nextInt(6) + 1)
                    * SFConfig.getMultiplier(accuracy, data.getEvent().getExpMultiplier()));
            level.spawnEntity(new ExperienceOrbEntity(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp));

            if (StardewFishing.COBBLEMON_INSTALLED && reward.isOf(SFItems.POKEMON_PLACEHOLDER)) {
                // The canonical Cobblemon spawn integration is commented out in the
                // reference; the placeholder is consumed without spawning anything.
            } else {
                if (reward.isIn(ItemTags.FISHES)) {
                    player.increaseStat(Stats.FISH_CAUGHT, 1);
                }

                ItemEntity itemEntity;
                if (data.getEvent().isLavaFishing()) {
                    itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward) {
                        @Override
                        public boolean isFireImmune() {
                            return true;
                        }
                    };
                } else {
                    itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward);
                }
                double scale = 0.1;
                double dx = player.getX() - hook.getX();
                double dy = player.getY() - hook.getY();
                double dz = player.getZ() - hook.getZ();
                itemEntity.setVelocity(dx * scale, dy * scale + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * scale);
                level.spawnEntity(itemEntity);
            }
        }

        player.getEntityWorld().playSound(null, player.getBlockPos(), SFSoundEvents.PULL_ITEM, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static List<ItemStack> getTreasureChestLoot(ServerWorld level, boolean isGolden) {
        LootTable lootTable = level.getServer().getReloadableRegistries().getLootTable(
                level.getRegistryKey() == World.NETHER ? StardewFishing.TREASURE_CHEST_NETHER_LOOT : StardewFishing.TREASURE_CHEST_LOOT);
        List<ItemStack> items = new ArrayList<>();

        int rolls;
        if (isGolden) {
            rolls = 2; // 100% for at least 2
            if (level.random.nextFloat() < 0.25F) {
                rolls++; // 1 in 4 chance to get 3

                if (level.random.nextFloat() < 0.5F) {
                    rolls++; // 1 in 8 chance to get 4
                }
            }
        } else {
            rolls = 1;
        }

        for (int i = 0; i < rolls; i++) {
            items.addAll(lootTable.generateLoot(new LootWorldContext.Builder(level).build(LootContextTypes.EMPTY)));
        }

        return items;
    }
}
