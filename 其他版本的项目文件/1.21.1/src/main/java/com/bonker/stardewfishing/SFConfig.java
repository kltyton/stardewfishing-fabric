package com.bonker.stardewfishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SFConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Values values = new Values();

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("stardew_fishing.json");
        try {
            if (Files.isRegularFile(path)) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    Values loaded = GSON.fromJson(reader, Values.class);
                    if (loaded != null) values = loaded;
                }
            }
            values.clamp();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(values, writer);
            }
        } catch (IOException | RuntimeException exception) {
            StardewFishing.LOGGER.error("Unable to load config {}, using defaults", path, exception);
            values = new Values();
        }
    }

    public static int getQuality(double accuracy) {
        if (accuracy >= values.quality3Threshold) return 3;
        if (accuracy >= values.quality2Threshold) return 2;
        if (accuracy >= values.quality1Threshold) return 1;
        return 0;
    }

    public static double getMultiplier(double accuracy, double attributeMultiplier) {
        return (switch (getQuality(accuracy)) {
            case 3 -> values.quality3Multiplier;
            case 2 -> values.quality2Multiplier;
            case 1 -> values.quality1Multiplier;
            default -> 1.0;
        }) * attributeMultiplier;
    }

    public static double getBiteTimeMultiplier() { return values.biteTimeMultiplier; }
    public static double getTreasureChestChance() { return values.treasureChestChance; }
    public static double getGoldenChestChance() { return values.goldenChestChance; }
    public static boolean isInventoryEquippingEnabled() { return values.inventoryBobberEquipping; }
    public static boolean isLegendaryFlashingEnabled() { return values.legendaryFishFlashing; }
    public static float getLegendaryFishChance(float luck) { return (float) (values.legendaryFishChance + luck * 0.01F); }
    public static boolean isolateAudioCues() { return values.isolateAudioCues; }
    public static boolean pauseDuringMinigame() { return values.pauseDuringMinigame; }

    private static final class Values {
        double quality1Threshold = 0.75;
        double quality2Threshold = 0.9;
        double quality3Threshold = 1.0;
        double quality1Multiplier = 1.5;
        double quality2Multiplier = 2.5;
        double quality3Multiplier = 4.0;
        double biteTimeMultiplier = 0.8;
        double treasureChestChance = 0.15;
        double goldenChestChance = 0.1;
        boolean inventoryBobberEquipping = true;
        boolean legendaryFishFlashing = true;
        double legendaryFishChance = 0.01;
        boolean isolateAudioCues = false;
        boolean pauseDuringMinigame = true;

        void clamp() {
            quality1Threshold = clamp(quality1Threshold, 0, 1);
            quality2Threshold = clamp(quality2Threshold, 0, 1);
            quality3Threshold = clamp(quality3Threshold, 0, 1);
            quality1Multiplier = clamp(quality1Multiplier, 1, 10);
            quality2Multiplier = clamp(quality2Multiplier, 1, 10);
            quality3Multiplier = clamp(quality3Multiplier, 1, 10);
            biteTimeMultiplier = clamp(biteTimeMultiplier, 0, 1);
            treasureChestChance = clamp(treasureChestChance, 0, 1);
            goldenChestChance = clamp(goldenChestChance, 0, 1);
            legendaryFishChance = clamp(legendaryFishChance, 0, 1);
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private SFConfig() {
    }
}
