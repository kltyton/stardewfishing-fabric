package com.bonker.stardewfishing.mixin.compat.tide;

import com.bonker.stardewfishing.proxy.TideProxy;
import com.li64.tide.compat.CompatHelper;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Connects Tide's third-party minigame callback to the Fabric reward flow. */
@Pseudo
@Mixin(value = CompatHelper.class, remap = false)
abstract class TideMinigameMixin {
    @Inject(method = "stardewFishingStartMinigame", at = @At("HEAD"), cancellable = true, require = 0)
    private static void stardewFishing$startMinigame(ServerPlayer player, HookAccessor hook, ItemStack rod,
                                                     List<ItemStack> rewards, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TideProxy.startMinigame(player, hook, rewards));
    }
}
