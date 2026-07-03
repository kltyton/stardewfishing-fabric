package com.kltyton.stardewfishingFabric.mixin;

import com.kltyton.stardewfishingFabric.common.FishingDataStorage;
import com.kltyton.stardewfishingFabric.common.FishingHookLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingHookMixin {
    @Inject(method = "tickFishingLogic", at = @At("TAIL"))
    private void startMinigameOnBite(net.minecraft.util.math.BlockPos pos, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        FishingBobberEntity hook = (FishingBobberEntity) (Object) this;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) hook.getPlayerOwner();
        if (serverPlayer == null || !stardewfishing$isCaughtFish()) {
            return;
        }

        if (FishingDataStorage.getHookForPlayer(serverPlayer) != null) {
            return;
        }

        FishingHookLogic.startMinigame(serverPlayer, hook);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void retrieve(net.minecraft.item.ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity hook = (FishingBobberEntity) (Object) this;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) hook.getPlayerOwner();
        if (serverPlayer == null || FishingDataStorage.getHookForPlayer(serverPlayer) != hook) {
            return;
        }

        cir.setReturnValue(0);
    }

    @org.spongepowered.asm.mixin.gen.Accessor("caughtFish")
    protected abstract boolean stardewfishing$isCaughtFish();
}
