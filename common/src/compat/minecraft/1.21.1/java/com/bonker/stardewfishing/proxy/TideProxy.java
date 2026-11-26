package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import com.li64.tide.data.TideTags;
import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TideProxy {
    public static boolean startMinigame(ServerPlayer player, HookAccessor hook, List<ItemStack> caughtItems) {
        if (player.fishing != hook) return false;
        TideFishingHook tideHook = HookAccessor.getHook(player);
        if (tideHook == null) return false;

        FishingHookAttachment data = FishingHookAttachment.get(hook);
        if (data.getEvent() != null) return true;

        var rewards = data.getRewards();
        rewards.unlock();
        rewards.clear();
        caughtItems.stream().map(ItemStack::copy).forEach(rewards::add);
        if (!FishingHookLogic.startStardewMinigame(player)) {
            rewards.clear();
            return false;
        }

        tideHook.setMinigameActive(true);
        player.level().playSound(null, player, SFSoundEvents.FISH_BITE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static void damageEquippedBobber(ItemStack fishingRod, ServerPlayer player) {
        if (!CustomRodManager.hasBobber(fishingRod)) {
            return;
        }
        ItemUtils.tryDamageBobber(getBobber(fishingRod, player.registryAccess()), player)
                .ifPresent(b -> CustomRodManager.setBobber(fishingRod, b));
    }

    public static ItemStack getBobber(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        return CustomRodManager.hasBobber(fishingRod) ? CustomRodManager.getBobber(fishingRod) : ItemStack.EMPTY;
    }

    public static boolean isTideRod(ItemStack fishingRod) {
        return fishingRod.getItem() instanceof TideFishingRodItem;
    }

    public static boolean isTideBobber(ItemStack stack) {
        return stack.is(TideTags.Items.BOBBERS);
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber, HolderLookup.Provider registryAccess) {
        CustomRodManager.setBobber(fishingRod, bobber);
    }

    public static FishingHook spawnHook(Player player, ItemStack fishingRod, Vec3 pos) {
        TideFishingHook hook = new TideFishingHook(TideEntityTypes.FISHING_BOBBER, player, player.level(), 0, 0, 1, fishingRod) {
            @Override
            public void tick() {
                baseTick();
            }
        };
        player.level().addFreshEntity(hook);
        hook.setPos(pos);
        HookAccessor accessor = new HookAccessor(hook, player.level());
        player.level().addFreshEntity(accessor);
        return accessor;
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        if (CustomRodManager.hasBobber(fishingRod)) {
            modifiers.add(CustomRodManager.getBobber(fishingRod));
        }
        if (CustomRodManager.hasHook(fishingRod)) {
            modifiers.add(CustomRodManager.getHook(fishingRod));
        }
        if (CustomRodManager.hasLine(fishingRod)) {
            modifiers.add(CustomRodManager.getLine(fishingRod));
        }
        return modifiers;
    }

    public static boolean isTideHookEntity(FishingHook hook) {
        return hook instanceof HookAccessor;
    }

    public static int getLuck(FishingHook hook) {
        if (hook instanceof HookAccessor accessor) {
            Player owner = accessor.getPlayerOwner();
            return owner == null ? 0 : HookAccessor.getHook(owner).getLuck();
        }
        return 0;
    }
}
