package com.bonker.stardewfishing.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {
    @Accessor
    int getNibble();

    @Accessor
    int getTimeUntilHooked();

    @Accessor
    int getTimeUntilLured();

    @Accessor
    void setTimeUntilLured(int value);

    @Accessor
    int getLureSpeed();
}
