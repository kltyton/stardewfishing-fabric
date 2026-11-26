package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.FishingHookLogic;
import com.bonker.stardewfishing.gameplay.LegendaryFishHandler;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookAttachment;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity implements FishingBobberEntityAccessor {
    protected FishingBobberEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickFishingLogic", at = @At("HEAD"))
    private void stardewFishing$adjustBiteTime(BlockPos pos, CallbackInfo ci) {
        if (getHookCountdown() <= 0 && getWaitCountdown() <= 0 && getFishTravelCountdown() <= 0) {
            int time = MathHelper.nextInt(random, 100, 600);
            setWaitCountdown(Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier())));
        }
    }

    @Inject(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/FishingRodHookedCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/projectile/FishingBobberEntity;Ljava/util/Collection;)V",
                    ordinal = 1),
            cancellable = true)
    private void stardewFishing$startMinigame(ItemStack rod, CallbackInfoReturnable<Integer> cir,
                                              @Local List<ItemStack> items) {
        FishingBobberEntity hook = (FishingBobberEntity) (Object) this;
        ServerPlayerEntity player = (ServerPlayerEntity) hook.getPlayerOwner();
        if (player == null) {
            return;
        }

        LegendaryFishHandler.maybeReplace(items, (ServerWorld) getEntityWorld(),
                getBlockPos(), getLuckBonus(), LootTables.FISHING_GAMEPLAY.getValue());

        if (items.stream().anyMatch(stack -> stack.isIn(StardewFishing.STARTS_MINIGAME))) {
            FishingHookAttachment.get(hook).getRewards().addAll(items);
            if (FishingHookLogic.startStardewMinigame(player)) {
                cir.setReturnValue(0);
            }
        } else {
            FishingHookLogic.modifyRewards(items, 0, 0);
            getEntityWorld().playSound(null, player.getBlockPos(), SFSoundEvents.PULL_ITEM,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Inject(method = "onRemoved", at = @At("TAIL"))
    private void stardewFishing$clearAttachment(CallbackInfo ci) {
        FishingHookAttachment.remove((FishingBobberEntity) (Object) this);
    }
}
