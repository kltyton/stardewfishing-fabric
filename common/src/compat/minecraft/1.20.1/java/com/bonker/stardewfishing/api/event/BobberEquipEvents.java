package com.bonker.stardewfishing.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.inventory.Slot;

public final class BobberEquipEvents {
    public static final Event<Changed> CHANGED = EventFactory.createArrayBacked(
            Changed.class,
            callbacks -> (slot, equipped) -> {
                for (Changed callback : callbacks) {
                    callback.onChanged(slot, equipped);
                }
            }
    );

    private BobberEquipEvents() {
    }

    @FunctionalInterface
    public interface Changed {
        void onChanged(Slot slot, boolean equipped);
    }
}

