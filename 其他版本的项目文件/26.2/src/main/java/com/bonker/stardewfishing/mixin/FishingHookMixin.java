package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.common.config.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.CommonEvents;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.server.fishing.FishingHookAttachment;
import com.bonker.stardewfishing.server.loot.LegendaryFishLogic;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FishingHook.class)
public abstract class FishingHookMixin extends Entity implements FishingHookAccessor {
    private FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "catchingFish", at = @At(value = "HEAD"), cancellable = true)
    private void cancel_catchingFish(BlockPos pPos, CallbackInfo ci) {
        if (getNibble() <= 0 && getTimeUntilHooked() <= 0 && getTimeUntilLured() <= 0) {
            // replicate vanilla
            int time = Mth.nextInt(random, 100, 600);
            time -= getLureSpeed() * 20 * 5;

            // apply configurable reduction
            time = Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier()));

            setTimeUntilLured(time);
        }

        if (!FishingHookAttachment.get((FishingHook) (Object) this).getRewards().isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/advancements/triggers/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V"),
            cancellable = true)
    private void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir,
                          @Local List<ItemStack> items, @Local LootParams params) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
        if (player == null) return;

        // Fabric-side legendary fish hook into actual fishing loot before reward locking/spawn
        LegendaryFishLogic.apply(items, params, (ServerLevel) hook.level());

        if (items.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            FishingHookAttachment.get(hook).getRewards().addAll(items);
            if (FishingHookLogic.startStardewMinigame(player)) {
                cir.cancel();
            }
        } else {
            CommonEvents.markLegendaryCatches(hook, items);
            FishingHookLogic.modifyRewards(items, 0, 0);
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
