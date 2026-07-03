package com.kltyton.stardewfishingFabric.common;

import com.kltyton.stardewfishingFabric.common.networking.S2CStartMinigamePacket;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import com.kltyton.stardewfishingFabric.server.FishBehaviorReloadListener;
import koala.fishingreal.FishingReal;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.stat.Stats;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FishingHookLogic {
    public static void startMinigame(ServerPlayerEntity player, FishingBobberEntity hook) {
        List<ItemStack> rolledItems = rollFishingLoot(player, hook);
        Optional<ItemStack> previewItem = firstNonEmpty(rolledItems).map(ItemStack::copy);
        FishBehavior behavior = FishBehaviorReloadListener.getBehavior(previewItem.orElse(null));
        if (behavior == null) {
            behavior = new FishBehavior(25, 8.0F, 0.4F, 0.8F, 50, 35);
        }
        FishingDataStorage.storeData(player, hook, rolledItems);
        SFNetworking.sendToPlayer(player, new S2CStartMinigamePacket(behavior, previewItem));
    }

    private static List<ItemStack> rollFishingLoot(ServerPlayerEntity player, FishingBobberEntity hook) {
        try {
            ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
            ItemStack rodStack = player.getMainHandStack();
            if (rodStack.isEmpty() || !(rodStack.getItem() instanceof net.minecraft.item.FishingRodItem)) {
                ItemStack offHand = player.getOffHandStack();
                if (!offHand.isEmpty() && offHand.getItem() instanceof net.minecraft.item.FishingRodItem) {
                    rodStack = offHand;
                }
            }
            LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld)
                    .add(LootContextParameters.ORIGIN, new Vec3d(hook.getX(), hook.getY(), hook.getZ()))
                    .add(LootContextParameters.TOOL, rodStack)
                    .add(LootContextParameters.THIS_ENTITY, hook)
                    .luck(EnchantmentHelper.getFishingLuckBonus(serverWorld, rodStack, player))
                    .build(LootContextTypes.FISHING);

            var lootTable = serverWorld.getServer().getReloadableRegistries()
                    .getLootTable(LootTables.FISHING_GAMEPLAY);
            List<ItemStack> generated = lootTable.generateLoot(lootWorldContext);
            List<ItemStack> copy = new ArrayList<>();
            for (ItemStack stack : generated) {
                if (!stack.isEmpty()) {
                    copy.add(stack.copy());
                }
            }
            if (!copy.isEmpty()) {
                return copy;
            }
        } catch (RuntimeException e) {
            // Preview is cosmetic; fall back to empty instead of crashing fishing.
            e.printStackTrace();
        }

        return List.of(new ItemStack(Items.COD));
    }

    private static Optional<ItemStack> firstNonEmpty(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    // 结束迷你游戏
    public static boolean endMinigame(ServerPlayerEntity player, boolean success, double accuracy, FishingBobberEntity hook, List<ItemStack> items) {
        if (success && !player.getEntityWorld().isClient()) {
            for (ItemStack stack : items) {
                if (stack.isEmpty()) {
                    continue;
                }

                ItemEntity itemEntity = new ItemEntity(hook.getEntityWorld(), hook.getX(), hook.getY(), hook.getZ(), stack.copy());
                double d = player.getX() - hook.getX();
                double e = player.getY() - hook.getY();
                double f = player.getZ() - hook.getZ();
                itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                if (FabricLoader.getInstance().isModLoaded("fishingreal")) {
                    hook.getEntityWorld().spawnEntity(FishingReal.convertItemEntity(itemEntity, player));
                } else {
                    hook.getEntityWorld().spawnEntity(itemEntity);
                }
                if (FabricLoader.getInstance().isModLoaded("jobsaddon")) {
                    JobHelper.addFisherXp(player, stack);
                }
                if (stack.isIn(ItemTags.FISHES)) {
                    player.increaseStat(Stats.FISH_CAUGHT, 1);
                }
            }
            player.getEntityWorld().spawnEntity(new ExperienceOrbEntity(player.getEntityWorld(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, hook.getRandom().nextInt(6) + 1));
        }
        FishingDataStorage.clearDataForPlayer(player);
        if (hook != null) {
            hook.discard();
        }
        return success;
    }
}
