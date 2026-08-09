package ru.snake.collections.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;

class IntSetTest {

	@Test
	void defaultConstructorCreatesEmptySet() {
		IntSet set = new IntSet();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		IntSet set = new IntSet(64);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IntSet(-1));
	}

	// ------------------------------------------------------------------
	// add / contains / remove
	// ------------------------------------------------------------------

	@Test
	void addAndContains() {
		IntSet set = new IntSet();
		assertTrue(set.add(42));
		assertTrue(set.contains(42));
		assertFalse(set.contains(43));
	}

	@Test
	void addDuplicateReturnsFalse() {
		IntSet set = new IntSet();
		assertTrue(set.add(10));
		assertFalse(set.add(10));
		assertEquals(1, set.size());
	}

	@Test
	void addNullThrows() {
		Set<Integer> set = new IntSet();
		assertThrows(NullPointerException.class, () -> set.add(null));
	}

	@Test
	void removeExistingReturnsTrue() {
		IntSet set = new IntSet();
		set.add(10);
		assertTrue(set.remove(10));
		assertEquals(0, set.size());
		assertFalse(set.contains(10));
	}

	@Test
	void removeNonExistentReturnsFalse() {
		IntSet set = new IntSet();
		set.add(10);
		assertFalse(set.remove(20));
		assertEquals(1, set.size());
	}

	@Test
	void removeNullReturnsFalse() {
		IntSet set = new IntSet();
		set.add(5);
		assertFalse(set.remove(null));
	}

	@Test
	void removeNonIntegerReturnsFalse() {
		IntSet set = new IntSet();
		set.add(5);
		assertFalse(set.remove((Object) "five"));
	}

	@Test
	void containsNonIntegerReturnsFalse() {
		IntSet set = new IntSet();
		set.add(5);
		assertFalse(set.contains((Object) "five"));
	}

	// ------------------------------------------------------------------
	// Compact encoding — 32 elements sharing a 27-bit prefix
	// ------------------------------------------------------------------

	@Test
	void packs32ElementsWithSamePrefix() {
		IntSet set = new IntSet(8);
		// All values 0x00000000 .. 0x0000001f share the same 27-bit key
		for (int i = 0; i < 32; i++) {
			assertTrue(set.add(i), "should add " + i);
		}
		assertEquals(32, set.size());
		for (int i = 0; i < 32; i++) {
			assertTrue(set.contains(i), "should contain " + i);
		}
	}

	@Test
	void removesOneOf32PackedElements() {
		IntSet set = new IntSet(8);
		for (int i = 0; i < 32; i++) {
			set.add(i);
		}
		assertTrue(set.remove(15));
		assertEquals(31, set.size());
		assertFalse(set.contains(15));
		assertTrue(set.contains(0));
		assertTrue(set.contains(31));
	}

	@Test
	void removesAllPackedElementsWithSamePrefix() {
		IntSet set = new IntSet(8);
		for (int i = 0; i < 32; i++) {
			set.add(i);
		}
		for (int i = 0; i < 32; i++) {
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
		IntSet set = new IntSet(8);
		set.add(0x20);
		set.add(0x40);
		set.add(0x60);

		assertTrue(set.remove(0x40));
		assertEquals(2, set.size());
		assertTrue(set.contains(0x20));
		assertTrue(set.contains(0x60));
		assertFalse(set.contains(0x40));
	}

	@Test
	void elementsWithDifferentPrefixes() {
		IntSet set = new IntSet();
		// 0x00000005 -> key 0x00000000, offset 5
		// 0x00000025 -> key 0x00000020, offset 5
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
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
		assertTrue(set.add(Integer.MIN_VALUE));
		assertTrue(set.contains(Integer.MIN_VALUE));
		assertTrue(set.remove(Integer.MIN_VALUE));
		assertFalse(set.contains(Integer.MIN_VALUE));
	}

	@Test
	void maxValueWorks() {
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
		Iterator<Integer> it = set.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	void iteratorRemoveThrows() {
		IntSet set = new IntSet();
		set.add(1);
		Iterator<Integer> it = set.iterator();
		it.next();
		assertThrows(UnsupportedOperationException.class, it::remove);
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	@Test
	void addAll() {
		IntSet set = new IntSet();
		set.add(0);
		assertTrue(set.addAll(Set.of(0, 1, 2)));
		assertEquals(3, set.size());
	}

	@Test
	void addAllAllDuplicatesReturnsFalse() {
		IntSet set = new IntSet();
		set.add(1);
		set.add(2);
		assertFalse(set.addAll(Set.of(1, 2)));
		assertEquals(2, set.size());
	}

	@Test
	void removeAll() {
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
		set.add(1);
		set.add(2);
		assertFalse(set.removeAll(Set.of(3, 4)));
		assertEquals(2, set.size());
	}

	@Test
	void retainAll() {
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
		set.add(1);
		set.add(2);
		assertFalse(set.retainAll(Set.of(1, 2, 3)));
		assertEquals(2, set.size());
	}

	@Test
	void containsAll() {
		IntSet set = new IntSet();
		set.add(1);
		set.add(2);
		assertTrue(set.containsAll(Set.of(1, 2)));
		assertFalse(set.containsAll(Set.of(1, 3)));
	}

	@Test
	void containsAllEmptyCollectionReturnsTrue() {
		IntSet set = new IntSet();
		assertTrue(set.containsAll(Set.of()));
	}

	// ------------------------------------------------------------------
	// putAll (IntSet specific)
	// ------------------------------------------------------------------

	@Test
	void putAllCopiesElements() {
		IntSet source = new IntSet();
		source.add(10);
		source.add(20);
		source.add(30);

		IntSet dest = new IntSet();
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
		IntSet set = new IntSet();
		assertEquals(set, set);
	}

	@Test
	void equalsSameElementsReturnsTrue() {
		IntSet a = new IntSet();
		a.add(1);
		a.add(2);
		IntSet b = new IntSet();
		b.add(1);
		b.add(2);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equalsDifferentSizeReturnsFalse() {
		IntSet a = new IntSet();
		a.add(1);
		IntSet b = new IntSet();
		b.add(1);
		b.add(2);
		assertNotEquals(a, b);
	}

	@Test
	void equalsDifferentElementsReturnsFalse() {
		IntSet a = new IntSet();
		a.add(1);
		a.add(2);
		IntSet b = new IntSet();
		b.add(1);
		b.add(3);
		assertNotEquals(a, b);
	}

	@Test
	void equalsNonSetReturnsFalse() {
		IntSet set = new IntSet();
		assertNotEquals("not a set", set);
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsElements() {
		IntSet set = new IntSet();
		set.add(5);
		set.add(10);
		String s = set.toString();
		assertTrue(s.contains("5"));
		assertTrue(s.contains("10"));
	}

	@Test
	void emptyToString() {
		IntSet set = new IntSet();
		assertEquals("[]", set.toString());
	}

	// ------------------------------------------------------------------
	// toArray
	// ------------------------------------------------------------------

	@Test
	void toArrayReturnsElements() {
		IntSet set = new IntSet();
		set.add(5);
		set.add(10);
		Object[] arr = set.toArray();
		assertEquals(2, arr.length);
	}

	@Test
	void toArrayTypedReturnsElements() {
		IntSet set = new IntSet();
		set.add(5);
		set.add(10);
		Integer[] arr = set.toArray(new Integer[0]);
		assertEquals(2, arr.length);
	}

	// ------------------------------------------------------------------
	// Collision stress test
	// ------------------------------------------------------------------

	@Test
	void largeNumberOfEntries() {
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
		for (int round = 0; round < 5; round++) {
			for (int i = 0; i < 200; i++) {
				set.add(i);
			}
			for (int i = 0; i < 200; i++) {
				assertTrue(set.contains(i), "round " + round + " should contain " + i);
			}
			assertEquals(200, set.size(), "round " + round);
			set.clear();
		}
		assertEquals(0, set.size());
	}

	@Test
	void zeroCapacityCreatesEmptySet() {
		IntSet set = new IntSet(0);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void implementsSetInterface() {
		// Verify IntSet is usable as Set<Integer>
		Set<Integer> set = new IntSet();
		set.add(1);
		set.add(2);
		set.add(3);
		assertEquals(3, set.size());
		assertTrue(set.contains(2));
		assertTrue(set.remove(2));
		assertEquals(2, set.size());
	}

	@Test
	void zeroIsAValidElement() {
		IntSet set = new IntSet();
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
		IntSet set = new IntSet();
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
