package com.bonker.stardewfishing.server.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModifierOperationTest {
    @Test
    void parsesAndFormatsValidOperations() {
        assertEquals(new ModifierOperation(ModifierOperation.Type.ADDITION, 1.5), ModifierOperation.parse("+1.5"));
        assertEquals(new ModifierOperation(ModifierOperation.Type.SUBTRACTION, 2), ModifierOperation.parse("-2"));
        assertEquals(new ModifierOperation(ModifierOperation.Type.MULTIPLICATION, 3), ModifierOperation.parse("x3"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("default"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("+0"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("x1"));
        assertEquals("x1.5", ModifierOperation.parse("x1.5").toString());
    }

    @Test
    void rejectsMalformedOrNonFiniteOperations() {
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("1.5"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("y2"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("+"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("+a"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("--1"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("xNaN"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("xInfinity"));
    }

    @Test
    void appliesAndMergesOperations() {
        assertEquals(5.5, ModifierOperation.parse("+1.5").apply(4.0), 1e-9);
        assertEquals(2.0, ModifierOperation.parse("-2").apply(4.0), 1e-9);
        assertEquals(12.0, ModifierOperation.parse("x3").apply(4.0), 1e-9);
        assertEquals(7, ModifierOperation.parse("+2").apply(5));
        assertEquals(2.5F, ModifierOperation.parse("x2").apply(1.25F), 1e-6F);
        assertEquals(ModifierOperation.parse("+3"), ModifierOperation.parse("+1").merge(ModifierOperation.parse("+2")));
        assertEquals(ModifierOperation.parse("x4"), ModifierOperation.parse("x2").merge(ModifierOperation.parse("x2")));
    }

    @Test
    void exposesModifierSemantics() {
        assertTrue(ModifierOperation.parse("+1").isPositive());
        assertFalse(ModifierOperation.parse("-1").isPositive());
        assertTrue(ModifierOperation.parse("x2").isPositive());
        assertFalse(ModifierOperation.parse("x0.5").isPositive());
        assertFalse(ModifierOperation.DEFAULT.matters());
        assertTrue(ModifierOperation.parse("+1").matters());
    }
}
