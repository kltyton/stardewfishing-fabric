package com.bonker.stardewfishing.client.ui.layout;

public final class ContainerOverlayLayout {
    private ContainerOverlayLayout() {
    }

    public static int screenX(int localX, int leftPos) {
        return leftPos + localX;
    }

    public static int screenY(int localY, int topPos) {
        return topPos + localY;
    }
}
