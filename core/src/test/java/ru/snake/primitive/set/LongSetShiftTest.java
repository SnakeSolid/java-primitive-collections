package ru.snake.primitive.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for backward-shift deletion in LongSet.
 *
 * <p>
 * These verify that probe chains stay intact when elements are removed from
 * the middle of chains.
 * </p>
 */
class LongSetShiftTest {

	@Test
	void removeFromMiddleOfProbeChainKeepsSiblings() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);
		set.add(256);

		assertTrue(set.remove(128));
		assertEquals(3, set.size());

		assertTrue(set.contains(64));
		assertTrue(set.contains(192));
		assertTrue(set.contains(256));
		assertFalse(set.contains(128));
	}

	@Test
	void removeFromBeginningOfProbeChainShiftsBack() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);

		assertTrue(set.remove(64));
		assertEquals(2, set.size());

		assertTrue(set.contains(128));
		assertTrue(set.contains(192));
		assertFalse(set.contains(64));
	}

	@Test
	void removeFromEndOfProbeChainDoesNotDisruptOthers() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);

		assertTrue(set.remove(192));
		assertEquals(2, set.size());
		assertTrue(set.contains(64));
		assertTrue(set.contains(128));
	}

	// ------------------------------------------------------------------
	// Split key chains
	// ------------------------------------------------------------------

	@Test
	void splitKeyChainsSurviveRemovals() {
		LongSet set = new LongSet(16);

		set.add(64);
		set.add(128);
		set.add(192);
		set.add(256);
		set.add(320);
		set.add(384);
		set.add(448);
		set.add(512);

		assertTrue(set.remove(128));
		assertTrue(set.remove(256));
		assertTrue(set.remove(384));
		assertEquals(5, set.size());

		assertTrue(set.contains(64));
		assertTrue(set.contains(192));
		assertTrue(set.contains(320));
		assertTrue(set.contains(448));
		assertTrue(set.contains(512));

		assertFalse(set.contains(128));
		assertFalse(set.contains(256));
		assertFalse(set.contains(384));
	}

	@Test
	void splitKeyChainsWithPackedSlots() {
		LongSet set = new LongSet(16);

		set.add(0);
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(4);
		set.add(64);
		set.add(128);
		set.add(192);

		assertEquals(8, set.size());

		assertTrue(set.remove(1));
		assertTrue(set.remove(3));
		assertEquals(6, set.size());

		assertTrue(set.contains(0));
		assertTrue(set.contains(2));
		assertTrue(set.contains(4));
		assertTrue(set.contains(64));
		assertTrue(set.contains(128));
		assertTrue(set.contains(192));
	}

	@Test
	void backwardShiftDoesNotCorruptAfterMultipleRemovals() {
		LongSet set = new LongSet(32);
		for (int i = 0; i < 20; i++) {
			set.add(128 + i * 128);
		}
		assertEquals(20, set.size());

		for (int i = 0; i < 20; i++) {
			int val = 128 + i * 128;
			if (i % 3 == 0) {
				assertTrue(set.remove(val));
			}
		}
		assertEquals(13, set.size());

		for (int i = 0; i < 20; i++) {
			int val = 128 + i * 128;
			if (i % 3 == 0) {
				assertFalse(set.contains(val), "should not contain " + val);
			} else {
				assertTrue(set.contains(val), "should contain " + val);
			}
		}
	}

	@Test
	void backwardShiftAfterRemovingAllPackedElements() {
		LongSet set = new LongSet(8);
		for (int i = 0; i < 64; i++) {
			set.add(i);
		}
		set.add(64);
		set.add(128);

		for (int i = 0; i < 64; i++) {
			assertTrue(set.remove(i));
		}
		assertEquals(2, set.size());
		assertTrue(set.contains(64));
		assertTrue(set.contains(128));
	}

	// ------------------------------------------------------------------
	// Iterator + shift
	// ------------------------------------------------------------------

	@Test
	void iteratorReflectsShiftedChains() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);
		set.add(256);

		assertTrue(set.remove(128));

		Set<Integer> collected = new HashSet<>();
		for (Integer e : set) {
			collected.add(e);
		}
		assertEquals(3, collected.size());
		assertTrue(collected.contains(64));
		assertTrue(collected.contains(192));
		assertTrue(collected.contains(256));
	}

	// ------------------------------------------------------------------
	// retainAll + shift
	// ------------------------------------------------------------------

	@Test
	void retainAllTriggersBackwardShift() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);
		set.add(256);
		set.add(320);

		assertTrue(set.retainAll(Set.of(64, 192, 320)));
		assertEquals(3, set.size());
		assertTrue(set.contains(64));
		assertTrue(set.contains(192));
		assertTrue(set.contains(320));
		assertFalse(set.contains(128));
		assertFalse(set.contains(256));
	}

	// ------------------------------------------------------------------
	// Add after removal
	// ------------------------------------------------------------------

	@Test
	void addAfterRemovalReusesShiftedSlots() {
		LongSet set = new LongSet(8);
		set.add(64);
		set.add(128);
		set.add(192);

		assertTrue(set.remove(128));
		assertTrue(set.add(128));
		assertEquals(3, set.size());

		assertTrue(set.contains(64));
		assertTrue(set.contains(128));
		assertTrue(set.contains(192));
	}

	// ------------------------------------------------------------------
	// Wrap-around shift
	// ------------------------------------------------------------------

	@Test
	void shiftBackWorksAcrossTableWrapAround() {
		LongSet set = new LongSet(16);

		// Values whose hashes land near the end of the table so that
		// probe chains wrap around.
		for (int i = 0; i < 12; i++) {
			set.add(i * 64);
		}
		assertEquals(12, set.size());

		// Verify all present
		for (int i = 0; i < 12; i++) {
			assertTrue(set.contains(i * 64), "should contain " + i * 64);
		}

		// Remove from the middle and verify the rest
		for (int i = 0; i < 12; i++) {
			if (i % 2 == 0) {
				assertTrue(set.remove(i * 64));
			}
		}
		assertEquals(6, set.size());

		for (int i = 0; i < 12; i++) {
			int val = i * 64;
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
		LongSet set = new LongSet(32);
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
