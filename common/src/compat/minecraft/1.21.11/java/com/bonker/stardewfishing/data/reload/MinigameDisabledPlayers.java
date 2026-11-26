package com.bonker.stardewfishing.data.reload;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Saved per-player flag for the {@code /stardew_fishing toggle_minigame} command. */
public class MinigameDisabledPlayers extends PersistentState {
    public static final Codec<MinigameDisabledPlayers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Uuids.CODEC.listOf().fieldOf("minigame_disabled_for").forGetter(o -> o.players)
    ).apply(inst, MinigameDisabledPlayers::new));

    public static final PersistentStateType<MinigameDisabledPlayers> TYPE = new PersistentStateType<>(
            "minigame_disabled_players", MinigameDisabledPlayers::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final List<UUID> players = new ArrayList<>();

    private MinigameDisabledPlayers() {
    }

    private MinigameDisabledPlayers(List<UUID> list) {
        players.addAll(list);
    }

    public boolean isMinigameDisabled(ServerPlayerEntity player) {
        return players.contains(player.getUuid());
    }

    public void setMinigameDisabled(ServerPlayerEntity player, boolean disabled) {
        UUID uuid = player.getUuid();
        if (disabled) {
            if (!players.contains(uuid)) {
                players.add(uuid);
                markDirty();
            }
        } else {
            if (players.contains(uuid)) {
                players.remove(uuid);
                markDirty();
            }
        }
    }

    public static MinigameDisabledPlayers get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }
}
