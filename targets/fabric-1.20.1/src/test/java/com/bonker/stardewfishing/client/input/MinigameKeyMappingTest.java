package com.bonker.stardewfishing.client.input;

import com.bonker.stardewfishing.client.StardewFishingClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinigameKeyMappingTest {
    @Test
    void minigameLeftMouseBindingDoesNotConflictWithVanillaAttack() {
        KeyMapping attack = new KeyMapping(
                "key.attack",
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_1,
                KeyMapping.CATEGORY_GAMEPLAY);

        KeyMapping minigame = StardewFishingClient.MINIGAME_BUTTON;

        assertTrue(minigame.matchesMouse(GLFW.GLFW_MOUSE_BUTTON_1));
        assertFalse(
                minigame.same(attack) || attack.same(minigame),
                "The GUI-only fishing input must not conflict with the in-game attack/break binding");

        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1));
        assertTrue(attack.consumeClick(),
                "Left mouse input must still reach the vanilla attack/break binding outside the minigame screen");
        assertFalse(minigame.consumeClick(),
                "The minigame binding must only be driven by FishingScreen input callbacks");
    }
}
