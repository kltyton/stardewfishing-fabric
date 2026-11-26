package com.bonker.stardewfishing.client.ui.layout;

public final class ContainerOverlayLayoutTest {
    private ContainerOverlayLayoutTest() {
    }

    public static void main(String[] args) {
        assertEquals(661, ContainerOverlayLayout.screenX(27, 634), "slot x must include container leftPos");
        assertEquals(191, ContainerOverlayLayout.screenY(8, 183), "slot y must include container topPos");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
