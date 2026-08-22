package ru.snake.primitive.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LongSetTest {

	@Test
	void defaultConstructorCreatesEmptySet() {
		LongSet set = new LongSet();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		LongSet set = new LongSet(64);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () -> new LongSet(-1));
	}

	// ------------------------------------------------------------------
	// add / contains / remove
	// ------------------------------------------------------------------

	@Test
	void addAndContains() {
		LongSet set = new LongSet();
		assertTrue(set.add(42));
		assertTrue(set.contains(42));
		assertFalse(set.contains(43));
	}

	@Test
	void addDuplicateReturnsFalse() {
		LongSet set = new LongSet();
		assertTrue(set.add(10));
		assertFalse(set.add(10));
		assertEquals(1, set.size());
	}

	@Test
	void addNullThrows() {
		Set<Integer> set = new LongSet();
		assertThrows(NullPointerException.class, () -> set.add(null));
	}

	@Test
	void removeExistingReturnsTrue() {
		LongSet set = new LongSet();
		set.add(10);
		assertTrue(set.remove(10));
		assertEquals(0, set.size());
		assertFalse(set.contains(10));
	}

	@Test
	void removeNonExistentReturnsFalse() {
		LongSet set = new LongSet();
		set.add(10);
		assertFalse(set.remove(20));
		assertEquals(1, set.size());
	}

	@Test
	void removeNullReturnsFalse() {
		LongSet set = new LongSet();
		set.add(5);
		assertFalse(set.remove(null));
	}

	@Test
	void removeNonIntegerReturnsFalse() {
		LongSet set = new LongSet();
		set.add(5);
		assertFalse(set.remove((Object) "five"));
	}

	@Test
	void containsNonIntegerReturnsFalse() {
		LongSet set = new LongSet();
		set.add(5);
		assertFalse(set.contains((Object) "five"));
	}

	// ------------------------------------------------------------------
	// Compact encoding — 64 elements sharing a 26-bit prefix
	// ------------------------------------------------------------------

	@Test
	void packs64ElementsWithSamePrefix() {
		LongSet set = new LongSet(8);
		// All values 0 .. 63 share the same 26-bit key (key = value & 0xFFFF_FFC0 = 0)
		for (int i = 0; i < 64; i++) {
			assertTrue(set.add(i), "should add " + i);
		}
		assertEquals(64, set.size());
		for (int i = 0; i < 64; i++) {
			assertTrue(set.contains(i), "should contain " + i);
		}
	}

	@Test
	void removesOneOf64PackedElements() {
		LongSet set = new LongSet(8);
		for (int i = 0; i < 64; i++) {
			set.add(i);
		}
		assertTrue(set.remove(31));
		assertEquals(63, set.size());
		assertFalse(set.contains(31));
		assertTrue(set.contains(0));
		assertTrue(set.contains(63));
	}

	@Test
	void removesAllPackedElementsWithSamePrefix() {
		LongSet set = new LongSet(8);
		for (int i = 0; i < 64; i++) {
			set.add(i);
		}
		for (int i = 0; i < 64; i++) {
			assertTrue(set.remove(i), "should remove " + i);
		}
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());

		// After backward-shift, the table should be clean — add and verify
		assertTrue(set.add(99));
		assertTrue(set.contains(99));
	}

	@Test
	void removeFromProbeChainKeepsSiblings() {
		// When a slot is fully emptied and backward-shift runs,
		// sibling elements that probed past this slot must remain findable.
		LongSet set = new LongSet(8);
		set.add(64); // key=0x40, offset=0
		set.add(128); // key=0x80, offset=0
		set.add(192); // key=0xC0, offset=0

		assertTrue(set.remove(128));
		assertEquals(2, set.size());
		assertTrue(set.contains(64));
		assertTrue(set.contains(192));
		assertFalse(set.contains(128));
	}

	@Test
	void elementsWithDifferentPrefixes() {
		LongSet set = new LongSet();
		// 5 -> key 0, offset 5
		// 0x20 + 5 = 37 -> key 0x20, offset 5
		assertTrue(set.add(5));
		assertTrue(set.add(0x20 + 5)); // 37
		assertEquals(2, set.size());
		assertTrue(set.contains(5));
		assertTrue(set.contains(37));
	}

	// ------------------------------------------------------------------
	// Negative values
	// ------------------------------------------------------------------

	@Test
	void negativeValuesWork() {
		LongSet set = new LongSet();
		assertTrue(set.add(-1));
		assertTrue(set.add(-100));
		assertTrue(set.add(-1000));
		assertEquals(3, set.size());
		assertTrue(set.contains(-1));
		assertTrue(set.contains(-100));
		assertTrue(set.contains(-1000));
	}

	@Test
	void minValueWorks() {
		LongSet set = new LongSet();
		assertTrue(set.add(Integer.MIN_VALUE));
		assertTrue(set.contains(Integer.MIN_VALUE));
		assertTrue(set.remove(Integer.MIN_VALUE));
		assertFalse(set.contains(Integer.MIN_VALUE));
	}

	@Test
	void maxValueWorks() {
		LongSet set = new LongSet();
		assertTrue(set.add(Integer.MAX_VALUE));
		assertTrue(set.contains(Integer.MAX_VALUE));
		assertTrue(set.remove(Integer.MAX_VALUE));
		assertFalse(set.contains(Integer.MAX_VALUE));
	}

	// ------------------------------------------------------------------
	// clear / isEmpty
	// ------------------------------------------------------------------

	@Test
	void clearEmptiesTheSet() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		set.add(3);
		set.clear();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
		assertFalse(set.contains(1));
		assertFalse(set.contains(2));
		assertFalse(set.contains(3));
	}

	@Test
	void isEmptyReturnsTrueForEmptySet() {
		LongSet set = new LongSet();
		assertTrue(set.isEmpty());
		set.add(1);
		assertFalse(set.isEmpty());
	}

	// ------------------------------------------------------------------
	// iterator
	// ------------------------------------------------------------------

	@SuppressWarnings("unused")
	@Test
	void iteratorVisitsAllElements() {
		LongSet set = new LongSet();
		set.add(10);
		set.add(20);
		set.add(30);
		int count = 0;
		for (Integer e : set) {
			count++;
		}
		assertEquals(3, count);
	}

	@Test
	void iteratorYieldsCorrectElements() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(100);
		set.add(200);
		Set<Integer> collected = new HashSet<>();
		for (Integer e : set) {
			collected.add(e);
		}
		assertTrue(collected.contains(5));
		assertTrue(collected.contains(100));
		assertTrue(collected.contains(200));
	}

	@Test
	void iteratorOnEmptySet() {
		LongSet set = new LongSet();
		Iterator<Integer> it = set.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	void iteratorRemoveThrowsIllegalStateExceptionWithoutNext() {
		LongSet set = new LongSet();
		set.add(1);
		Iterator<Integer> it = set.iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void iteratorRemoveDeletesElement() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		set.add(3);
		Iterator<Integer> it = set.iterator();
		int removed = it.next();
		it.remove();
		assertEquals(2, set.size());
		assertFalse(set.contains(removed));
		// iterator still works after remove
		ArrayList<Integer> remaining = new ArrayList<>();
		while (it.hasNext()) {
			remaining.add(it.next());
		}
		assertEquals(2, remaining.size());
		for (Integer e : remaining) {
			assertTrue(set.contains(e));
		}
	}

	@Test
	void iteratorRemoveTwiceWithoutNextThrows() {
		LongSet set = new LongSet();
		set.add(1);
		Iterator<Integer> it = set.iterator();
		it.next();
		it.remove();
		assertThrows(IllegalStateException.class, it::remove);
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	@Test
	void addAll() {
		LongSet set = new LongSet();
		set.add(0);
		assertTrue(set.addAll(Set.of(0, 1, 2)));
		assertEquals(3, set.size());
	}

	@Test
	void addAllAllDuplicatesReturnsFalse() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		assertFalse(set.addAll(Set.of(1, 2)));
		assertEquals(2, set.size());
	}

	@Test
	void removeAll() {
		LongSet set = new LongSet();
		set.add(0);
		set.add(1);
		set.add(2);
		assertTrue(set.removeAll(Set.of(1)));
		assertEquals(2, set.size());
		assertTrue(set.contains(0));
		assertFalse(set.contains(1));
		assertTrue(set.contains(2));
	}

	@Test
	void removeAllNothingToRemoveReturnsFalse() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		assertFalse(set.removeAll(Set.of(3, 4)));
		assertEquals(2, set.size());
	}

	@Test
	void retainAll() {
		LongSet set = new LongSet();
		set.add(0);
		set.add(1);
		set.add(2);
		assertTrue(set.retainAll(Set.of(1, 3)));
		assertEquals(1, set.size());
		assertTrue(set.contains(1));
		assertFalse(set.contains(0));
		assertFalse(set.contains(2));
	}

	@Test
	void retainAllNothingToRemoveReturnsFalse() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		assertFalse(set.retainAll(Set.of(1, 2, 3)));
		assertEquals(2, set.size());
	}

	@Test
	void containsAll() {
		LongSet set = new LongSet();
		set.add(1);
		set.add(2);
		assertTrue(set.containsAll(Set.of(1, 2)));
		assertFalse(set.containsAll(Set.of(1, 3)));
	}

	@Test
	void containsAllEmptyCollectionReturnsTrue() {
		LongSet set = new LongSet();
		assertTrue(set.containsAll(Set.of()));
	}

	// ------------------------------------------------------------------
	// putAll (LongSet specific)
	// ------------------------------------------------------------------

	@Test
	void putAllCopiesElements() {
		LongSet source = new LongSet();
		source.add(10);
		source.add(20);
		source.add(30);

		LongSet dest = new LongSet();
		dest.add(10);
		dest.putAll(source);

		assertEquals(3, dest.size());
		assertTrue(dest.contains(10));
		assertTrue(dest.contains(20));
		assertTrue(dest.contains(30));
	}

	// ------------------------------------------------------------------
	// equals / hashCode
	// ------------------------------------------------------------------

	@Test
	void equalsSelfReturnsTrue() {
		LongSet set = new LongSet();
		assertEquals(set, set);
	}

	@Test
	void equalsSameElementsReturnsTrue() {
		LongSet a = new LongSet();
		a.add(1);
		a.add(2);
		LongSet b = new LongSet();
		b.add(1);
		b.add(2);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equalsDifferentSizeReturnsFalse() {
		LongSet a = new LongSet();
		a.add(1);
		LongSet b = new LongSet();
		b.add(1);
		b.add(2);
		assertNotEquals(a, b);
	}

	@Test
	void equalsDifferentElementsReturnsFalse() {
		LongSet a = new LongSet();
		a.add(1);
		a.add(2);
		LongSet b = new LongSet();
		b.add(1);
		b.add(3);
		assertNotEquals(a, b);
	}

	@Test
	void equalsNonSetReturnsFalse() {
		LongSet set = new LongSet();
		assertNotEquals("not a set", set);
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsElements() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(10);
		String s = set.toString();
		assertTrue(s.contains("5"));
		assertTrue(s.contains("10"));
	}

	@Test
	void emptyToString() {
		LongSet set = new LongSet();
		assertEquals("[]", set.toString());
	}

	// ------------------------------------------------------------------
	// toArray
	// ------------------------------------------------------------------

	@Test
	void toArrayReturnsElements() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(10);
		Object[] arr = set.toArray();
		assertEquals(2, arr.length);
		assertEquals(5, arr[0]);
		assertEquals(10, arr[1]);
	}

	@Test
	void toArrayEmptyReturnsEmptyArray() {
		LongSet set = new LongSet();
		Object[] arr = set.toArray();
		assertEquals(0, arr.length);
	}

	@Test
	void toArrayTypedReturnsElements() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(10);
		Integer[] arr = set.toArray(new Integer[0]);
		assertEquals(2, arr.length);
		assertEquals(5, arr[0].intValue());
		assertEquals(10, arr[1].intValue());
	}

	@Test
	void toArrayTypedLargeArray() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(10);
		Integer[] arr = set.toArray(new Integer[10]);
		assertEquals(10, arr.length);
		assertEquals(5, arr[0].intValue());
		assertEquals(10, arr[1].intValue());
		assertNull(arr[2]);
	}

	@Test
	void toArrayTypedExactArray() {
		LongSet set = new LongSet();
		set.add(5);
		set.add(10);
		Integer[] arr = new Integer[2];
		Integer[] result = set.toArray(arr);
		assertEquals(arr, (Object) result);
		assertEquals(5, arr[0].intValue());
		assertEquals(10, arr[1].intValue());
	}

	@Test
	void toArrayTypedEmptyReturnsSameArray() {
		LongSet set = new LongSet();
		Integer[] arr = new Integer[3];
		Integer[] result = set.toArray(arr);
		assertEquals(arr, (Object) result);
		assertNull(arr[0]);
	}

	// ------------------------------------------------------------------
	// Collision stress test
	// ------------------------------------------------------------------

	@Test
	void largeNumberOfEntries() {
		LongSet set = new LongSet();
		for (int i = 0; i < 1000; i++) {
			assertTrue(set.add(i), "should add " + i);
		}
		assertEquals(1000, set.size());
		for (int i = 0; i < 1000; i++) {
			assertTrue(set.contains(i), "should contain " + i);
		}
	}

	@Test
	void repeatedAddAndRemoveStress() {
		LongSet set = new LongSet();
		for (int round = 0; round < 5; round++) {
			for (int i = 0; i < 200; i++) {
				set.add(i);
			}
			for (int i = 0; i < 200; i++) {
				assertTrue(
					set.contains(i),
					"round " + round + " should contain " + i
				);
			}
			assertEquals(200, set.size(), "round " + round);
			set.clear();
		}
		assertEquals(0, set.size());
	}

	@Test
	void zeroCapacityCreatesEmptySet() {
		LongSet set = new LongSet(0);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void implementsSetInterface() {
		// Verify LongSet is usable as Set<Integer>
		Set<Integer> set = new LongSet();
		set.add(1);
		set.add(2);
		set.add(3);
		assertEquals(3, set.size());
		assertTrue(set.contains(2));
		assertTrue(set.remove(2));
		assertEquals(2, set.size());
	}

	@Test
	void zeroIsValidElement() {
		LongSet set = new LongSet();
		assertTrue(set.add(0));
		assertTrue(set.contains(0));
		assertFalse(set.add(0));
		assertEquals(1, set.size());
		assertTrue(set.remove(0));
		assertFalse(set.contains(0));
	}

	// ------------------------------------------------------------------
	// Iterator consistency
	// ------------------------------------------------------------------

	@Test
	void iteratorSizeMatches() {
		LongSet set = new LongSet();
		for (int i = 0; i < 100; i++) {
			set.add(i * 7);
		}
		ArrayList<Integer> collected = new ArrayList<>();
		for (Integer e : set) {
			collected.add(e);
		}
		assertEquals(set.size(), collected.size());
		for (Integer e : collected) {
			assertTrue(set.contains(e));
		}
	}
}
