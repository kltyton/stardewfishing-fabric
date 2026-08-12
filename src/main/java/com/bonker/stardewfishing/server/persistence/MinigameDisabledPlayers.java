package com.bonker.stardewfishing.server.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MinigameDisabledPlayers extends SavedData {
    private final Set<UUID> players = new HashSet<>();

    public MinigameDisabledPlayers() {
    }

    public MinigameDisabledPlayers(CompoundTag tag) {
        ListTag list = tag.getList("minigame_disabled_for", Tag.TAG_INT_ARRAY);
        for (Tag entry : list) players.add(NbtUtils.loadUUID(entry));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        players.forEach(uuid -> list.add(NbtUtils.createUUID(uuid)));
        tag.put("minigame_disabled_for", list);
        return tag;
    }

    public boolean isMinigameDisabled(ServerPlayer player) {
        return players.contains(player.getUUID());
    }

    public void setMinigameDisabled(ServerPlayer player, boolean disabled) {
        boolean changed = disabled ? players.add(player.getUUID()) : players.remove(player.getUUID());
        if (changed) setDirty();
    }

    public static MinigameDisabledPlayers get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                MinigameDisabledPlayers::new, MinigameDisabledPlayers::new, "disabled_minigame_players");
    }
}
