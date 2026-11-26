package com.bonker.stardewfishing.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot") Slot stardewFishing$getHoveredSlot();
    @Accessor("leftPos") int stardewFishing$getLeftPos();
    @Accessor("topPos") int stardewFishing$getTopPos();
}
