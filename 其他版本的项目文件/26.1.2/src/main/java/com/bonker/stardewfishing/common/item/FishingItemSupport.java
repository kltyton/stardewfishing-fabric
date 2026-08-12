package com.bonker.stardewfishing.common.item;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.BobberEquipEvents;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FishingItemSupport {
    private FishingItemSupport() {
    }

    public static ItemStack getBobber(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        Bobber bobber = fishingRod.get(SFComponentTypes.BOBBER);
        return bobber == null ? ItemStack.EMPTY : bobber.item();
    }

    public static boolean isFishingRod(ItemStack stack) {
        return stack.is(StardewFishing.MODIFIABLE_RODS);
    }

    public static boolean isBobber(ItemStack stack) {
        return stack.is(StardewFishing.BOBBERS);
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber, HolderLookup.Provider registryAccess) {
        if (bobber.isEmpty()) {
            fishingRod.remove(SFComponentTypes.BOBBER);
        } else {
            fishingRod.set(SFComponentTypes.BOBBER, new Bobber(bobber.copyWithCount(1)));
        }
    }

    public static boolean handleStackedOnRod(ItemStack fishingRod, ItemStack carried, Slot slot,
                                              ClickAction clickAction, Player player, SlotAccess carriedSlotAccess) {
        if (!SFConfig.isInventoryEquippingEnabled()
                || clickAction != ClickAction.SECONDARY
                || !isFishingRod(fishingRod)) {
            return false;
        }

        ItemStack currentBobber = getBobber(fishingRod, player.registryAccess());
        boolean equipped = true;
        boolean handled = false;
        if (isBobber(carried)) {
            if (currentBobber.isEmpty()) {
                setBobber(fishingRod, carried, player.registryAccess());
                carried.shrink(1);
                handled = true;
            } else if (carried.getCount() == 1) {
                setBobber(fishingRod, carried, player.registryAccess());
                carriedSlotAccess.set(currentBobber.copy());
                handled = true;
            } else if (ItemStack.isSameItemSameComponents(carried, currentBobber)) {
                int transferAmount = Math.min(carried.getMaxStackSize() - carried.getCount(), currentBobber.getCount());
                if (transferAmount > 0) {
                    setBobber(fishingRod, currentBobber.copyWithCount(currentBobber.getCount() - transferAmount), player.registryAccess());
                    carried.grow(transferAmount);
                    handled = true;
                    equipped = false;
                }
            }
        } else if (!currentBobber.isEmpty() && carried.isEmpty()) {
            setBobber(fishingRod, ItemStack.EMPTY, player.registryAccess());
            carriedSlotAccess.set(currentBobber.copy());
            handled = true;
            equipped = false;
        }

        if (handled) {
            BobberEquipEvents.CHANGED.invoker().onChanged(slot, equipped);
        }
        return handled;
    }

    public static void damageAttachedBobber(ItemStack fishingRod, ServerPlayer player) {
        ItemStack bobber = getBobber(fishingRod, player.registryAccess());
        if (bobber.isEmpty()) {
            return;
        }
        tryDamageBobber(bobber, player).ifPresent(damaged -> setBobber(fishingRod, damaged, player.registryAccess()));
    }

    public static Optional<ItemStack> tryDamageBobber(ItemStack bobber, ServerPlayer player) {
        if (!bobber.isDamageableItem()) {
            return Optional.empty();
        }

        ItemStack bobberCache = bobber.copy();
        bobber.hurtAndBreak(1, player.level(), player, item -> {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS);
            Vec3 particlePos = player.getEyePosition().add(player.getLookAngle());
            player.level().sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(bobberCache)),
                    particlePos.x(), particlePos.y(), particlePos.z(), 8, 0.1, 0.1, 0.1, 0.1);
            player.sendSystemMessage(Component.translatable("stardew_fishing.bobber_broke", bobberCache.getHoverName()), true);
        });
        return Optional.of(bobber);
    }

    public static FishingHook spawnHook(ServerPlayer player, ItemStack fishingRod, Vec3 pos) {
        FishingHook hook = new FishingHook(player, player.level(), 0, 0) {
            @Override
            public void tick() {
                baseTick();
            }
        };
        hook.setPos(pos);
        player.level().addFreshEntity(hook);
        return hook;
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        ItemStack bobber = getBobber(fishingRod, registryAccess);
        if (!bobber.isEmpty()) {
            modifiers.add(bobber);
        }
        return modifiers;
    }

    public static int getLuck(FishingHook hook) {
        return hook.luck;
    }

    public static boolean isLegendaryFish(ItemStack stack) {
        return stack.is(StardewFishing.LEGENDARY_FISH);
    }

    public static void addCatchTooltip(ItemStack stack, List<Component> tooltip) {
        LegendaryCatch data = stack.get(SFComponentTypes.LEGENDARY_CATCH);
        if (data == null) {
            return;
        }
        String time = DateFormat.getDateTimeInstance().format(new Date(data.time()));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.stardew_fishing.legendary_data", data.player(), time)
                .withStyle(StardewFishing.LIGHTER_COLOR));
    }

    public static InteractionHand getRodHand(Player player) {
        if (isFishingRod(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            return InteractionHand.MAIN_HAND;
        }
        if (isFishingRod(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
