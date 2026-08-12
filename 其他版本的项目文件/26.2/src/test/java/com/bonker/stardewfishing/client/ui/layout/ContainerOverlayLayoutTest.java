package com.bonker.stardewfishing.client.ui.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerOverlayLayoutTest {
    @Test
    void translatesContainerCoordinatesToScreenCoordinates() {
        assertEquals(661, ContainerOverlayLayout.screenX(27, 634));
        assertEquals(191, ContainerOverlayLayout.screenY(8, 183));
    }
}
