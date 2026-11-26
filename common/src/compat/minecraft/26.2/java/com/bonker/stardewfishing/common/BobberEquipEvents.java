package com.bonker.stardewfishing.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.function.BiConsumer;

/**
 * Side-safe bridge for the client-only bobber equip feedback (tooltip shake and equip sounds).
 * The client entrypoint installs a handler; dedicated servers never load client classes.
 */
public final class BobberEquipEvents {
    public static BiConsumer<Player, EquipEvent> CLIENT_HANDLER = null;

    private BobberEquipEvents() {
    }

    public static void notify(Player player, Slot slot, boolean equipped) {
        if (player.level().isClientSide() && CLIENT_HANDLER != null) {
            CLIENT_HANDLER.accept(player, new EquipEvent(slot, equipped));
        }
    }

    public record EquipEvent(Slot slot, boolean equipped) {
    }
}
