package com.bonker.stardewfishing.gameplay.minigame;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fabric observer events mirroring the NeoForge reference's bus events for
 * addon-facing minigame lifecycle. Registration happens in
 * {@code com.bonker.stardewfishing.mixin.FishingBobberEntityMixin}-adjacent
 * code paths (server side); observers are plain callbacks.
 */
public final class StardewMinigameEvents {
    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class,
            callbacks -> event -> {
                for (Started callback : callbacks) {
                    callback.onStart(event);
                }
            });

    public static final Event<Ended> ENDED = EventFactory.createArrayBacked(Ended.class,
            callbacks -> event -> {
                for (Ended callback : callbacks) {
                    callback.onEnd(event);
                }
            });

    public static final Event<ModifyRewards> MODIFY_REWARDS = EventFactory.createArrayBacked(ModifyRewards.class,
            callbacks -> event -> {
                for (ModifyRewards callback : callbacks) {
                    callback.onModifyRewards(event);
                }
            });

    private StardewMinigameEvents() {
    }

    @FunctionalInterface
    public interface Started {
        void onStart(StardewMinigameStartedEvent event);
    }

    @FunctionalInterface
    public interface Ended {
        void onEnd(StardewMinigameEndedEvent event);
    }

    @FunctionalInterface
    public interface ModifyRewards {
        void onModifyRewards(StardewMinigameModifyRewardsEvent event);
    }
}
