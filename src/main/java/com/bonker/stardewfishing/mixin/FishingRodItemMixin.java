package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin {
    @ModifyArg(method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
            index = 2)
    private Consumer<LivingEntity> stardewFishing$dropBobberWhenRodBreaks(Consumer<LivingEntity> original,
                                                                          @Local(argsOnly = true) Player player,
                                                                          @Local(argsOnly = true) InteractionHand hand) {
        ItemStack bobber = FishingItemSupport.getBobber(player.getItemInHand(hand)).copy();
        return entity -> {
            if (!bobber.isEmpty()) player.spawnAtLocation(bobber);
            original.accept(entity);
        };
    }
}
