package com.bonker.stardewfishing.server.command;

import com.bonker.stardewfishing.gameplay.minigame.FishingHookLogic;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.server.resource.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.fishing.FishingHookState;
import com.bonker.stardewfishing.server.persistence.MinigameDisabledPlayers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class SFCommands {
    private static final SimpleCommandExceptionType NO_BEHAVIOR = new SimpleCommandExceptionType(
            Component.translatable("commands.stardew_fishing.no_behavior", "item"));
    private static final SimpleCommandExceptionType NO_ROD = new SimpleCommandExceptionType(
            Component.translatable("commands.stardew_fishing.no_rod"));

    private SFCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("stardew_fishing")
                .then(Commands.literal("start_minigame")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("item", ItemArgument.item(context))
                                .executes(command -> startMinigame(command.getSource(), ItemArgument.getItem(command, "item")))))
                .then(Commands.literal("toggle_minigame").executes(command -> toggle(command.getSource()))));
    }

    private static int startMinigame(CommandSourceStack source, ItemInput input) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        InteractionHand hand = FishingItemSupport.getRodHand(player);
        if (hand == null) throw NO_ROD.create();
        ItemStack fish = input.createItemStack(1, false);
        if (!FishBehaviorReloadListener.getKeys().contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(fish.getItem()))) {
            throw NO_BEHAVIOR.create();
        }

        FishingHook hook = FishingItemSupport.spawnHook(player, player.getItemInHand(hand), player.position().add(0, 1, 0));
        player.fishing = hook;
        FishingHookState.get(hook).stardewFishing$getRewards().add(fish);
        FishingHookLogic.startStardewMinigame(player);
        return 1;
    }

    private static int toggle(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinigameDisabledPlayers data = MinigameDisabledPlayers.get(source.getServer());
        boolean disabled = data.isMinigameDisabled(player);
        data.setMinigameDisabled(player, !disabled);
        source.sendSuccess(() -> Component.translatable("commands.stardew_fishing."
                + (disabled ? "enabled" : "disabled") + "_minigame"), false);
        return 1;
    }
}
