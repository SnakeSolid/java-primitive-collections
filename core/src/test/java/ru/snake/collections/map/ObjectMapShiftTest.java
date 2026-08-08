package ru.snake.collections.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for backward-shift deletion in ObjectMap.
 *
 * <p>Uses Integer keys whose hash codes are chosen so that probe chains
 * form reliably (same pattern as IntSetShiftTest).</p>
 */
class ObjectMapShiftTest {

    @Test
    void removeFromMiddleOfProbeChainKeepsSiblings() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");
        map.put(0x80, "4");

        assertEquals("2", map.remove(0x40));
        assertEquals(3, map.size());

        assertEquals("1", map.get(0x20));
        assertEquals("3", map.get(0x60));
        assertEquals("4", map.get(0x80));
        assertNull(map.get(0x40));
    }

    @Test
    void removeFromBeginningOfProbeChainShiftsBack() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");

        assertEquals("1", map.remove(0x20));
        assertEquals(2, map.size());

        assertEquals("2", map.get(0x40));
        assertEquals("3", map.get(0x60));
    }

    @Test
    void removeFromEndOfProbeChainDoesNotDisruptOthers() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");

        assertEquals("3", map.remove(0x60));
        assertEquals(2, map.size());
        assertEquals("1", map.get(0x20));
        assertEquals("2", map.get(0x40));
    }

    // ------------------------------------------------------------------
    // Split key chains
    // ------------------------------------------------------------------

    @Test
    void splitKeyChainsSurviveRemovals() {
        ObjectMap<Integer, String> map = new ObjectMap<>(16);

        map.put(32, "1");
        map.put(64, "2");
        map.put(96, "3");
        map.put(128, "4");
        map.put(160, "5");
        map.put(192, "6");
        map.put(224, "7");
        map.put(256, "8");

        assertEquals("2", map.remove(64));
        assertEquals("4", map.remove(128));
        assertEquals("6", map.remove(192));
        assertEquals(5, map.size());

        assertEquals("1", map.get(32));
        assertEquals("3", map.get(96));
        assertEquals("5", map.get(160));
        assertEquals("7", map.get(224));
        assertEquals("8", map.get(256));
    }

    @Test
    void backwardShiftDoesNotCorruptAfterMultipleRemovals() {
        ObjectMap<Integer, String> map = new ObjectMap<>(16);
        for (int i = 0; i < 20; i++) {
            map.put(32 + i * 32, String.valueOf(i));
        }
        assertEquals(20, map.size());

        for (int i = 0; i < 20; i++) {
            int key = 32 + i * 32;
            if (i % 3 == 0) {
                assertEquals(String.valueOf(i), map.remove(key));
            }
        }
        assertEquals(13, map.size());

        for (int i = 0; i < 20; i++) {
            int key = 32 + i * 32;
            if (i % 3 == 0) {
                assertNull(map.get(key));
            } else {
                assertEquals(String.valueOf(i), map.get(key));
            }
        }
    }

    // ------------------------------------------------------------------
    // Iterator + shift
    // ------------------------------------------------------------------

    @Test
    void iteratorReflectsShiftedChains() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");
        map.put(0x80, "4");

        assertEquals("2", map.remove(0x40));

        Set<Integer> collected = new HashSet<>();
        for (Integer k : map.keySet()) {
            collected.add(k);
        }
        assertEquals(3, collected.size());
        assertTrue(collected.contains(0x20));
        assertTrue(collected.contains(0x60));
        assertTrue(collected.contains(0x80));
    }

    @Test
    void entrySetIteratorReflectsShiftedChains() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "a");
        map.put(0x40, "b");
        map.put(0x60, "c");
        map.put(0x80, "d");

        assertEquals("b", map.remove(0x40));

        Set<Map.Entry<Integer, String>> entries = new HashSet<>();
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            entries.add(e);
        }
        assertEquals(3, entries.size());
        assertTrue(
            entries
                .stream()
                .anyMatch(
                    e -> e.getKey().equals(0x20) && e.getValue().equals("a")
                )
        );
        assertTrue(
            entries
                .stream()
                .anyMatch(
                    e -> e.getKey().equals(0x60) && e.getValue().equals("c")
                )
        );
        assertTrue(
            entries
                .stream()
                .anyMatch(
                    e -> e.getKey().equals(0x80) && e.getValue().equals("d")
                )
        );
    }

    // ------------------------------------------------------------------
    // View mutation + shift
    // ------------------------------------------------------------------

    @Test
    void keySetRemoveTriggersShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");

        assertTrue(map.keySet().remove(0x40));
        assertEquals(2, map.size());
        assertEquals("1", map.get(0x20));
        assertEquals("3", map.get(0x60));
        assertNull(map.get(0x40));
    }

    @Test
    void entrySetRemoveTriggersShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "10");
        map.put(0x40, "20");
        map.put(0x60, "30");

        Map.Entry<Integer, String> entry = Map.entry(0x40, "20");
        assertTrue(map.entrySet().remove(entry));
        assertEquals(2, map.size());
        assertEquals("10", map.get(0x20));
        assertEquals("30", map.get(0x60));
        assertNull(map.get(0x40));
    }

    // ------------------------------------------------------------------
    // Add after removal
    // ------------------------------------------------------------------

    @Test
    void addAfterRemovalReusesShiftedSlots() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "1");
        map.put(0x40, "2");
        map.put(0x60, "3");

        assertEquals("2", map.remove(0x40));
        map.put(0x40, "42");
        assertEquals(3, map.size());

        assertEquals("1", map.get(0x20));
        assertEquals("42", map.get(0x40));
        assertEquals("3", map.get(0x60));
    }

    // ------------------------------------------------------------------
    // Wrap-around shift
    // ------------------------------------------------------------------

    @Test
    void shiftBackWorksAcrossTableWrapAround() {
        ObjectMap<Integer, String> map = new ObjectMap<>(16);

        for (int i = 0; i < 12; i++) {
            map.put(i * 16, String.valueOf(i));
        }
        assertEquals(12, map.size());

        for (int i = 0; i < 12; i++) {
            assertEquals(String.valueOf(i), map.get(i * 16));
        }

        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0) {
                assertEquals(String.valueOf(i), map.remove(i * 16));
            }
        }
        assertEquals(6, map.size());

        for (int i = 0; i < 12; i++) {
            int key = i * 16;
            if (i % 2 == 0) {
                assertNull(map.get(key));
            } else {
                assertEquals(String.valueOf(i), map.get(key));
            }
        }
    }

    // ------------------------------------------------------------------
    // Functional operations + shift
    // ------------------------------------------------------------------

    @Test
    void computeIfPresentRemovalTriggersShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "a");
        map.put(0x40, "b");
        map.put(0x60, "c");

        assertNull(map.computeIfPresent(0x40, (k, v) -> null));
        assertEquals(2, map.size());
        assertEquals("a", map.get(0x20));
        assertEquals("c", map.get(0x60));
    }

    @Test
    void computeRemovalTriggersShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "a");
        map.put(0x40, "b");
        map.put(0x60, "c");

        assertNull(map.compute(0x40, (k, v) -> null));
        assertEquals(2, map.size());
        assertEquals("a", map.get(0x20));
        assertEquals("c", map.get(0x60));
    }

    @Test
    void mergeRemovalTriggersShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(8);
        map.put(0x20, "a");
        map.put(0x40, "b");
        map.put(0x60, "c");

        assertNull(map.merge(0x40, "x", (v, nv) -> null));
        assertEquals(2, map.size());
        assertEquals("a", map.get(0x20));
        assertEquals("c", map.get(0x60));
    }

    // ------------------------------------------------------------------
    // Stress — remove then re-add
    // ------------------------------------------------------------------

    @Test
    void stressRemoveAndReAddAfterShift() {
        ObjectMap<Integer, String> map = new ObjectMap<>(32);
        for (int round = 0; round < 5; round++) {
            for (int i = 0; i < 50; i++) {
                map.put(64 + i, "v" + i);
            }
            assertEquals(50, map.size());

            // Remove half
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    assertEquals("v" + i, map.remove(64 + i));
                }
            }
            assertEquals(25, map.size());

            // Verify remaining
            for (int i = 0; i < 50; i++) {
                int key = 64 + i;
                if (i % 2 == 1) {
                    assertNull(map.get(key));
                } else {
                    assertEquals("v" + i, map.get(key));
                }
            }

            // Re-add the removed ones
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    map.put(64 + i, "x" + i);
                }
            }
            assertEquals(50, map.size());

            // Everything should be there
            for (int i = 0; i < 50; i++) {
                assertNotNull(
                    map.get(64 + i),
                    "round " + round + " should contain " + (64 + i)
                );
            }

            map.clear();
        }
        assertEquals(0, map.size());
    }
}
