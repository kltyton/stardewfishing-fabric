package com.bonker.stardewfishing.client.input;

import com.bonker.stardewfishing.client.StardewFishingClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Regression test for the 1.20.1 "Fix minigame key conflict" (b661f3d) port.
 * The minigame binding defaults to left mouse, which must not conflict with,
 * nor consume clicks intended for, the vanilla in-game attack/break binding.
 */
public final class MinigameKeyMappingTest {
    private MinigameKeyMappingTest() {
    }

    public static void main(String[] args) {
        KeyMapping attack = new KeyMapping(
                "key.attack",
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_1,
                KeyMapping.CATEGORY_GAMEPLAY);

        KeyMapping minigame = StardewFishingClient.MINIGAME_BUTTON;

        assertTrue(
                minigame.matchesMouse(GLFW.GLFW_MOUSE_BUTTON_1),
                "Left mouse must still drive the fishing minigame inside FishingScreen");
        assertFalse(
                minigame.same(attack) || attack.same(minigame),
                "The GUI-only fishing input must not conflict with the in-game attack/break binding");

        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1));
        assertTrue(
                attack.consumeClick(),
                "Left mouse input must still reach the vanilla attack/break binding outside the minigame screen");
        assertFalse(
                minigame.consumeClick(),
                "The minigame binding must only be driven by FishingScreen input callbacks");

        System.out.println("MinigameKeyMappingTest PASSED");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
