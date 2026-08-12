package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import com.bonker.stardewfishing.server.data.FishingHookDataAccess;
import com.bonker.stardewfishing.server.data.LegendaryFishModifier;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FishingHook.class, priority = 1100)
public abstract class FishingHookMixin extends Entity implements FishingHookAccessor, FishingHookDataAccess {
    @Unique
    private final FishingHookAttachment stardewFishing$data = new FishingHookAttachment();

    private FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public FishingHookAttachment stardewFishing$getData() {
        return stardewFishing$data;
    }

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$adjustBiteTime(BlockPos pos, CallbackInfo ci) {
        if (getNibble() <= 0 && getTimeUntilHooked() <= 0 && getTimeUntilLured() <= 0) {
            int time = Mth.nextInt(random, 100, 600) - getLureSpeed() * 100;
            setTimeUntilLured(Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier())));
        }
        if (!stardewFishing$data.getRewards().isEmpty()) ci.cancel();
    }

    @Inject(method = "retrieve", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V",
            ordinal = 1),
            cancellable = true)
    private void stardewFishing$interceptRewards(ItemStack rod, CallbackInfoReturnable<Integer> cir,
                                                  @Local List<ItemStack> rewards) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) return;

        LegendaryFishModifier.applyFishingLoot(rewards, (ServerLevel) level(), blockPosition(), getLuck() + player.getLuck());
        if (rewards.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            stardewFishing$data.getRewards().addAll(rewards);
            if (FishingHookLogic.startStardewMinigame(player)) cir.setReturnValue(1);
        } else {
            FishingHookLogic.modifyRewards(rewards, 0, 0);
        }
    }
}
