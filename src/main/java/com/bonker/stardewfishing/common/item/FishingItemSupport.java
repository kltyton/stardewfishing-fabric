package com.bonker.stardewfishing.common.item;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.api.event.BobberEquipEvents;
import com.bonker.stardewfishing.compat.tide.TideCompat;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.mixin.FishingHookAccessor;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class FishingItemSupport {
    private static final String MODIFIER_KEY = "modifier";
    private static final String BOBBER_KEY = "bobber";
    private static final String LEGENDARY_CATCH_KEY = "legendary_catch";

    private FishingItemSupport() {
    }

    public static ItemStack getBobber(ItemStack fishingRod) {
        if (TideCompat.isRod(fishingRod)) return TideCompat.getBobber(fishingRod);
        if (!fishingRod.hasTag()) return ItemStack.EMPTY;
        CompoundTag root = fishingRod.getTag();
        if (root == null || !root.contains(MODIFIER_KEY, Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
        CompoundTag modifier = root.getCompound(MODIFIER_KEY);
        return modifier.contains(BOBBER_KEY, Tag.TAG_COMPOUND)
                ? ItemStack.of(modifier.getCompound(BOBBER_KEY)) : ItemStack.EMPTY;
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber) {
        if (TideCompat.setBobber(fishingRod, bobber)) return;
        CompoundTag root = fishingRod.getOrCreateTag();
        CompoundTag modifier = root.contains(MODIFIER_KEY, Tag.TAG_COMPOUND)
                ? root.getCompound(MODIFIER_KEY) : new CompoundTag();
        if (bobber.isEmpty()) modifier.remove(BOBBER_KEY);
        else modifier.put(BOBBER_KEY, bobber.copyWithCount(1).save(new CompoundTag()));
        if (modifier.isEmpty()) root.remove(MODIFIER_KEY);
        else root.put(MODIFIER_KEY, modifier);
        if (root.isEmpty()) fishingRod.setTag(null);
    }

    public static boolean isFishingRod(ItemStack stack) {
        return TideCompat.isRod(stack) || stack.is(StardewFishing.MODIFIABLE_RODS)
                || stack.getItem() instanceof FishingRodItem;
    }

    public static boolean isBobber(ItemStack stack) {
        return stack.is(StardewFishing.BOBBERS) || TideCompat.isBobber(stack);
    }

    public static boolean handleStackedOnRod(ItemStack rod, ItemStack carried, Slot slot, ClickAction clickAction,
                                             Player player, SlotAccess carriedSlotAccess) {
        if (!SFConfig.isInventoryEquippingEnabled() || clickAction != ClickAction.SECONDARY || !isFishingRod(rod)) {
            return false;
        }

        ItemStack current = getBobber(rod);
        boolean equipped = true;
        boolean handled = false;
        if (isBobber(carried)) {
            if (current.isEmpty()) {
                setBobber(rod, carried);
                carried.shrink(1);
                handled = true;
            } else if (carried.getCount() == 1) {
                setBobber(rod, carried);
                carriedSlotAccess.set(current.copy());
                handled = true;
            } else if (ItemStack.isSameItemSameTags(carried, current)) {
                int transfer = Math.min(carried.getMaxStackSize() - carried.getCount(), current.getCount());
                if (transfer > 0) {
                    carried.grow(transfer);
                    setBobber(rod, current.copyWithCount(current.getCount() - transfer));
                    equipped = false;
                    handled = true;
                }
            }
        } else if (!current.isEmpty() && carried.isEmpty()) {
            setBobber(rod, ItemStack.EMPTY);
            carriedSlotAccess.set(current.copy());
            equipped = false;
            handled = true;
        }

        if (handled) {
            BobberEquipEvents.CHANGED.invoker().onChanged(slot, equipped);
            player.playSound(equipped ? SoundEvents.ARMOR_EQUIP_LEATHER : SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.8F, 1.0F);
        }
        return handled;
    }

    public static void damageAttachedBobber(ItemStack fishingRod, ServerPlayer player) {
        ItemStack bobber = getBobber(fishingRod);
        if (bobber.isEmpty() || !bobber.isDamageableItem()) return;
        ItemStack particleStack = bobber.copy();
        bobber.hurtAndBreak(1, player, ignored -> {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS);
            Vec3 particlePos = player.getEyePosition().add(player.getLookAngle());
            player.serverLevel().sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    particlePos.x, particlePos.y, particlePos.z, 8, 0.1, 0.1, 0.1, 0.1);
            player.displayClientMessage(Component.translatable("stardew_fishing.bobber_broke", particleStack.getHoverName()), true);
        });
        setBobber(fishingRod, bobber);
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

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod) {
        if (TideCompat.isRod(fishingRod)) return TideCompat.getModifierItems(fishingRod);
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        ItemStack bobber = getBobber(fishingRod);
        if (!bobber.isEmpty()) modifiers.add(bobber);
        return modifiers;
    }

    public static int getLuck(FishingHook hook) {
        Integer tideLuck = TideCompat.getHookLuck(hook);
        if (tideLuck != null) return tideLuck;
        return ((FishingHookAccessor) hook).stardewFishing$getLuck();
    }

    public static boolean isLegendaryFish(ItemStack stack) {
        return stack.is(StardewFishing.LEGENDARY_FISH);
    }

    public static void recordLegendaryCatch(ItemStack stack, Player player) {
        CompoundTag data = new CompoundTag();
        data.putString("player", player.getScoreboardName());
        data.putLong("time", System.currentTimeMillis());
        stack.getOrCreateTag().put(LEGENDARY_CATCH_KEY, data);
    }

    public static void addCatchTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(LEGENDARY_CATCH_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag data = root.getCompound(LEGENDARY_CATCH_KEY);
        String time = DateFormat.getDateTimeInstance().format(new Date(data.getLong("time")));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.stardew_fishing.legendary_data", data.getString("player"), time)
                .withStyle(StardewFishing.LIGHTER_COLOR));
    }

    public static @Nullable InteractionHand getRodHand(Player player) {
        if (isFishingRod(player.getItemInHand(InteractionHand.MAIN_HAND))) return InteractionHand.MAIN_HAND;
        if (isFishingRod(player.getItemInHand(InteractionHand.OFF_HAND))) return InteractionHand.OFF_HAND;
        return null;
    }
}
