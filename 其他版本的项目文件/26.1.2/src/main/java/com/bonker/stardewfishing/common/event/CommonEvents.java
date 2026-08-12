package com.bonker.stardewfishing.common.event;

import com.bonker.stardewfishing.network.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.network.S2CStartMinigamePacket;
import com.bonker.stardewfishing.network.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.registry.SFAttributes;
import com.bonker.stardewfishing.server.command.SFCommands;
import com.bonker.stardewfishing.server.loot.LegendaryFishModifier;
import com.bonker.stardewfishing.server.resource.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.resource.MinigameModifiersReloadListener;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;

import static com.bonker.stardewfishing.StardewFishing.identifier;

public final class CommonEvents {
    private CommonEvents() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
                SFCommands.register(dispatcher, buildContext));

        ResourceLoader serverData = ResourceLoader.get(PackType.SERVER_DATA);
        serverData.registerReloadListener(identifier("fish_behaviors"), FishBehaviorReloadListener.create());
        serverData.registerReloadListener(identifier("minigame_modifiers"), MinigameModifiersReloadListener.getOrCreate());

        PayloadTypeRegistry.clientboundPlay().register(S2CStartMinigamePacket.TYPE, S2CStartMinigamePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CSyncModifiersPacket.TYPE, S2CSyncModifiersPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCompleteMinigamePacket.TYPE, C2SCompleteMinigamePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePacket.TYPE,
                (payload, context) -> payload.handle(context.player()));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                ServerPlayNetworking.send(player,
                        new S2CSyncModifiersPacket(MinigameModifiersReloadListener.getOrCreate().getData())));

        FabricDefaultAttributeRegistry.MODIFY.register(context -> context.modify(EntityType.PLAYER, (type, builder) -> builder
                .add(SFAttributes.LINE_STRENGTH)
                .add(SFAttributes.BAR_SIZE)
                .add(SFAttributes.TREASURE_CHANCE_BONUS)
                .add(SFAttributes.GOLDEN_CHEST_BONUS)
                .add(SFAttributes.EXP_MULTIPLIER)));

        LootTableEvents.MODIFY_DROPS.register(LegendaryFishModifier::modify);
    }
}
