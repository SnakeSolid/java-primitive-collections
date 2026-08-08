package ru.snake.collections.set;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for backward-shift deletion in IntSet.
 *
 * <p>These replace the old tombstone-specific tests and verify that probe
 * chains stay intact when elements are removed from the middle of chains.</p>
 */
class IntSetShiftTest {

    @Test
    void removeFromMiddleOfProbeChainKeepsSiblings() {
        IntSet set = new IntSet(8);
        set.add(0x20);
        set.add(0x40);
        set.add(0x60);
        set.add(0x80);

        assertTrue(set.remove(0x40));
        assertEquals(3, set.size());

        assertTrue(set.contains(0x20));
        assertTrue(set.contains(0x60));
        assertTrue(set.contains(0x80));
        assertFalse(set.contains(0x40));
    }

    @Test
    void removeFromBeginningOfProbeChainShiftsBack() {
        IntSet set = new IntSet(8);
        set.add(0x20);
        set.add(0x40);
        set.add(0x60);

        assertTrue(set.remove(0x20));
        assertEquals(2, set.size());

        assertTrue(set.contains(0x40));
        assertTrue(set.contains(0x60));
        assertFalse(set.contains(0x20));
    }

    @Test
    void removeFromEndOfProbeChainDoesNotDisruptOthers() {
        IntSet set = new IntSet(8);
        set.add(0x20);
        set.add(0x40);
        set.add(0x60);

        assertTrue(set.remove(0x60));
        assertEquals(2, set.size());
        assertTrue(set.contains(0x20));
        assertTrue(set.contains(0x40));
    }

    // ------------------------------------------------------------------
    // Split key chains
    // ------------------------------------------------------------------

    @Test
    void splitKeyChainsSurviveRemovals() {
        IntSet set = new IntSet(16);

        set.add(32);
        set.add(64);
        set.add(96);
        set.add(128);
        set.add(160);
        set.add(192);
        set.add(224);
        set.add(256);

        assertTrue(set.remove(64));
        assertTrue(set.remove(128));
        assertTrue(set.remove(192));
        assertEquals(5, set.size());

        assertTrue(set.contains(32));
        assertTrue(set.contains(96));
        assertTrue(set.contains(160));
        assertTrue(set.contains(224));
        assertTrue(set.contains(256));

        assertFalse(set.contains(64));
        assertFalse(set.contains(128));
        assertFalse(set.contains(192));
    }

    @Test
    void splitKeyChainsWithPackedSlots() {
        IntSet set = new IntSet(16);

        set.add(0);
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        set.add(32);
        set.add(64);
        set.add(96);

        assertEquals(8, set.size());

        assertTrue(set.remove(1));
        assertTrue(set.remove(3));
        assertEquals(6, set.size());

        assertTrue(set.contains(0));
        assertTrue(set.contains(2));
        assertTrue(set.contains(4));
        assertTrue(set.contains(32));
        assertTrue(set.contains(64));
        assertTrue(set.contains(96));
    }

    @Test
    void backwardShiftDoesNotCorruptAfterMultipleRemovals() {
        IntSet set = new IntSet(16);
        for (int i = 0; i < 20; i++) {
            set.add(32 + i * 32);
        }
        assertEquals(20, set.size());

        for (int i = 0; i < 20; i++) {
            int val = 32 + i * 32;
            if (i % 3 == 0) {
                assertTrue(set.remove(val));
            }
        }
        assertEquals(13, set.size());

        for (int i = 0; i < 20; i++) {
            int val = 32 + i * 32;
            if (i % 3 == 0) {
                assertFalse(set.contains(val), "should not contain " + val);
            } else {
                assertTrue(set.contains(val), "should contain " + val);
            }
        }
    }

    @Test
    void backwardShiftAfterRemovingAllPackedElements() {
        IntSet set = new IntSet(8);
        for (int i = 0; i < 32; i++) {
            set.add(i);
        }
        set.add(32);
        set.add(64);

        for (int i = 0; i < 32; i++) {
            assertTrue(set.remove(i));
        }
        assertEquals(2, set.size());
        assertTrue(set.contains(32));
        assertTrue(set.contains(64));
    }

    // ------------------------------------------------------------------
    // Iterator + shift
    // ------------------------------------------------------------------

    @Test
    void iteratorReflectsShiftedChains() {
        IntSet set = new IntSet(8);
        set.add(0x20);
        set.add(0x40);
        set.add(0x60);
        set.add(0x80);

        assertTrue(set.remove(0x40));

        Set<Integer> collected = new HashSet<>();
        for (Integer e : set) {
            collected.add(e);
        }
        assertEquals(3, collected.size());
        assertTrue(collected.contains(0x20));
        assertTrue(collected.contains(0x60));
        assertTrue(collected.contains(0x80));
    }

    // ------------------------------------------------------------------
    // retainAll + shift
    // ------------------------------------------------------------------

    @Test
    void retainAllTriggersBackwardShift() {
        IntSet set = new IntSet(8);
        set.add(32);
        set.add(64);
        set.add(96);
        set.add(128);
        set.add(160);

        assertTrue(set.retainAll(Set.of(32, 96, 160)));
        assertEquals(3, set.size());
        assertTrue(set.contains(32));
        assertTrue(set.contains(96));
        assertTrue(set.contains(160));
        assertFalse(set.contains(64));
        assertFalse(set.contains(128));
    }

    // ------------------------------------------------------------------
    // Add after removal
    // ------------------------------------------------------------------

    @Test
    void addAfterRemovalReusesShiftedSlots() {
        IntSet set = new IntSet(8);
        set.add(0x20);
        set.add(0x40);
        set.add(0x60);

        assertTrue(set.remove(0x40));
        assertTrue(set.add(0x40));
        assertEquals(3, set.size());

        assertTrue(set.contains(0x20));
        assertTrue(set.contains(0x40));
        assertTrue(set.contains(0x60));
    }

    // ------------------------------------------------------------------
    // Wrap-around shift
    // ------------------------------------------------------------------

    @Test
    void shiftBackWorksAcrossTableWrapAround() {
        IntSet set = new IntSet(16);

        // Find values whose hashes land near the end of the table so that
        // probe chains wrap around.
        // hash(0) = 0, hash(16) = 16 (out of bounds for size-16, wraps to 0)
        // We'll brute-force insert enough elements to trigger wrapping.
        for (int i = 0; i < 12; i++) {
            set.add(i * 16);
        }
        assertEquals(12, set.size());

        // Verify all present
        for (int i = 0; i < 12; i++) {
            assertTrue(set.contains(i * 16), "should contain " + i * 16);
        }

        // Remove from the middle and verify the rest
        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0) {
                assertTrue(set.remove(i * 16));
            }
        }
        assertEquals(6, set.size());

        for (int i = 0; i < 12; i++) {
            int val = i * 16;
            if (i % 2 == 0) {
                assertFalse(set.contains(val), "should not contain " + val);
            } else {
                assertTrue(set.contains(val), "should contain " + val);
            }
        }
    }

    // ------------------------------------------------------------------
    // Stress — remove then re-add
    // ------------------------------------------------------------------

    @Test
    void stressRemoveAndReAddAfterShift() {
        IntSet set = new IntSet(32);
        for (int round = 0; round < 5; round++) {
            for (int i = 0; i < 50; i++) {
                set.add(64 + i);
            }
            assertEquals(50, set.size());

            // Remove half
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    assertTrue(set.remove(64 + i));
                }
            }
            assertEquals(25, set.size());

            // Verify remaining
            for (int i = 0; i < 50; i++) {
                int val = 64 + i;
                if (i % 2 == 1) {
                    assertFalse(set.contains(val));
                } else {
                    assertTrue(set.contains(val));
                }
            }

            // Re-add the removed ones
            for (int i = 0; i < 50; i++) {
                if (i % 2 == 1) {
                    set.add(64 + i);
                }
            }
            assertEquals(50, set.size());

            // Everything should be there
            for (int i = 0; i < 50; i++) {
                assertTrue(
                    set.contains(64 + i),
                    "round " + round + " should contain " + (64 + i)
                );
            }

            set.clear();
        }
        assertEquals(0, set.size());
    }
}
