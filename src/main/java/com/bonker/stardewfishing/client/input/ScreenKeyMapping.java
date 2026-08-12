package com.bonker.stardewfishing.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A configurable key mapping that is matched by a {@code Screen} only and is
 * therefore excluded from Minecraft's global in-game key dispatch table.
 */
public final class ScreenKeyMapping extends KeyMapping {
    private final InputConstants.Key defaultScreenKey;
    private InputConstants.Key screenKey;

    public ScreenKeyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        super(name, InputConstants.UNKNOWN.getValue(), category);
        defaultScreenKey = type.getOrCreate(keyCode);
        screenKey = defaultScreenKey;
    }

    @Override
    @NotNull
    public InputConstants.Key getDefaultKey() {
        return defaultScreenKey;
    }

    @Override
    public void setKey(InputConstants.Key key) {
        screenKey = key;
    }

    @Override
    public boolean same(KeyMapping other) {
        return other instanceof ScreenKeyMapping screenMapping && screenKey.equals(screenMapping.screenKey);
    }

    @Override
    public boolean isUnbound() {
        return screenKey.equals(InputConstants.UNKNOWN);
    }

    @Override
    public boolean matches(int keyCode, int scanCode) {
        return keyCode == InputConstants.UNKNOWN.getValue()
                ? screenKey.getType() == InputConstants.Type.SCANCODE && screenKey.getValue() == scanCode
                : screenKey.getType() == InputConstants.Type.KEYSYM && screenKey.getValue() == keyCode;
    }

    @Override
    public boolean matchesMouse(int button) {
        return screenKey.getType() == InputConstants.Type.MOUSE && screenKey.getValue() == button;
    }

    @Override
    @NotNull
    public Component getTranslatedKeyMessage() {
        return screenKey.getDisplayName();
    }

    @Override
    public boolean isDefault() {
        return screenKey.equals(defaultScreenKey);
    }

    @Override
    @NotNull
    public String saveString() {
        return screenKey.getName();
    }
}
