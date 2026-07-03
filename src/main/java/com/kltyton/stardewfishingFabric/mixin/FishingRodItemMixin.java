package com.kltyton.stardewfishingFabric.mixin;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Redirect(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
                    ordinal = 0))
    private void redirectPlaySoundFirst(World instance, Entity entity, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        PlayerEntity nearestPlayer = null;
        if (MinecraftClient.getInstance().world != null) {
            nearestPlayer = MinecraftClient.getInstance().world.getClosestPlayer(x, y, z, 1, false);
        }
        if (nearestPlayer != null && nearestPlayer.fishHook != null) {
            FishingBobberEntity fishingHook = nearestPlayer.fishHook;

            boolean isBiting = fishingHook.getHookedEntity() != null;

            if (isBiting) {
                instance.playSound(null, x, y, z, StardewfishingFabric.FISH_HIT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            } else {
                instance.playSound(null, x, y, z, StardewfishingFabric.PULL_ITEM, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }
    }

    @Redirect(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
                    ordinal = 1))
    private void redirectPlaySoundSecond(World instance, Entity entity, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        instance.playSound(null, x, y, z, StardewfishingFabric.CAST, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
}
