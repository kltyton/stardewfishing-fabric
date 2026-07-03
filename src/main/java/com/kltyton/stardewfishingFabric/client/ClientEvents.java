package com.kltyton.stardewfishingFabric.client;

import com.kltyton.stardewfishingFabric.common.FishBehavior;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.item.ItemStack;

public class ClientEvents implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SFNetworking.registerClientReceivers();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                SoundManager soundManager = client.getSoundManager();
                soundManager.tick(false);
            }
        });
    }

    // 打开钓鱼屏幕的静态方法
    public static void openFishingScreen(FishBehavior behavior, ItemStack previewItem) {
        MinecraftClient.getInstance().setScreen(new FishingScreen(behavior, previewItem));
    }
}
