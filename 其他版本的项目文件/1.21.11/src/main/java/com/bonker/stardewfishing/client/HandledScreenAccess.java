package com.bonker.stardewfishing.client;

import net.minecraft.screen.slot.Slot;

public interface HandledScreenAccess {
    Slot stardewFishing$getFocusedSlot();

    int stardewFishing$getX();

    int stardewFishing$getY();
}
