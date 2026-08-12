package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.config.SFConfig;
import com.bonker.stardewfishing.gameplay.minigame.FishingHookLogic;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.server.fishing.FishingHookState;
import com.bonker.stardewfishing.server.loot.LegendaryFishSelector;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.scouter.netherdepthsupgrade.entity.entities.LavaFishingBobberEntity")
public abstract class LavaFishingBobberEntityMixin extends FishingHook {
    @Shadow private int nibble;
    @Shadow private int timeUntilHooked;
    @Shadow private int timeUntilLured;
    @Shadow @Final private int lureSpeed;

    private LavaFishingBobberEntityMixin(EntityType<? extends FishingHook> type, Level level) {
        super(type, level);
    }

    @Inject(method = "catchingFish(Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"),
            cancellable = true, remap = false)
    private void stardewFishing$adjustBiteTime(BlockPos pos, CallbackInfo ci) {
        if (nibble <= 0 && timeUntilHooked <= 0 && timeUntilLured <= 0) {
            int time = Mth.nextInt(random, 100, 600) - lureSpeed * 100;
            timeUntilLured = Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier()));
        }
        if (!FishingHookState.get(this).stardewFishing$getRewards().isEmpty()) {
            ci.cancel();
        }
    }

    @Redirect(method = {"retrieve", "method_6957"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> stardewFishing$captureRewards(LootTable table, LootParams params) {
        ObjectArrayList<ItemStack> rewards = table.getRandomItems(params);
        Player owner = getPlayerOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return rewards;
        }

        LegendaryFishSelector.replaceFirstFish(rewards, this, player);
        if (rewards.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            FishingHookState.get(this).stardewFishing$getRewards().addAll(rewards);
        } else {
            FishingHookLogic.modifyRewards(rewards, 0, 0);
            level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return rewards;
    }

    @Inject(method = {"retrieve", "method_6957"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V"),
            cancellable = true)
    private void stardewFishing$startMinigame(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        Player owner = getPlayerOwner();
        if (owner instanceof ServerPlayer player
                && !FishingHookState.get(this).stardewFishing$getRewards().isEmpty()
                && FishingHookLogic.startStardewMinigame(player)) {
            cir.setReturnValue(0);
        }
    }
}
