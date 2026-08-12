package com.bonker.stardewfishing.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SFConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    private static final ForgeConfigSpec.DoubleValue QUALITY_1_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue QUALITY_2_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue QUALITY_3_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue QUALITY_1_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue QUALITY_2_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue QUALITY_3_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue BITE_TIME_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue TREASURE_CHEST_CHANCE;
    private static final ForgeConfigSpec.DoubleValue GOLDEN_CHEST_CHANCE;
    private static final ForgeConfigSpec.BooleanValue INVENTORY_BOBBER_EQUIPPING;
    private static final ForgeConfigSpec.BooleanValue LEGENDARY_FISH_FLASHING;
    private static final ForgeConfigSpec.DoubleValue LEGENDARY_FISH_CHANCE;
    private static final ForgeConfigSpec.BooleanValue ISOLATE_AUDIO_CUES;
    private static final ForgeConfigSpec.BooleanValue PAUSE_DURING_MINIGAME;

    static {
        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
        QUALITY_1_THRESHOLD = server.comment("The minimum accuracy that grants quality 1.").defineInRange("quality1Threshold", 0.75, 0, 1);
        QUALITY_2_THRESHOLD = server.comment("The minimum accuracy that grants quality 2.").defineInRange("quality2Threshold", 0.9, 0, 1);
        QUALITY_3_THRESHOLD = server.comment("The minimum accuracy that grants quality 3.").defineInRange("quality3Threshold", 1.0, 0, 1);
        QUALITY_1_MULTIPLIER = server.comment("Experience multiplier for quality 1.").defineInRange("quality1Multiplier", 1.5, 1, 10);
        QUALITY_2_MULTIPLIER = server.comment("Experience multiplier for quality 2.").defineInRange("quality2Multiplier", 2.5, 1, 10);
        QUALITY_3_MULTIPLIER = server.comment("Experience multiplier for quality 3.").defineInRange("quality3Multiplier", 4.0, 1, 10);
        BITE_TIME_MULTIPLIER = server.comment("Multiplier applied to the vanilla bite delay.").defineInRange("biteTimeMultiplier", 0.8, 0, 1);
        TREASURE_CHEST_CHANCE = server.comment("Chance for a treasure chest in the minigame.").defineInRange("treasureChestChance", 0.15, 0, 1);
        GOLDEN_CHEST_CHANCE = server.comment("Chance that a treasure chest is golden.").defineInRange("goldenChestChance", 0.1, 0, 1);
        INVENTORY_BOBBER_EQUIPPING = server.comment("Allow right-click bobber equipping in inventories.").define("inventoryBobberEquipping", true);
        LEGENDARY_FISH_FLASHING = server.comment("Allow legendary fish to flash in the minigame.").define("legendaryFishFlashing", true);
        LEGENDARY_FISH_CHANCE = server.comment("Base chance that a caught fish is replaced by a legendary fish.").defineInRange("legendaryFishChance", 0.01, 0, 1);
        SERVER_SPEC = server.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ISOLATE_AUDIO_CUES = client.comment("Mute unrelated sounds while the minigame is active.").define("isolateAudioCues", false);
        PAUSE_DURING_MINIGAME = client.comment("Pause an integrated server during the minigame.").define("pauseDuringMinigame", true);
        CLIENT_SPEC = client.build();
    }

    private SFConfig() {
    }

    public static int getQuality(double accuracy) {
        if (accuracy >= QUALITY_3_THRESHOLD.get()) return 3;
        if (accuracy >= QUALITY_2_THRESHOLD.get()) return 2;
        if (accuracy >= QUALITY_1_THRESHOLD.get()) return 1;
        return 0;
    }

    public static double getMultiplier(double accuracy, double eventMultiplier) {
        double multiplier = switch (getQuality(accuracy)) {
            case 3 -> QUALITY_3_MULTIPLIER.get();
            case 2 -> QUALITY_2_MULTIPLIER.get();
            case 1 -> QUALITY_1_MULTIPLIER.get();
            default -> 1.0;
        };
        return multiplier * eventMultiplier;
    }

    public static double getBiteTimeMultiplier() { return BITE_TIME_MULTIPLIER.get(); }
    public static double getTreasureChestChance() { return TREASURE_CHEST_CHANCE.get(); }
    public static double getGoldenChestChance() { return GOLDEN_CHEST_CHANCE.get(); }
    public static boolean isInventoryEquippingEnabled() { return !SERVER_SPEC.isLoaded() || INVENTORY_BOBBER_EQUIPPING.get(); }
    public static boolean isLegendaryFlashingEnabled() { return LEGENDARY_FISH_FLASHING.get(); }
    public static float getLegendaryFishChance(float luck) { return (float) (LEGENDARY_FISH_CHANCE.get() + luck * 0.01F); }
    public static boolean isolateAudioCues() { return ISOLATE_AUDIO_CUES.get(); }
    public static boolean pauseDuringMinigame() { return PAUSE_DURING_MINIGAME.get(); }
}
