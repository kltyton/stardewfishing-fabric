package com.bonker.stardewfishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal validated Gson config mirroring the NeoForge reference's ModConfigSpec
 * options, defaults and ranges. Written to the Fabric config directory as two
 * files matching the reference's server/client split:
 * {@code config/stardew_fishing_server.json} and {@code config/stardew_fishing_client.json}.
 *
 * <p>Fabric has no built-in server-to-client config sync; clients read their own
 * local copies. Multiplayer hosts should keep the two files consistent.
 */
public final class SFConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // server
    private static double quality1Threshold = 0.75;
    private static double quality2Threshold = 0.9;
    private static double quality3Threshold = 1.0;
    private static double quality1Multiplier = 1.5;
    private static double quality2Multiplier = 2.5;
    private static double quality3Multiplier = 4.0;
    private static double biteTimeMultiplier = 0.8;
    private static double treasureChestChance = 0.15;
    private static double goldenChestChance = 0.1;
    private static boolean inventoryBobberEquipping = true;
    private static boolean legendaryFishFlashing = true;
    private static double legendaryFishChance = 0.01;

    // client
    private static boolean isolateAudioCues = false;
    private static boolean pauseDuringMinigame = true;

    private SFConfig() {
    }

    public static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        loadServer(Files.exists(dir.resolve("stardew_fishing_server.json")) ? dir.resolve("stardew_fishing_server.json") : dir.resolve("stardew_fishing.json"));
        loadClient(dir.resolve("stardew_fishing_client.json"));
    }

    private static void loadServer(Path path) {
        JsonObject object = readOrCreate(path, "server");
        quality1Threshold = clampDouble(object, "quality1Threshold", quality1Threshold, 0, 1);
        quality2Threshold = clampDouble(object, "quality2Threshold", quality2Threshold, 0, 1);
        quality3Threshold = clampDouble(object, "quality3Threshold", quality3Threshold, 0, 1);
        quality1Multiplier = clampDouble(object, "quality1Multiplier", quality1Multiplier, 1, 10);
        quality2Multiplier = clampDouble(object, "quality2Multiplier", quality2Multiplier, 1, 10);
        quality3Multiplier = clampDouble(object, "quality3Multiplier", quality3Multiplier, 1, 10);
        biteTimeMultiplier = clampDouble(object, "biteTimeMultiplier", biteTimeMultiplier, 0, 1);
        treasureChestChance = clampDouble(object, "treasureChestChance", treasureChestChance, 0, 1);
        goldenChestChance = clampDouble(object, "goldenChestChance", goldenChestChance, 0, 1);
        inventoryBobberEquipping = getBoolean(object, "inventoryBobberEquipping", inventoryBobberEquipping);
        legendaryFishFlashing = getBoolean(object, "legendaryFishFlashing", legendaryFishFlashing);
        legendaryFishChance = clampDouble(object, "legendaryFishChance", legendaryFishChance, 0, 1);
    }

    private static void loadClient(Path path) {
        JsonObject object = readOrCreate(path, "client");
        isolateAudioCues = getBoolean(object, "isolateAudioCues", isolateAudioCues);
        pauseDuringMinigame = getBoolean(object, "pauseDuringMinigame", pauseDuringMinigame);
    }

    private static JsonObject readOrCreate(Path path, String kind) {
        if (Files.exists(path)) {
            try {
                JsonObject object = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
                if (object != null) {
                    return object;
                }
            } catch (IOException | RuntimeException e) {
                StardewFishing.LOGGER.error("Failed to read {} config at {}, using defaults", kind, path, e);
            }
        }
        JsonObject object = new JsonObject();
        writeDefaults(path, object, kind);
        return object;
    }

    private static void writeDefaults(Path path, JsonObject object, String kind) {
        if (object.size() == 0) {
            writeTo(object, kind);
        }
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, GSON.toJson(object), StandardCharsets.UTF_8);
        } catch (IOException e) {
            StardewFishing.LOGGER.error("Failed to write default config to {}", path, e);
        }
    }

    private static void writeTo(JsonObject object, String name) {
        switch (name) {
            case "server" -> {
                object.addProperty("quality1Threshold", quality1Threshold);
                object.addProperty("quality2Threshold", quality2Threshold);
                object.addProperty("quality3Threshold", quality3Threshold);
                object.addProperty("quality1Multiplier", quality1Multiplier);
                object.addProperty("quality2Multiplier", quality2Multiplier);
                object.addProperty("quality3Multiplier", quality3Multiplier);
                object.addProperty("biteTimeMultiplier", biteTimeMultiplier);
                object.addProperty("treasureChestChance", treasureChestChance);
                object.addProperty("goldenChestChance", goldenChestChance);
                object.addProperty("inventoryBobberEquipping", inventoryBobberEquipping);
                object.addProperty("legendaryFishFlashing", legendaryFishFlashing);
                object.addProperty("legendaryFishChance", legendaryFishChance);
            }
            case "client" -> {
                object.addProperty("isolateAudioCues", isolateAudioCues);
                object.addProperty("pauseDuringMinigame", pauseDuringMinigame);
            }
            default -> throw new IllegalArgumentException("Unknown config kind " + name);
        }
    }

    private static double clampDouble(JsonObject object, String key, double fallback, double min, double max) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive() || !object.get(key).getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        return Math.max(min, Math.min(max, object.get(key).getAsDouble()));
    }

    private static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive() || !object.get(key).getAsJsonPrimitive().isBoolean()) {
            return fallback;
        }
        return object.get(key).getAsBoolean();
    }

    public static int getQuality(double accuracy) {
        if (accuracy >= quality3Threshold) {
            return 3;
        } else if (accuracy >= quality2Threshold) {
            return 2;
        } else if (accuracy >= quality1Threshold) {
            return 1;
        }
        return 0;
    }

    public static double getMultiplier(double accuracy, double expMultiplierStat) {
        double multiplier = switch (getQuality(accuracy)) {
            case 3 -> quality3Multiplier;
            case 2 -> quality2Multiplier;
            case 1 -> quality1Multiplier;
            default -> 1;
        };
        multiplier *= expMultiplierStat;
        return multiplier;
    }

    public static double getBiteTimeMultiplier() {
        return biteTimeMultiplier;
    }

    public static double getTreasureChestChance() {
        return treasureChestChance;
    }

    public static double getGoldenChestChance() {
        return goldenChestChance;
    }

    public static boolean isInventoryEquippingEnabled() {
        return inventoryBobberEquipping;
    }

    public static boolean isLegendaryFlashingEnabled() {
        return legendaryFishFlashing;
    }

    public static float getLegendaryFishChance(float luck) {
        return (float) (legendaryFishChance + luck * 0.01F);
    }

    public static boolean isolateAudioCues() {
        return isolateAudioCues;
    }

    public static boolean pauseDuringMinigame() {
        return pauseDuringMinigame;
    }
}
