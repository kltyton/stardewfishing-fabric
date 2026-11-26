package com.bonker.stardewfishing;

import com.bonker.stardewfishing.server.fishing.LockableList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LockableListTest {
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
    void lockedListBlocksMutations() {
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
        assertFalse(list.removeIf(s -> true));
        list.clear();

        assertEquals(List.of("a", "b"), list);
        assertTrue(list.isLocked());

        list.unlock();
        assertTrue(list.add("c"));
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void lockedListKeepsReadsAndIteration() {
        LockableList<String> list = new LockableList<>();
        list.add("a");
        list.add("b");
        list.lock();

        assertEquals("a", list.get(0));
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch("b"::equals));
    }
}
