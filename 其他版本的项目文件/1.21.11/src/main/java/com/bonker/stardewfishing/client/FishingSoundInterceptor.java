package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class FishingSoundInterceptor {
    private FishingSoundInterceptor() {
    }

    public static SoundInstance replace(SoundInstance sound) {
        if (SFConfig.isolateAudioCues()
                && MinecraftClient.getInstance().currentScreen instanceof FishingScreen
                && !sound.getId().getNamespace().equals(StardewFishing.MODID)) {
            return null;
        }
        if (!sound.getId().getNamespace().equals("minecraft")) {
            return sound;
        }

        SoundEvent replacement = switch (sound.getId().getPath()) {
            case "entity.fishing_bobber.throw" -> SFSoundEvents.CAST;
            case "entity.fishing_bobber.splash" -> SFSoundEvents.FISH_BITE;
            case "entity.fishing_bobber.retrieve" -> retrieveSound(sound);
            default -> null;
        };
        if (replacement == null) {
            return sound;
        }
        return new PositionedSoundInstance(replacement, SoundCategory.MASTER, sound.getVolume(),
                sound.getPitch(), SoundInstance.createRandom(), sound.getX(), sound.getY(), sound.getZ());
    }

    private static SoundEvent retrieveSound(SoundInstance sound) {
        if (MinecraftClient.getInstance().world == null) {
            return SFSoundEvents.PULL_ITEM;
        }
        PlayerEntity player = MinecraftClient.getInstance().world.getClosestPlayer(
                sound.getX(), sound.getY(), sound.getZ(), 1, false);
        return player == null || player.fishHook == null ? SFSoundEvents.PULL_ITEM : SFSoundEvents.FISH_HIT;
    }
}
