package com.kltyton.stardewfishingFabric.common;

import com.kltyton.stardewfishingFabric.server.FishBehaviorReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;

public class CommonEvents {

    // 初始化公共事件
    public static void initialize() {
        // 注册资源重载监听器
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(FishBehaviorReloadListener.ID, FishBehaviorReloadListener.create());
    }
}
