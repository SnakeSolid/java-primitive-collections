package ru.snake.collections.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for backward-shift deletion in ObjectToIntMap.
 *
 * <p>Uses Integer keys whose hash codes are chosen so that probe chains
 * form reliably (same pattern as IntSetShiftTest).</p>
 */
class ObjectToIntMapShiftTest {

    @Test
    void removeFromMiddleOfProbeChainKeepsSiblings() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);
        map.putInt(0x80, 4);

        assertEquals(2, map.delete(0x40));
        assertEquals(3, map.size());

        assertEquals(1, map.getInt(0x20));
        assertEquals(3, map.getInt(0x60));
        assertEquals(4, map.getInt(0x80));
        assertFalse(map.hasKey(0x40));
    }

    @Test
    void removeFromBeginningOfProbeChainShiftsBack() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertEquals(1, map.delete(0x20));
        assertEquals(2, map.size());

        assertEquals(2, map.getInt(0x40));
        assertEquals(3, map.getInt(0x60));
        assertFalse(map.hasKey(0x20));
    }

    @Test
    void removeFromEndOfProbeChainDoesNotDisruptOthers() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertEquals(3, map.delete(0x60));
        assertEquals(2, map.size());
        assertEquals(1, map.getInt(0x20));
        assertEquals(2, map.getInt(0x40));
    }

    // ------------------------------------------------------------------
    // Split key chains
    // ------------------------------------------------------------------

    @Test
    void splitKeyChainsSurviveRemovals() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(16);

        map.putInt(32, 1);
        map.putInt(64, 2);
        map.putInt(96, 3);
        map.putInt(128, 4);
        map.putInt(160, 5);
        map.putInt(192, 6);
        map.putInt(224, 7);
        map.putInt(256, 8);

        assertEquals(2, map.delete(64));
        assertEquals(4, map.delete(128));
        assertEquals(6, map.delete(192));
        assertEquals(5, map.size());

        assertEquals(1, map.getInt(32));
        assertEquals(3, map.getInt(96));
        assertEquals(5, map.getInt(160));
        assertEquals(7, map.getInt(224));
        assertEquals(8, map.getInt(256));
    }

    @Test
    void backwardShiftDoesNotCorruptAfterMultipleRemovals() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(16);
        for (int i = 0; i < 20; i++) {
            map.putInt(32 + i * 32, i);
        }
        assertEquals(20, map.size());

        for (int i = 0; i < 20; i++) {
            int key = 32 + i * 32;
            if (i % 3 == 0) {
                assertEquals(i, map.delete(key));
            }
        }
        assertEquals(13, map.size());

        for (int i = 0; i < 20; i++) {
            int key = 32 + i * 32;
            if (i % 3 == 0) {
                assertFalse(map.hasKey(key), "should not contain " + key);
            } else {
                assertEquals(
                    i,
                    map.getInt(key),
                    "should have value " + i + " for key " + key
                );
            }
        }
    }

    // ------------------------------------------------------------------
    // Iterator + shift
    // ------------------------------------------------------------------

    @Test
    void iteratorReflectsShiftedChains() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);
        map.putInt(0x80, 4);

        assertEquals(2, map.delete(0x40));

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
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 100);
        map.putInt(0x40, 200);
        map.putInt(0x60, 300);
        map.putInt(0x80, 400);

        assertEquals(200, map.delete(0x40));

        Set<Map.Entry<Integer, Integer>> entries = new HashSet<>();
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            entries.add(e);
        }
        assertEquals(3, entries.size());
        assertTrue(
            entries
                .stream()
                .anyMatch(e -> e.getKey().equals(0x20) && e.getValue() == 100)
        );
        assertTrue(
            entries
                .stream()
                .anyMatch(e -> e.getKey().equals(0x60) && e.getValue() == 300)
        );
        assertTrue(
            entries
                .stream()
                .anyMatch(e -> e.getKey().equals(0x80) && e.getValue() == 400)
        );
    }

    // ------------------------------------------------------------------
    // View mutation + shift
    // ------------------------------------------------------------------

    @Test
    void keySetRemoveTriggersShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertTrue(map.keySet().remove(0x40));
        assertEquals(2, map.size());
        assertEquals(1, map.getInt(0x20));
        assertEquals(3, map.getInt(0x60));
        assertFalse(map.hasKey(0x40));
    }

    @Test
    void entrySetRemoveTriggersShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 10);
        map.putInt(0x40, 20);
        map.putInt(0x60, 30);

        Map.Entry<Integer, Integer> entry = Map.entry(0x40, 20);
        assertTrue(map.entrySet().remove(entry));
        assertEquals(2, map.size());
        assertEquals(10, map.getInt(0x20));
        assertEquals(30, map.getInt(0x60));
        assertFalse(map.hasKey(0x40));
    }

    // ------------------------------------------------------------------
    // Add after removal
    // ------------------------------------------------------------------

    @Test
    void addAfterRemovalReusesShiftedSlots() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertEquals(2, map.delete(0x40));
        map.putInt(0x40, 42);
        assertEquals(3, map.size());

        assertEquals(1, map.getInt(0x20));
        assertEquals(42, map.getInt(0x40));
        assertEquals(3, map.getInt(0x60));
    }

    // ------------------------------------------------------------------
    // Wrap-around shift
    // ------------------------------------------------------------------

    @Test
    void shiftBackWorksAcrossTableWrapAround() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(16);

        for (int i = 0; i < 12; i++) {
            map.putInt(i * 16, i);
        }
        assertEquals(12, map.size());

        for (int i = 0; i < 12; i++) {
            assertEquals(i, map.getInt(i * 16));
        }

        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0) {
                assertEquals(i, map.delete(i * 16));
            }
        }
        assertEquals(6, map.size());

        for (int i = 0; i < 12; i++) {
            int key = i * 16;
            if (i % 2 == 0) {
                assertFalse(map.hasKey(key), "should not contain " + key);
            } else {
                assertEquals(i, map.getInt(key), "should contain " + key);
            }
        }
    }

    // ------------------------------------------------------------------
    // Functional operations + shift
    // ------------------------------------------------------------------

    @Test
    void computeIfPresentRemovalTriggersShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertNull(map.computeIfPresent(0x40, (k, v) -> null));
        assertEquals(2, map.size());
        assertEquals(1, map.getInt(0x20));
        assertEquals(3, map.getInt(0x60));
    }

    @Test
    void computeRemovalTriggersShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertNull(map.compute(0x40, (k, v) -> null));
        assertEquals(2, map.size());
        assertEquals(1, map.getInt(0x20));
        assertEquals(3, map.getInt(0x60));
    }

    @Test
    void mergeRemovalTriggersShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(8);
        map.putInt(0x20, 1);
        map.putInt(0x40, 2);
        map.putInt(0x60, 3);

        assertNull(map.merge(0x40, 99, (v, nv) -> null));
        assertEquals(2, map.size());
        assertEquals(1, map.getInt(0x20));
        assertEquals(3, map.getInt(0x60));
    }

    // ------------------------------------------------------------------
    // Stress — remove then re-add
    // ------------------------------------------------------------------

    @Test
    void stressRemoveAndReAddAfterShift() {
        ObjectToIntMap<Integer> map = new ObjectToIntMap<>(32);
        for (int round = 0; round < 5; round++) {
            for (int i = 0; i < 50; i++) {
                map.putInt(64 + i, i);
            }
            assertEquals(50, map.size());

            // Remove half
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    assertEquals(i, map.delete(64 + i));
                }
            }
            assertEquals(25, map.size());

            // Verify remaining
            for (int i = 0; i < 50; i++) {
                int key = 64 + i;
                if (i % 2 == 1) {
                    assertFalse(map.hasKey(key));
                } else {
                    assertEquals(i, map.getInt(key));
                }
            }

            // Re-add the removed ones
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    map.putInt(64 + i, i * 10);
                }
            }
            assertEquals(50, map.size());

            // Everything should be there
            for (int i = 0; i < 50; i++) {
                assertTrue(
                    map.hasKey(64 + i),
                    "round " + round + " should contain " + (64 + i)
                );
            }

            map.clear();
        }
        assertEquals(0, map.size());
    }
}
