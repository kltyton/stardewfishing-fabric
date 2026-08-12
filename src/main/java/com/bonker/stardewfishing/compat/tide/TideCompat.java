package com.bonker.stardewfishing.compat.tide;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.mixin.FishingHookAccessor;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookLogic;
import com.bonker.stardewfishing.server.fishing.FishingHookState;
import com.bonker.stardewfishing.server.fishing.LockableList;
import com.bonker.stardewfishing.server.loot.LegendaryFishSelector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts Tide's published 1.20.1 integration ABI to the new hook-owned state model.
 */
public final class TideCompat {
    private static final String TIDE_ID = "tide";
    private static final TagKey<net.minecraft.world.item.Item> BOBBERS = TagKey.create(
            Registries.ITEM, StardewFishing.resource(TIDE_ID, "bobbers"));
    private static final Map<ResourceLocation, ResourceLocation> ITEM_ALIASES = Map.ofEntries(
            alias("apple_fishing_bobber", "apple_bobber"),
            alias("golden_apple_fishing_bobber", "golden_apple_bobber"),
            alias("enchanted_golden_apple_fishing_bobber", "enchanted_golden_apple_bobber"),
            alias("iron_fishing_bobber", "iron_bobber"),
            alias("golden_fishing_bobber", "golden_bobber"),
            alias("diamond_fishing_bobber", "diamond_bobber"),
            alias("netherite_fishing_bobber", "netherite_bobber"),
            alias("amethyst_fishing_bobber", "amethyst_bobber"),
            alias("echo_fishing_bobber", "echo_bobber"),
            alias("chorus_fishing_bobber", "chorus_bobber"),
            alias("feather_fishing_bobber", "feather_bobber"),
            alias("lichen_fishing_bobber", "lichen_bobber"),
            alias("nautilus_fishing_bobber", "nautilus_bobber"),
            alias("pearl_fishing_bobber", "pearl_bobber"),
            alias("heart_fishing_bobber", "heart_bobber"),
            alias("grassy_fishing_bobber", "grassy_bobber"),
            alias("iron_fishing_hook", "iron_line"),
            alias("fortune_line", "golden_line")
    );
    private static boolean reflectionFailureReported;

    private TideCompat() {
    }

    public static boolean isRod(ItemStack stack) {
        return Api.ROD_CLASS != null && Api.ROD_CLASS.isInstance(stack.getItem());
    }

    public static boolean isBobber(ItemStack stack) {
        return FabricLoader.getInstance().isModLoaded(TIDE_ID) && stack.is(BOBBERS);
    }

    public static Item resolveRenamedItem(ResourceLocation id) {
        Item direct = BuiltInRegistries.ITEM.get(id);
        if (direct != Items.AIR) return direct;
        ResourceLocation alias = ITEM_ALIASES.get(id);
        return alias == null ? Items.AIR : BuiltInRegistries.ITEM.get(alias);
    }

    public static ItemStack getBobber(ItemStack rod) {
        if (!isRod(rod) || Api.HAS_BOBBER == null || Api.GET_BOBBER == null) return ItemStack.EMPTY;
        try {
            if (!(boolean) Api.HAS_BOBBER.invoke(null, rod)) return ItemStack.EMPTY;
            return ((ItemStack) Api.GET_BOBBER.invoke(null, rod)).copy();
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            reportReflectionFailure(exception);
            return ItemStack.EMPTY;
        }
    }

    public static boolean setBobber(ItemStack rod, ItemStack bobber) {
        if (!isRod(rod) || Api.SET_BOBBER == null) return false;
        try {
            Api.SET_BOBBER.invoke(null, rod, bobber.copyWithCount(1));
            return true;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            reportReflectionFailure(exception);
            return false;
        }
    }

    public static List<ItemStack> getModifierItems(ItemStack rod) {
        List<ItemStack> result = new ArrayList<>();
        result.add(rod);
        if (!isRod(rod) || Api.GET_ACCESSORY_LIST == null) return result;
        try {
            Object value = Api.GET_ACCESSORY_LIST.invoke(null, rod);
            if (value instanceof List<?> accessories) {
                for (Object accessory : accessories) {
                    if (accessory instanceof ItemStack stack && !stack.isEmpty()) result.add(stack);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            reportReflectionFailure(exception);
        }
        return result;
    }

    public static Integer getHookLuck(FishingHook hook) {
        if (Api.HOOK_ACCESSOR_CLASS == null || Api.GET_HOOK == null || !Api.HOOK_ACCESSOR_CLASS.isInstance(hook)) {
            return null;
        }
        Player owner = hook.getPlayerOwner();
        if (owner == null) return 0;
        try {
            Object actualHook = Api.GET_HOOK.invoke(null, owner);
            return actualHook instanceof FishingHook fishingHook
                    ? ((FishingHookAccessor) fishingHook).stardewFishing$getLuck() : 0;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            reportReflectionFailure(exception);
            return 0;
        }
    }

    public static void storeReward(ServerPlayer player, FishingHook hook, ItemStack item) {
        LockableList<ItemStack> rewards = FishingHookState.get(hook).stardewFishing$getRewards();
        rewards.unlock();
        rewards.clear();
        rewards.add(item.copy());
        LegendaryFishSelector.replaceFirstFish(rewards, hook, player);
        player.fishing = hook;
    }

    public static void startMinigame(ServerPlayer player, ItemStack item) {
        FishingHook hook = player.fishing;
        if (hook == null) {
            return;
        }
        LockableList<ItemStack> rewards = FishingHookState.get(hook).stardewFishing$getRewards();
        if (rewards.isEmpty()) {
            rewards.add(item.copy());
        }
        if (!FishingHookLogic.startStardewMinigame(player)) {
            retrieveThroughTide(player);
        }
    }

    private static void retrieveThroughTide(ServerPlayer player) {
        ItemStack rod = getTideRod(player);
        if (rod.isEmpty()) {
            StardewFishing.LOGGER.error("Tide compatibility could not resume normal retrieval: no Tide rod is held");
            return;
        }

        try {
            if (Api.ROD_RETRIEVE_HOOK != null) {
                Api.ROD_RETRIEVE_HOOK.invoke(rod.getItem(), rod, player, player.level());
                return;
            }

            Object hook = Api.GET_HOOK == null ? null : Api.GET_HOOK.invoke(null, player);
            if (hook != null && Api.HOOK_RETRIEVE_HOOK != null) {
                Api.HOOK_RETRIEVE_HOOK.invoke(hook, rod, player, player.level());
                return;
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            reportReflectionFailure(exception);
            return;
        }

        StardewFishing.LOGGER.error("Tide compatibility could not resume normal retrieval: unsupported Tide API");
    }

    private static ItemStack getTideRod(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (isRod(mainHand)) return mainHand;
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        return isRod(offHand) ? offHand : ItemStack.EMPTY;
    }

    private static void reportReflectionFailure(Throwable exception) {
        if (reflectionFailureReported) return;
        reflectionFailureReported = true;
        StardewFishing.LOGGER.error("Tide compatibility API invocation failed; falling back to base fishing behavior", exception);
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> alias(String oldName, String newName) {
        return Map.entry(StardewFishing.resource(TIDE_ID, oldName), StardewFishing.resource(TIDE_ID, newName));
    }

    private static final class Api {
        private static final Class<?> ROD_CLASS = loadClass("com.li64.tide.registries.items.TideFishingRodItem");
        private static final Class<?> MANAGER_CLASS = loadClass("com.li64.tide.data.rods.CustomRodManager");
        private static final Class<?> HOOK_ACCESSOR_CLASS = loadClass(
                "com.li64.tide.registries.entities.misc.fishing.HookAccessor");
        private static final Class<?> HOOK_CLASS = loadClass(
                "com.li64.tide.registries.entities.misc.fishing.TideFishingHook");
        private static final Method HAS_BOBBER = method(MANAGER_CLASS, "hasBobber", ItemStack.class);
        private static final Method GET_BOBBER = method(MANAGER_CLASS, "getBobber", ItemStack.class);
        private static final Method SET_BOBBER = method(MANAGER_CLASS, "setBobber", ItemStack.class, ItemStack.class);
        private static final Method GET_ACCESSORY_LIST = method(MANAGER_CLASS, "getAccessoryList", ItemStack.class);
        private static final Method GET_HOOK = method(HOOK_ACCESSOR_CLASS, "getHook", Player.class);
        private static final Method ROD_RETRIEVE_HOOK = optionalMethod(
                ROD_CLASS, "retrieveHook", ItemStack.class, Player.class, Level.class);
        private static final Method HOOK_RETRIEVE_HOOK = optionalMethod(
                HOOK_CLASS, "retrieveHook", ItemStack.class, Player.class, Level.class);

        private Api() {
        }

        private static Class<?> loadClass(String name) {
            if (!FabricLoader.getInstance().isModLoaded(TIDE_ID)) return null;
            try {
                return Class.forName(name, false, TideCompat.class.getClassLoader());
            } catch (ClassNotFoundException | LinkageError exception) {
                reportReflectionFailure(exception);
                return null;
            }
        }

        private static Method method(Class<?> owner, String name, Class<?>... parameters) {
            if (owner == null) return null;
            try {
                return owner.getMethod(name, parameters);
            } catch (NoSuchMethodException exception) {
                reportReflectionFailure(exception);
                return null;
            }
        }

        private static Method optionalMethod(Class<?> owner, String name, Class<?>... parameters) {
            if (owner == null) return null;
            try {
                return owner.getMethod(name, parameters);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }
    }
}
