package com.bonker.stardewfishing.common.event;

import com.bonker.stardewfishing.network.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.network.SFNetworking;
import com.bonker.stardewfishing.server.resource.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.command.SFCommands;
import com.bonker.stardewfishing.server.resource.MinigameModifiersReloadListener;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class CommonEvents {
    private CommonEvents() {
    }

    public static void initialize() {
        SFNetworking.registerCommon();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SFCommands.register(dispatcher, registryAccess));
        ResourceManagerHelper data = ResourceManagerHelper.get(PackType.SERVER_DATA);
        data.registerReloadListener(FishBehaviorReloadListener.create());
        data.registerReloadListener(MinigameModifiersReloadListener.getOrCreate());
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                SFNetworking.sendModifiers(player,
                        new S2CSyncModifiersPacket(MinigameModifiersReloadListener.getOrCreate().getData())));
    }
}
