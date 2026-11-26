package com.bonker.stardewfishing.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class StardewFishingEvents {
    public static final Event<MinigameStarted> MINIGAME_STARTED = EventFactory.createArrayBacked(
            MinigameStarted.class,
            callbacks -> event -> {
                for (MinigameStarted callback : callbacks) {
                    callback.onMinigameStarted(event);
                }
            }
    );
    public static final Event<MinigameEnded> MINIGAME_ENDED = EventFactory.createArrayBacked(
            MinigameEnded.class,
            callbacks -> event -> {
                for (MinigameEnded callback : callbacks) {
                    callback.onMinigameEnded(event);
                }
            }
    );
    public static final Event<ModifyRewards> MODIFY_REWARDS = EventFactory.createArrayBacked(
            ModifyRewards.class,
            callbacks -> event -> {
                for (ModifyRewards callback : callbacks) {
                    callback.modifyRewards(event);
                }
            }
    );
    public static final Event<AllowRewardDelivery> ALLOW_REWARD_DELIVERY = EventFactory.createArrayBacked(
            AllowRewardDelivery.class,
            callbacks -> event -> {
                for (AllowRewardDelivery callback : callbacks) {
                    if (!callback.allowRewardDelivery(event)) {
                        return false;
                    }
                }
                return true;
            }
    );

    private StardewFishingEvents() {
    }

    @FunctionalInterface
    public interface MinigameStarted {
        void onMinigameStarted(StardewMinigameStartedEvent event);
    }

    @FunctionalInterface
    public interface MinigameEnded {
        void onMinigameEnded(StardewMinigameEndedEvent event);
    }

    @FunctionalInterface
    public interface ModifyRewards {
        void modifyRewards(StardewMinigameModifyRewardsEvent event);
    }

    @FunctionalInterface
    public interface AllowRewardDelivery {
        boolean allowRewardDelivery(StardewMinigameModifyRewardsEvent event);
    }
}

