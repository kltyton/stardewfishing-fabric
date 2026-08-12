package com.kltyton.stardewfishingFabric.mixin;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.api.event.StardewMinigameStartedEvent;
import com.kltyton.stardewfishingFabric.common.FishingHookLogic;
import com.kltyton.stardewfishingFabric.common.config.SFConfig;
import com.kltyton.stardewfishingFabric.registry.SFSoundEvents;
import com.kltyton.stardewfishingFabric.server.fishing.FishingHookState;
import com.kltyton.stardewfishingFabric.server.fishing.LockableList;
import com.kltyton.stardewfishingFabric.server.loot.LegendaryFishSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity implements FishingHookAccessor, FishingHookState {
    @Unique private final LockableList<ItemStack> stardewFishing$rewards = new LockableList<>();
    @Unique private boolean stardewFishing$treasureChest;
    @Unique private boolean stardewFishing$goldenChest;
    @Unique private StardewMinigameStartedEvent stardewFishing$event;

    private FishingHookMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void stardewFishing$adjustBiteTime(BlockPos pos, CallbackInfo ci) {
        if (getNibble() <= 0 && getTimeUntilHooked() <= 0 && getTimeUntilLured() <= 0) {
            int time = Mth.nextInt(random, 100, 600) - getLureSpeed() * 100;
            setTimeUntilLured(Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier())));
        }
        if (!stardewFishing$rewards.isEmpty()) ci.cancel();
    }

    @Inject(method = "retrieve",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V", ordinal = 1),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void stardewFishing$captureRewards(ItemStack rod, CallbackInfoReturnable<Integer> cir,
                                                Player owner, int result, LootParams params, LootTable table,
                                                List<ItemStack> rewards) {
        if (!(owner instanceof ServerPlayer player)) return;
        FishingHook hook = (FishingHook) (Object) this;
        LegendaryFishSelector.replaceFirstFish(rewards, hook, player);
        if (rewards.stream().anyMatch(stack -> stack.is(StardewfishingFabric.STARTS_MINIGAME))) {
            stardewFishing$rewards.addAll(rewards);
            if (FishingHookLogic.startStardewMinigame(player)) cir.setReturnValue(0);
        } else {
            FishingHookLogic.modifyRewards(rewards, 0, 0);
            level().playSound(null, player, SFSoundEvents.PULL_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override public LockableList<ItemStack> stardewFishing$getRewards() { return stardewFishing$rewards; }
    @Override public boolean stardewFishing$hasTreasureChest() { return stardewFishing$treasureChest; }
    @Override public void stardewFishing$setTreasureChest(boolean value) { stardewFishing$treasureChest = value; }
    @Override public boolean stardewFishing$hasGoldenChest() { return stardewFishing$goldenChest; }
    @Override public void stardewFishing$setGoldenChest(boolean value) { stardewFishing$goldenChest = value; }
    @Override public @Nullable StardewMinigameStartedEvent stardewFishing$getEvent() { return stardewFishing$event; }
    @Override public void stardewFishing$setEvent(@Nullable StardewMinigameStartedEvent event) { stardewFishing$event = event; }
}
