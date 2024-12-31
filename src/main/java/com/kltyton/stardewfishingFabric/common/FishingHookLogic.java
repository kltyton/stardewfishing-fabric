package com.kltyton.stardewfishingFabric.common;

import com.kltyton.stardewfishingFabric.common.networking.S2CStartMinigamePacket;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import com.kltyton.stardewfishingFabric.server.FishBehaviorReloadListener;
import io.netty.buffer.Unpooled;
import koala.fishingreal.FishingReal;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public class FishingHookLogic {
    public static void startMinigame(ServerPlayer player, ItemStack item) {
        if (player.fishing == null) {
            return;
        }
        FishBehavior behavior = FishBehaviorReloadListener.getBehavior(item);
        if (behavior != null) {
            S2CStartMinigamePacket packet = new S2CStartMinigamePacket(behavior);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            packet.encode(buf);
            SFNetworking.sendToPlayer(player, buf);
        }
    }

    // 结束迷你游戏
    public static boolean endMinigame(ServerPlayer player, boolean success, double accuracy, FishingHook hook, ItemStack items) {
        if (success && !player.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), items);
            double d = player.getX() - hook.getX();
            double e = player.getY() - hook.getY();
            double f = player.getZ() - hook.getZ();
            itemEntity.setDeltaMovement(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
            if (FabricLoader.getInstance().isModLoaded("fishingreal")) {
                hook.level().addFreshEntity(FishingReal.convertItemEntity(itemEntity, player));
            } else {
                hook.level().addFreshEntity(itemEntity);
            }
            player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, hook.random.nextInt(6) + 1));
            if (items.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT, 1);
            }
            FishingDataStorage.clearDataForPlayer(player);
        }
        if (player.fishing != null) {
            player.fishing.discard();
        }
        return success;
    }
}
