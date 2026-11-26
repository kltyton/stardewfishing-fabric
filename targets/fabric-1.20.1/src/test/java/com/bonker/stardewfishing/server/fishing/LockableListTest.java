package com.bonker.stardewfishing.server.fishing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LockableListTest {
    @Test
    void unlockedListBehavesLikeArrayList() {
        LockableList<String> list = new LockableList<>();
        assertTrue(list.add("a"));
        list.add(0, "b");
        assertEquals(List.of("b", "a"), list);
        assertTrue(list.remove("a"));
        list.add("c");
        assertEquals("b", list.remove(0));
        assertEquals(List.of("c"), list);
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    void lockedListBlocksMutationsUntilUnlocked() {
        LockableList<String> list = new LockableList<>();
        list.add("a");
        list.add("b");
        list.lock();

        assertFalse(list.add("c"));
        list.add(0, "d");
        assertNull(list.set(0, "e"));
        assertNull(list.remove(0));
        assertFalse(list.remove("a"));
        assertFalse(list.addAll(List.of("x")));
        assertFalse(list.removeAll(List.of("a")));
        assertFalse(list.retainAll(List.of("a")));
        assertFalse(list.removeIf(value -> true));
        list.replaceAll(String::toUpperCase);
        list.sort(String::compareTo);
        list.clear();

        assertEquals(List.of("a", "b"), list);
        assertTrue(list.isLocked());

        list.unlock();
        assertTrue(list.add("c"));
        assertEquals(List.of("a", "b", "c"), list);
    }
}
