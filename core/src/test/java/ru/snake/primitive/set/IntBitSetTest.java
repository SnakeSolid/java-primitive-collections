package ru.snake.primitive.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class IntBitSetTest {

	@Test
	void constructorCreatesEmptySet() {
		IntBitSet set = new IntBitSet(64);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IntBitSet(-1));
	}

	// ------------------------------------------------------------------
	// get / set / clear
	// ------------------------------------------------------------------

	@Test
	void getReturnsFalseInitially() {
		IntBitSet set = new IntBitSet(64);
		for (int i = 0; i < 64; i++) {
			assertFalse(set.get(i));
		}
	}

	@Test
	void setAndGet() {
		IntBitSet set = new IntBitSet(64);
		set.set(10);
		set.set(31);
		set.set(32);
		set.set(63);
		assertEquals(4, set.size());
		assertTrue(set.get(10));
		assertTrue(set.get(31));
		assertTrue(set.get(32));
		assertTrue(set.get(63));
	}

	@Test
	void setSameBitDoesNotDoubleCount() {
		IntBitSet set = new IntBitSet(32);
		set.set(5);
		set.set(5);
		set.set(5);
		assertEquals(1, set.size());
	}

	@Test
	void clearRemovesBit() {
		IntBitSet set = new IntBitSet(64);
		set.set(10);
		set.clear(10);
		assertFalse(set.get(10));
		assertEquals(0, set.size());
	}

	@Test
	void clearNonExistentBitNoOp() {
		IntBitSet set = new IntBitSet(32);
		set.clear(5);
		assertEquals(0, set.size());
	}

	// ------------------------------------------------------------------
	// clearAll
	// ------------------------------------------------------------------

	@Test
	void clearAllResetsEverything() {
		IntBitSet set = new IntBitSet(128);
		set.set(0);
		set.set(31);
		set.set(32);
		set.set(127);
		set.clearAll();
		assertEquals(0, set.size());
		for (int i = 0; i < 128; i++) {
			assertFalse(set.get(i));
		}
	}

	// ------------------------------------------------------------------
	// Set<Integer> interface
	// ------------------------------------------------------------------

	@Test
	void addAndContains() {
		Set<Integer> set = new IntBitSet(64);
		assertTrue(set.add(10));
		assertTrue(set.contains(10));
		assertFalse(set.contains(11));
	}

	@Test
	void addDuplicateReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.add(5));
		assertEquals(1, set.size());
	}

	@Test
	void addNullThrows() {
		Set<Integer> set = new IntBitSet(32);
		assertThrows(NullPointerException.class, () -> set.add(null));
	}

	@Test
	void removeExistingReturnsTrue() {
		Set<Integer> set = new IntBitSet(32);
		set.add(10);
		assertTrue(set.remove(10));
		assertEquals(0, set.size());
	}

	@Test
	void removeNonExistentReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(10);
		assertFalse(set.remove(20));
		assertEquals(1, set.size());
	}

	@Test
	void removeNullReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		assertFalse(set.remove(null));
	}

	@SuppressWarnings("unused")
	@Test
	void iteratorVisitsAllElements() {
		IntBitSet set = new IntBitSet(64);
		set.set(3);
		set.set(10);
		set.set(35);
		set.set(63);
		int count = 0;
		for (Integer e : set) {
			count++;
		}
		assertEquals(4, count);
	}

	@Test
	void clearViaSetInterface() {
		Set<Integer> set = new IntBitSet(32);
		set.add(1);
		set.add(2);
		set.clear();
		assertEquals(0, set.size());
	}

	@Test
	void containsAll() {
		IntBitSet set = new IntBitSet(64);
		set.set(1);
		set.set(2);
		assertTrue(set.containsAll(Set.of(1, 2)));
		assertFalse(set.containsAll(Set.of(1, 3)));
	}

	@Test
	void addAll() {
		IntBitSet set = new IntBitSet(64);
		set.add(0);
		assertTrue(set.addAll(Set.of(0, 1, 2)));
		assertEquals(3, set.size());
	}

	@Test
	void removeAll() {
		IntBitSet set = new IntBitSet(64);
		set.set(0);
		set.set(1);
		set.set(2);
		assertTrue(set.removeAll(Set.of(1)));
		assertEquals(2, set.size());
		assertTrue(set.get(0));
		assertFalse(set.get(1));
		assertTrue(set.get(2));
	}

	@Test
	void retainAll() {
		IntBitSet set = new IntBitSet(64);
		set.set(0);
		set.set(1);
		set.set(2);
		assertTrue(set.retainAll(Set.of(1, 3)));
		assertEquals(1, set.size());
		assertTrue(set.get(1));
	}

	@Test
	void equalsAndHashCode() {
		IntBitSet a = new IntBitSet(64);
		a.set(1);
		a.set(2);
		IntBitSet b = new IntBitSet(64);
		b.set(1);
		b.set(2);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void toStringContainsElements() {
		IntBitSet set = new IntBitSet(32);
		set.set(5);
		set.set(10);
		String s = set.toString();
		assertTrue(s.contains("5"));
		assertTrue(s.contains("10"));
	}

	@Test
	void emptyToString() {
		IntBitSet set = new IntBitSet(32);
		assertEquals("[]", set.toString());
	}

	// ------------------------------------------------------------------
	// Word boundary tests (bits 31/32)
	// ------------------------------------------------------------------

	@Test
	void bitsAtWordBoundary() {
		IntBitSet set = new IntBitSet(64);
		set.set(31); // last bit in word 0
		set.set(32); // first bit in word 1
		assertTrue(set.get(31));
		assertTrue(set.get(32));
		assertEquals(2, set.size());
		set.clear(31);
		assertFalse(set.get(31));
		assertEquals(1, set.size());
	}

	// ------------------------------------------------------------------
	// add - out of range
	// ------------------------------------------------------------------

	@Test
	void addNegativeThrows() {
		Set<Integer> set = new IntBitSet(32);
		assertThrows(IllegalArgumentException.class, () -> set.add(-1));
	}

	@Test
	void addOutOfRangeThrows() {
		Set<Integer> set = new IntBitSet(32);
		assertThrows(IllegalArgumentException.class, () -> set.add(32));
	}

	// ------------------------------------------------------------------
	// remove - out of range Integer
	// ------------------------------------------------------------------

	@Test
	void removeNegativeIntegerReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.remove(-1));
	}

	@Test
	void removeOutOfRangeIntegerReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.remove(32));
	}

	// ------------------------------------------------------------------
	// contains - non-Integer / out of range
	// ------------------------------------------------------------------

	@Test
	void containsNonIntegerReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.contains((Object) "five"));
	}

	@Test
	void containsNegativeReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.contains(-1));
	}

	@Test
	void containsOutOfRangeReturnsFalse() {
		Set<Integer> set = new IntBitSet(32);
		set.add(5);
		assertFalse(set.contains(32));
	}

	// ------------------------------------------------------------------
	// containsAll - edge cases
	// ------------------------------------------------------------------

	@Test
	void containsAllEmptyCollectionReturnsTrue() {
		IntBitSet set = new IntBitSet(32);
		assertTrue(set.containsAll(Set.of()));
	}

	// ------------------------------------------------------------------
	// addAll - all duplicates -> false
	// ------------------------------------------------------------------

	@Test
	void addAllAllDuplicatesReturnsFalse() {
		IntBitSet set = new IntBitSet(32);
		set.add(1);
		set.add(2);
		assertFalse(set.addAll(Set.of(1, 2)));
		assertEquals(2, set.size());
	}

	// ------------------------------------------------------------------
	// retainAll - nothing to remove -> false
	// ------------------------------------------------------------------

	@Test
	void retainAllNothingToRemoveReturnsFalse() {
		IntBitSet set = new IntBitSet(32);
		set.set(1);
		set.set(2);
		assertFalse(set.retainAll(Set.of(1, 2, 3)));
		assertEquals(2, set.size());
	}

	// ------------------------------------------------------------------
	// removeAll - nothing to remove -> false
	// ------------------------------------------------------------------

	@Test
	void removeAllNothingToRemoveReturnsFalse() {
		IntBitSet set = new IntBitSet(32);
		set.set(1);
		set.set(2);
		assertFalse(set.removeAll(Set.of(3, 4)));
		assertEquals(2, set.size());
	}

	// ------------------------------------------------------------------
	// equals - edge cases
	// ------------------------------------------------------------------

	@Test
	void equalsSelfReturnsTrue() {
		IntBitSet set = new IntBitSet(32);
		assertEquals(set, set);
	}

	@Test
	void equalsNonSetReturnsFalse() {
		IntBitSet set = new IntBitSet(32);
		assertNotEquals("not a set", set);
	}

	@Test
	void equalsDifferentSizeReturnsFalse() {
		IntBitSet a = new IntBitSet(64);
		a.set(1);
		IntBitSet b = new IntBitSet(64);
		b.set(1);
		b.set(2);
		assertNotEquals(a, b);
	}

	// ------------------------------------------------------------------
	// toArray
	// ------------------------------------------------------------------

	@Test
	void toArrayReturnsElements() {
		IntBitSet set = new IntBitSet(32);
		set.set(5);
		set.set(10);
		Object[] arr = set.toArray();
		assertEquals(2, arr.length);
	}

	@Test
	void toArrayTypedReturnsElements() {
		IntBitSet set = new IntBitSet(32);
		set.set(5);
		set.set(10);
		Integer[] arr = set.toArray(new Integer[0]);
		assertEquals(2, arr.length);
	}

	@Test
	void toArrayTypedLargeArray() {
		IntBitSet set = new IntBitSet(32);
		set.set(5);
		set.set(10);
		Integer[] arr = set.toArray(new Integer[10]);
		// toArray reuses the supplied array when large enough
		assertEquals(10, arr.length);
		assertEquals(5, arr[0].intValue());
		assertEquals(10, arr[1].intValue());
		assertNull(arr[2]);
	}

	// ------------------------------------------------------------------
	// capacity zero
	// ------------------------------------------------------------------

	@Test
	void zeroCapacityCreatesEmptySet() {
		IntBitSet set = new IntBitSet(0);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}
}
