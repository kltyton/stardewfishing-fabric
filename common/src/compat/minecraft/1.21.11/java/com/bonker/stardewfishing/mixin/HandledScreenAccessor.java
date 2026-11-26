package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.client.HandledScreenAccess;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor extends HandledScreenAccess {
    @Override
    @Accessor("focusedSlot")
    Slot stardewFishing$getFocusedSlot();

    @Override
    @Accessor("x")
    int stardewFishing$getX();

    @Override
    @Accessor("y")
    int stardewFishing$getY();
}
