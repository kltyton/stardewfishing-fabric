package com.bonker.stardewfishing.client.ui.layout;

public final class ContainerOverlayLayout {
    private ContainerOverlayLayout() {
    }

    public static int screenX(int localX, int containerX) {
        return containerX + localX;
    }

    public static int screenY(int localY, int containerY) {
        return containerY + localY;
    }
}
