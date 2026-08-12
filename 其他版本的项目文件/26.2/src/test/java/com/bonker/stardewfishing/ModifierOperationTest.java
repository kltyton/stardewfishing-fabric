package com.bonker.stardewfishing;

import com.bonker.stardewfishing.server.resource.ModifierOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModifierOperationTest {
    @Test
    void parsesValidOperations() {
        assertEquals(new ModifierOperation(ModifierOperation.Type.ADDITION, 1.5), ModifierOperation.parse("+1.5"));
        assertEquals(new ModifierOperation(ModifierOperation.Type.SUBTRACTION, 2), ModifierOperation.parse("-2"));
        assertEquals(new ModifierOperation(ModifierOperation.Type.MULTIPLICATION, 3), ModifierOperation.parse("x3"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("default"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("+0"));
        assertEquals(ModifierOperation.DEFAULT, ModifierOperation.parse("x1"));
    }

    @Test
    void rejectsInvalidOperations() {
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("1.5"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("y2"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("+"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("+a"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("--1"));
        assertThrows(ModifierOperation.ModifierOperationException.class, () -> ModifierOperation.parse("x"));
    }

    @Test
    void appliesOperations() {
        assertEquals(5.5, ModifierOperation.parse("+1.5").apply(4.0), 1e-9);
        assertEquals(2.0, ModifierOperation.parse("-2").apply(4.0), 1e-9);
        assertEquals(12.0, ModifierOperation.parse("x3").apply(4.0), 1e-9);
        assertEquals(4.0, ModifierOperation.DEFAULT.apply(4.0), 1e-9);
        assertEquals(7, ModifierOperation.parse("+2").apply(5));
        assertEquals(2.5F, ModifierOperation.parse("x2").apply(1.25F), 1e-6F);
    }

    @Test
    void mergesOperations() {
        ModifierOperation add = ModifierOperation.parse("+1");
        ModifierOperation add2 = ModifierOperation.parse("+2");
        ModifierOperation mul = ModifierOperation.parse("x2");

        assertEquals(ModifierOperation.parse("+3"), add.merge(add2));
        assertEquals(ModifierOperation.parse("x4"), mul.merge(mul));
        assertEquals(add, add.merge(ModifierOperation.DEFAULT));
        assertEquals(mul, ModifierOperation.DEFAULT.merge(mul));
    }

    @Test
    void exposesSemantics() {
        assertTrue(ModifierOperation.parse("+1").isPositive());
        assertFalse(ModifierOperation.parse("-1").isPositive());
        assertTrue(ModifierOperation.parse("x2").isPositive());
        assertFalse(ModifierOperation.parse("x0.5").isPositive());
        assertFalse(ModifierOperation.DEFAULT.matters());
        assertTrue(ModifierOperation.parse("+1").matters());
    }
}
