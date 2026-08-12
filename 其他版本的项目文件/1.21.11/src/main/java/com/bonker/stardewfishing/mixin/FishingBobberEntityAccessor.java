package com.bonker.stardewfishing.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {
    @Accessor
    int getHookCountdown();

    @Accessor
    int getWaitCountdown();

    @Accessor
    void setWaitCountdown(int value);

    @Accessor
    int getFishTravelCountdown();

    @Accessor
    int getLuckBonus();
}
