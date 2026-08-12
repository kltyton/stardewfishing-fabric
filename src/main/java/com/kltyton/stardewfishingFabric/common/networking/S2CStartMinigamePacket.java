package com.kltyton.stardewfishingFabric.common.networking;

import com.kltyton.stardewfishingFabric.api.event.StardewMinigameStartedEvent;
import com.kltyton.stardewfishingFabric.common.FishBehavior;
import com.kltyton.stardewfishingFabric.server.fishing.FishingHookState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record S2CStartMinigamePacket(int idleTime, float topSpeed, float upAcceleration, float downAcceleration,
                                     int avgDistance, int moveVariation, ItemStack fish, boolean treasureChest,
                                     boolean goldenChest, float lineStrength, int barSize) {
    public S2CStartMinigamePacket(FishingHookState state) {
        this(requireEvent(state), state);
    }

    private S2CStartMinigamePacket(StardewMinigameStartedEvent event, FishingHookState state) {
        this(event.getIdleTime(), event.getTopSpeed(), event.getUpAcceleration(), event.getDownAcceleration(),
                event.getAvgDistance(), event.getMoveVariation(), event.getFish(), state.stardewFishing$hasTreasureChest(),
                state.stardewFishing$hasGoldenChest(), (float) event.getLineStrength(), event.getBarSize());
    }

    public S2CStartMinigamePacket(FishBehavior behavior) {
        this(behavior.idleTime(), behavior.topSpeed(), behavior.upAcceleration(), behavior.downAcceleration(),
                behavior.avgDistance(), behavior.moveVariation(), ItemStack.EMPTY, false, false, 1.0F, 36);
    }

    public S2CStartMinigamePacket(FriendlyByteBuf buf) {
        this(buf.readShort(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readShort(), buf.readShort(),
                buf.readItem(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readShort());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(idleTime);
        buf.writeFloat(topSpeed);
        buf.writeFloat(upAcceleration);
        buf.writeFloat(downAcceleration);
        buf.writeShort(avgDistance);
        buf.writeShort(moveVariation);
        buf.writeItem(fish);
        buf.writeBoolean(treasureChest);
        buf.writeBoolean(goldenChest);
        buf.writeFloat(lineStrength);
        buf.writeShort(barSize);
    }

    private static StardewMinigameStartedEvent requireEvent(FishingHookState state) {
        StardewMinigameStartedEvent event = state.stardewFishing$getEvent();
        if (event == null) throw new IllegalStateException("Cannot create start packet without a minigame event");
        return event;
    }
}
