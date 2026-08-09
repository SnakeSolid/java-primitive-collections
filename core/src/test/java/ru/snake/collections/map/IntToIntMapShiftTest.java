package ru.snake.collections.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for backward-shift deletion in IntToIntMap.
 *
 * <p>
 * These verify that probe chains stay intact when entries are removed from the
 * middle of chains, and that the map remains consistent after repeated removals
 * and re-insertions.
 * </p>
 */
class IntToIntMapShiftTest {

	@Test
	void removeFromMiddleOfProbeChainKeepsSiblings() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);
		map.put(0x80, 4);

		assertEquals(2, map.remove(0x40));
		assertEquals(3, map.size());

		assertEquals(1, map.get(0x20));
		assertEquals(3, map.get(0x60));
		assertEquals(4, map.get(0x80));
		assertEquals(0, map.get(0x40));
		assertFalse(map.containsKey(0x40));
	}

	@Test
	void removeFromBeginningOfProbeChainShiftsBack() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertEquals(1, map.remove(0x20));
		assertEquals(2, map.size());

		assertEquals(2, map.get(0x40));
		assertEquals(3, map.get(0x60));
		assertFalse(map.containsKey(0x20));
	}

	@Test
	void removeFromEndOfProbeChainDoesNotDisruptOthers() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertEquals(3, map.remove(0x60));
		assertEquals(2, map.size());
		assertEquals(1, map.get(0x20));
		assertEquals(2, map.get(0x40));
	}

	// ------------------------------------------------------------------
	// Split key chains
	// ------------------------------------------------------------------

	@Test
	void splitKeyChainsSurviveRemovals() {
		IntToIntMap map = new IntToIntMap(16);

		map.put(32, 1);
		map.put(64, 2);
		map.put(96, 3);
		map.put(128, 4);
		map.put(160, 5);
		map.put(192, 6);
		map.put(224, 7);
		map.put(256, 8);

		assertEquals(2, map.remove(64));
		assertEquals(4, map.remove(128));
		assertEquals(6, map.remove(192));
		assertEquals(5, map.size());

		assertEquals(1, map.get(32));
		assertEquals(3, map.get(96));
		assertEquals(5, map.get(160));
		assertEquals(7, map.get(224));
		assertEquals(8, map.get(256));

		assertFalse(map.containsKey(64));
		assertFalse(map.containsKey(128));
		assertFalse(map.containsKey(192));
	}

	@Test
	void collidingKeysSurviveRemovals() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 10);
		map.put(0x40, 11);
		map.put(0x60, 12);
		map.put(0x80, 13);
		map.put(0xA0, 14);
		map.put(0xC0, 15);

		assertEquals(6, map.size());

		assertEquals(11, map.remove(0x40));
		assertEquals(13, map.remove(0x80));
		assertEquals(4, map.size());

		assertEquals(10, map.get(0x20));
		assertEquals(12, map.get(0x60));
		assertEquals(14, map.get(0xA0));
		assertEquals(15, map.get(0xC0));
	}

	@Test
	void backwardShiftDoesNotCorruptAfterMultipleRemovals() {
		IntToIntMap map = new IntToIntMap(16);
		for (int i = 0; i < 20; i++) {
			map.put(32 + i * 32, i);
		}
		assertEquals(20, map.size());

		for (int i = 0; i < 20; i++) {
			int key = 32 + i * 32;
			if (i % 3 == 0) {
				assertEquals(i, map.remove(key));
			}
		}
		assertEquals(13, map.size());

		for (int i = 0; i < 20; i++) {
			int key = 32 + i * 32;
			if (i % 3 == 0) {
				assertFalse(map.containsKey(key), "should not contain " + key);
			} else {
				assertEquals(i, map.get(key), "should have value " + i + " for key " + key);
			}
		}
	}

	// ------------------------------------------------------------------
	// Iterator + shift
	// ------------------------------------------------------------------

	@Test
	void iteratorReflectsShiftedChains() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);
		map.put(0x80, 4);

		assertEquals(2, map.remove(0x40));

		Set<Integer> collected = new HashSet<>();
		for (int k : map.keySet()) {
			collected.add(k);
		}
		assertEquals(3, collected.size());
		assertTrue(collected.contains(0x20));
		assertTrue(collected.contains(0x60));
		assertTrue(collected.contains(0x80));
	}

	@Test
	void entrySetIteratorReflectsShiftedChains() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 100);
		map.put(0x40, 200);
		map.put(0x60, 300);
		map.put(0x80, 400);

		assertEquals(200, map.remove(0x40));

		Set<Map.Entry<Integer, Integer>> entries = new HashSet<>();
		for (Map.Entry<Integer, Integer> e : map.entrySet()) {
			entries.add(e);
		}
		assertEquals(3, entries.size());
		assertTrue(entries.stream().anyMatch(e -> e.getKey() == 0x20 && e.getValue() == 100));
		assertTrue(entries.stream().anyMatch(e -> e.getKey() == 0x60 && e.getValue() == 300));
		assertTrue(entries.stream().anyMatch(e -> e.getKey() == 0x80 && e.getValue() == 400));
	}

	// ------------------------------------------------------------------
	// View mutation + shift
	// ------------------------------------------------------------------

	@Test
	void keySetRemoveTriggersShift() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertTrue(map.keySet().remove((Object) 0x40));
		assertEquals(2, map.size());
		assertEquals(1, map.get(0x20));
		assertEquals(3, map.get(0x60));
		assertFalse(map.containsKey(0x40));
	}

	@Test
	void entrySetRemoveTriggersShift() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 10);
		map.put(0x40, 20);
		map.put(0x60, 30);

		Map.Entry<Integer, Integer> entry = Map.entry(0x40, 20);
		assertTrue(map.entrySet().remove(entry));
		assertEquals(2, map.size());
		assertEquals(10, map.get(0x20));
		assertEquals(30, map.get(0x60));
		assertFalse(map.containsKey(0x40));
	}

	// ------------------------------------------------------------------
	// Add after removal
	// ------------------------------------------------------------------

	@Test
	void addAfterRemovalReusesShiftedSlots() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertEquals(2, map.remove(0x40));
		map.put(0x40, 42);
		assertEquals(3, map.size());

		assertEquals(1, map.get(0x20));
		assertEquals(42, map.get(0x40));
		assertEquals(3, map.get(0x60));
	}

	// ------------------------------------------------------------------
	// Wrap-around shift
	// ------------------------------------------------------------------

	@Test
	void shiftBackWorksAcrossTableWrapAround() {
		IntToIntMap map = new IntToIntMap(16);

		for (int i = 0; i < 12; i++) {
			map.put(i * 16, i);
		}
		assertEquals(12, map.size());

		for (int i = 0; i < 12; i++) {
			assertEquals(i, map.get(i * 16), "should contain " + i * 16);
		}

		for (int i = 0; i < 12; i++) {
			if (i % 2 == 0) {
				assertEquals(i, map.remove(i * 16));
			}
		}
		assertEquals(6, map.size());

		for (int i = 0; i < 12; i++) {
			int key = i * 16;
			if (i % 2 == 0) {
				assertFalse(map.containsKey(key), "should not contain " + key);
			} else {
				assertEquals(i, map.get(key), "should contain " + key);
			}
		}
	}

	// ------------------------------------------------------------------
	// Functional operations + shift
	// ------------------------------------------------------------------

	@Test
	void computeIfPresentRemovalTriggersShift() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertNull(map.computeIfPresent(0x40, (k, v) -> null));
		assertEquals(2, map.size());
		assertEquals(1, map.get(0x20));
		assertEquals(3, map.get(0x60));
	}

	@Test
	void computeRemovalTriggersShift() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertNull(map.compute(0x40, (k, v) -> null));
		assertEquals(2, map.size());
		assertEquals(1, map.get(0x20));
		assertEquals(3, map.get(0x60));
	}

	@Test
	void mergeRemovalTriggersShift() {
		IntToIntMap map = new IntToIntMap(8);
		map.put(0x20, 1);
		map.put(0x40, 2);
		map.put(0x60, 3);

		assertNull(map.merge(0x40, 99, (v, nv) -> null));
		assertEquals(2, map.size());
		assertEquals(1, map.get(0x20));
		assertEquals(3, map.get(0x60));
	}

	// ------------------------------------------------------------------
	// Stress — remove then re-add
	// ------------------------------------------------------------------

	@Test
	void stressRemoveAndReAddAfterShift() {
		IntToIntMap map = new IntToIntMap(32);
		for (int round = 0; round < 5; round++) {
			for (int i = 0; i < 50; i++) {
				map.put(64 + i, i);
			}
			assertEquals(50, map.size());

			// Remove half
			for (int i = 0; i < 50; i++) {
				if (i % 2 == 1) {
					assertEquals(i, map.remove(64 + i));
				}
			}
			assertEquals(25, map.size());

			// Verify remaining
			for (int i = 0; i < 50; i++) {
				int key = 64 + i;
				if (i % 2 == 1) {
					assertFalse(map.containsKey(key));
				} else {
					assertEquals(i, map.get(key));
				}
			}

			// Re-add the removed ones
			for (int i = 0; i < 50; i++) {
				if (i % 2 == 1) {
					map.put(64 + i, i * 10);
				}
			}
			assertEquals(50, map.size());

			// Everything should be there
			for (int i = 0; i < 50; i++) {
				int key = 64 + i;
				assertTrue(map.containsKey(key), "round " + round + " should contain " + key);
			}

			map.clear();
		}
		assertEquals(0, map.size());
	}
}
