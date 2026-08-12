package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

public final class ClientEvents {
    private ClientEvents() {
    }

    /**
     * Pure sound transform API invoked from the sound engine mixin for every sound about to be played.
     * Returns a replacement {@link SoundInstance}, {@code null} to cancel the sound, or the original
     * instance when no transformation applies.
     */
    @Nullable
    public static SoundInstance transformSound(@Nullable SoundInstance instance) {
        try {
            if (instance == null) {
                return null;
            }

            SoundEvent replacement = null;
            if (instance instanceof SimpleSoundInstance && instance.getLocation().getNamespace().equals("minecraft")) {
                switch (instance.getLocation().getPath()) {
                    case "entity.fishing_bobber.throw" -> replacement = SFSoundEvents.CAST.get();
                    case "entity.fishing_bobber.retrieve" -> {
                        if (Minecraft.getInstance().level == null) break;
                        Player player = Minecraft.getInstance().level.getNearestPlayer(instance.getX(), instance.getY(), instance.getZ(), 1, false);
                        replacement = player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM.get() : SFSoundEvents.FISH_HIT.get();
                    }
                    case "entity.fishing_bobber.splash" -> replacement = SFSoundEvents.FISH_BITE.get();
                }
            }

            if (replacement != null) {
                return new SimpleSoundInstance(
                        replacement,
                        SoundSource.MASTER,
                        1.0F,
                        1.0F,
                        SoundInstance.createUnseededRandom(),
                        instance.getX(),
                        instance.getY(),
                        instance.getZ());
            } else if (SFConfig.isolateAudioCues() && !instance.getLocation().getNamespace().equals(StardewFishing.MODID) && Minecraft.getInstance().screen instanceof FishingScreen) {
                return null;
            }
        } catch (Exception e) {
            StardewFishing.LOGGER.error("An exception occurred while trying to replace a sound event.", e);
        }
        return instance;
    }

    public static void stopReelingSounds() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;

        minecraft.getSoundManager().stop(SFSoundEvents.REEL_FAST.get().getLocation(), null);
        minecraft.getSoundManager().stop(SFSoundEvents.REEL_SLOW.get().getLocation(), null);
    }
}
