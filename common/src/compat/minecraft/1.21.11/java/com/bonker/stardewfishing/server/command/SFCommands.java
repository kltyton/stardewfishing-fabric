package com.bonker.stardewfishing.server.command;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.data.reload.FishBehaviorReloadListener;
import com.bonker.stardewfishing.data.reload.MinigameDisabledPlayers;
import com.bonker.stardewfishing.gameplay.FishingHookLogic;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookAttachment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;

public final class SFCommands {
    private static final DynamicCommandExceptionType NO_BEHAVIOR =
            new DynamicCommandExceptionType(item -> Text.translatable("commands.stardew_fishing.no_behavior", item));
    private static final SimpleCommandExceptionType NO_ROD =
            new SimpleCommandExceptionType(Text.translatable("commands.stardew_fishing.no_rod"));

    private SFCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("stardew_fishing")
                .then(CommandManager.literal("start_minigame")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .executes(SFCommands::startMinigame)))
                .then(CommandManager.literal("toggle_minigame")
                        .executes(SFCommands::toggleMinigame)));
    }

    private static int startMinigame(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        Hand rodHand = ItemUtils.getRodHand(player);
        if (rodHand == null) {
            throw NO_ROD.create();
        }

        ItemStackArgument argument = ItemStackArgumentType.getItemStackArgument(context, "item");
        if (!FishBehaviorReloadListener.getKeys().contains(Registries.ITEM.getId(argument.getItem()))) {
            throw NO_BEHAVIOR.create(argument.getItem());
        }
        ItemStack stack = argument.createStack(1, false);
        FishingBobberEntity hook = ItemUtils.spawnHook(
                player, player.getStackInHand(rodHand), player.getEntityPos().add(0, 1, 0));
        player.fishHook = hook;
        List<ItemStack> rewards = FishingHookAttachment.get(hook).getRewards();
        rewards.clear();
        rewards.add(stack);
        FishingHookLogic.startStardewMinigame(player);
        return 1;
    }

    private static int toggleMinigame(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        MinigameDisabledPlayers data = MinigameDisabledPlayers.get(source.getServer());
        boolean disabled = data.isMinigameDisabled(player);
        data.setMinigameDisabled(player, !disabled);
        source.sendFeedback(() -> Text.translatable("commands.stardew_fishing."
                + (disabled ? "enabled" : "disabled") + "_minigame"), true);
        return 1;
    }
}
