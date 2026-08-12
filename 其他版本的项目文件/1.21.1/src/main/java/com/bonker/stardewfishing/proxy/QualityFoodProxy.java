package com.bonker.stardewfishing.proxy;

import net.minecraft.world.item.ItemStack;

/** Quality Food has no verified Fabric 1.21.1 API; keep rewards unchanged. */
public final class QualityFoodProxy {
    public static void applyQuality(ItemStack stack, int quality) {
    }

    private QualityFoodProxy() {
    }
}
