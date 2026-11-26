package com.bonker.stardewfishing.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Minimal internal event bus replacing the NeoForge event bus for the mod's own server events.
 * Fabric mods can subscribe to these events to observe or modify the minigame lifecycle.
 */
public final class StardewFishingEvents {
    public static final SimpleEvent<StardewMinigameStartedEvent> STARTED = new SimpleEvent<>();
    public static final SimpleEvent<StardewMinigameEndedEvent> ENDED = new SimpleEvent<>();
    public static final SimpleEvent<StardewMinigameModifyRewardsEvent> MODIFY_REWARDS = new SimpleEvent<>();

    private StardewFishingEvents() {
    }

    public static final class SimpleEvent<T> {
        private final List<Consumer<T>> listeners = new ArrayList<>();

        public void register(Consumer<T> listener) {
            listeners.add(listener);
        }

        public void post(T event) {
            for (Consumer<T> listener : listeners) {
                listener.accept(event);
            }
        }
    }
}
