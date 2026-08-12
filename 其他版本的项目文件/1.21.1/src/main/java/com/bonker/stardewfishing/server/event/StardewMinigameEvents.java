package com.bonker.stardewfishing.server.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class StardewMinigameEvents {
    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class,
            callbacks -> event -> {
                for (Started callback : callbacks) callback.onStarted(event);
            });
    public static final Event<Ended> ENDED = EventFactory.createArrayBacked(Ended.class,
            callbacks -> event -> {
                for (Ended callback : callbacks) callback.onEnded(event);
            });
    public static final Event<ModifyRewards> MODIFY_REWARDS = EventFactory.createArrayBacked(ModifyRewards.class,
            callbacks -> event -> {
                for (ModifyRewards callback : callbacks) callback.modify(event);
            });

    @FunctionalInterface
    public interface Started { void onStarted(StardewMinigameStartedEvent event); }
    @FunctionalInterface
    public interface Ended { void onEnded(StardewMinigameEndedEvent event); }
    @FunctionalInterface
    public interface ModifyRewards { void modify(StardewMinigameModifyRewardsEvent event); }

    private StardewMinigameEvents() {
    }
}
