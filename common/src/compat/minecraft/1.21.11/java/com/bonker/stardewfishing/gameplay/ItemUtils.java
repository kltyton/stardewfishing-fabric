package com.bonker.stardewfishing.gameplay;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.items.Bobber;
import com.bonker.stardewfishing.gameplay.items.LegendaryCatch;
import com.bonker.stardewfishing.mixin.FishingBobberEntityAccessor;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.StackReference;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ClickType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Vec3d;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Shared item helpers for rods, bobbers and legendary fish. */
public final class ItemUtils {
    private ItemUtils() {
    }

    public static ItemStack getBobber(ItemStack fishingRod, RegistryWrapper.WrapperLookup registryAccess) {
        return fishingRod.contains(SFComponentTypes.BOBBER)
                ? Objects.requireNonNull(fishingRod.get(SFComponentTypes.BOBBER)).item()
                : ItemStack.EMPTY;
    }

    public static boolean isFishingRod(ItemStack stack) {
        return stack.isIn(StardewFishing.MODIFIABLE_RODS);
    }

    public static boolean isBobber(ItemStack stack) {
        return stack.isIn(StardewFishing.BOBBERS);
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber, RegistryWrapper.WrapperLookup registryAccess) {
        if (bobber.isEmpty()) {
            fishingRod.remove(SFComponentTypes.BOBBER);
        } else {
            fishingRod.set(SFComponentTypes.BOBBER, new Bobber(bobber));
        }
    }

    public static void damageAttachedBobber(ItemStack fishingRod, ServerPlayerEntity player) {
        ItemStack bobber = getBobber(fishingRod, player.getRegistryManager());
        if (bobber.isEmpty()) {
            return;
        }

        tryDamageBobber(bobber, player)
                .ifPresent(b -> setBobber(fishingRod, b, player.getRegistryManager()));
    }

    public static Optional<ItemStack> tryDamageBobber(ItemStack bobber, ServerPlayerEntity player) {
        if (!bobber.isDamageable()) {
            return Optional.empty();
        }

        ItemStack bobberCache = bobber.copy();
        bobber.damage(1, (ServerWorld) player.getEntityWorld(), player, item -> {
            player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK.value(),
                    SoundCategory.PLAYERS);
            Vec3d particlePos = player.getEyePos().add(player.getRotationVec(1.0F));
            ((ServerWorld) player.getEntityWorld()).spawnParticles(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, bobberCache),
                    particlePos.x, particlePos.y, particlePos.z, 8, 0.1, 0.1, 0.1, 0.1);
            player.sendMessage(Text.translatable("stardew_fishing.bobber_broke", bobberCache.getName()), true);
        });
        return Optional.of(bobber);
    }

    public static FishingBobberEntity spawnHook(ServerPlayerEntity player, ItemStack fishingRod, Vec3d pos) {
        FishingBobberEntity hook = new FishingBobberEntity(player, player.getEntityWorld(), 0, 0) {
            @Override
            public void tick() {
                baseTick();
            }
        };
        hook.setPosition(pos);
        player.getEntityWorld().spawnEntity(hook);
        return hook;
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod, RegistryWrapper.WrapperLookup registryAccess) {
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        ItemStack bobber = getBobber(fishingRod, registryAccess);
        if (!bobber.isEmpty()) {
            modifiers.add(bobber);
        }
        return modifiers;
    }

    public static int getLuck(FishingBobberEntity hook) {
        return ((FishingBobberEntityAccessor) hook).getLuckBonus();
    }

    public static boolean isLegendaryFish(ItemStack stack) {
        return stack.isIn(StardewFishing.LEGENDARY_FISH);
    }

    public static void addCatchTooltip(ItemStack stack, List<Text> tooltip) {
        if (!stack.contains(SFComponentTypes.LEGENDARY_CATCH)) {
            return;
        }
        LegendaryCatch data = Objects.requireNonNull(stack.get(SFComponentTypes.LEGENDARY_CATCH));
        String time = DateFormat.getDateTimeInstance().format(new Date(data.time()));

        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.stardew_fishing.legendary_data", data.player(), time)
                .setStyle(StardewFishing.LIGHTER_COLOR));
    }

    public static Hand getRodHand(PlayerEntity player) {
        if (player.getMainHandStack().isIn(StardewFishing.MODIFIABLE_RODS)) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffHandStack().isIn(StardewFishing.MODIFIABLE_RODS)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static boolean handleStackedOnRod(ItemStack fishingRod, ItemStack carried, Slot slot,
                                             ClickType clickType, PlayerEntity player,
                                             StackReference cursorReference) {
        if (!com.bonker.stardewfishing.SFConfig.isInventoryEquippingEnabled()
                || clickType != ClickType.RIGHT || !isFishingRod(fishingRod)) {
            return false;
        }

        ItemStack currentBobber = getBobber(fishingRod, player.getRegistryManager());
        if (isBobber(carried)) {
            if (currentBobber.isEmpty()) {
                setBobber(fishingRod, carried.copyWithCount(1), player.getRegistryManager());
                carried.decrement(1);
                return true;
            }
            if (carried.getCount() == 1) {
                setBobber(fishingRod, carried.copy(), player.getRegistryManager());
                cursorReference.set(currentBobber.copy());
                return true;
            }
        } else if (carried.isEmpty() && !currentBobber.isEmpty()) {
            setBobber(fishingRod, ItemStack.EMPTY, player.getRegistryManager());
            cursorReference.set(currentBobber.copy());
            return true;
        }
        return false;
    }
}
