package ru.snake.primitive.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ObjectSetTest {

	@Test
	void defaultConstructorCreatesEmptySet() {
		ObjectSet<String> set = new ObjectSet<>();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		ObjectSet<String> set = new ObjectSet<>(32);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () ->
			new ObjectSet<String>(-1)
		);
	}

	// ------------------------------------------------------------------
	// add / contains / remove
	// ------------------------------------------------------------------

	@Test
	void addAndContains() {
		ObjectSet<String> set = new ObjectSet<>();
		assertTrue(set.add("hello"));
		assertTrue(set.contains("hello"));
		assertFalse(set.contains("world"));
	}

	@Test
	void addDuplicateReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		assertTrue(set.add("one"));
		assertFalse(set.add("one"));
		assertEquals(1, set.size());
	}

	@Test
	void addNullThrows() {
		Set<String> set = new ObjectSet<>();
		assertThrows(NullPointerException.class, () -> set.add(null));
	}

	@Test
	void removeExistingReturnsTrue() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("hello");
		assertTrue(set.remove("hello"));
		assertEquals(0, set.size());
		assertFalse(set.contains("hello"));
	}

	@Test
	void removeNonExistentReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("hello");
		assertFalse(set.remove("bye"));
		assertEquals(1, set.size());
	}

	@Test
	void removeNullReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("five");
		assertFalse(set.remove(null));
	}

	@Test
	void containsNullReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("five");
		assertFalse(set.contains(null));
	}

	// ------------------------------------------------------------------
	// clear / isEmpty
	// ------------------------------------------------------------------

	@Test
	void clearEmptiesTheSet() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		set.clear();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
		assertFalse(set.contains("a"));
		assertFalse(set.contains("b"));
		assertFalse(set.contains("c"));
	}

	@Test
	void isEmptyReturnsTrueForEmptySet() {
		ObjectSet<String> set = new ObjectSet<>();
		assertTrue(set.isEmpty());
		set.add("x");
		assertFalse(set.isEmpty());
	}

	// ------------------------------------------------------------------
	// iterator
	// ------------------------------------------------------------------

	@SuppressWarnings("unused")
	@Test
	void iteratorVisitsAllElements() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		int count = 0;
		for (String e : set) {
			count++;
		}
		assertEquals(3, count);
	}

	@Test
	void iteratorYieldsCorrectElements() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("foo");
		set.add("bar");
		set.add("baz");
		Set<String> collected = new HashSet<>();
		for (String e : set) {
			collected.add(e);
		}
		assertTrue(collected.contains("foo"));
		assertTrue(collected.contains("bar"));
		assertTrue(collected.contains("baz"));
	}

	@Test
	void iteratorOnEmptySet() {
		ObjectSet<String> set = new ObjectSet<>();
		Iterator<String> it = set.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	void iteratorRemove() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("one");
		set.add("two");
		set.add("three");
		Iterator<String> it = set.iterator();
		it.next();
		it.remove();
		assertEquals(2, set.size());

		// calling remove() again without next() should throw
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void iteratorRemoveWithoutNext() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("one");
		Iterator<String> it = set.iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	@Test
	void addAll() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		assertTrue(set.addAll(Set.of("a", "b", "c")));
		assertEquals(3, set.size());
	}

	@Test
	void addAllAllDuplicatesReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		assertFalse(set.addAll(Set.of("a", "b")));
		assertEquals(2, set.size());
	}

	@Test
	void removeAll() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		assertTrue(set.removeAll(Set.of("b")));
		assertEquals(2, set.size());
		assertTrue(set.contains("a"));
		assertFalse(set.contains("b"));
		assertTrue(set.contains("c"));
	}

	@Test
	void removeAllNothingToRemoveReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		assertFalse(set.removeAll(Set.of("x", "y")));
		assertEquals(2, set.size());
	}

	@Test
	void retainAll() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		assertTrue(set.retainAll(Set.of("b", "z")));
		assertEquals(1, set.size());
		assertTrue(set.contains("b"));
		assertFalse(set.contains("a"));
		assertFalse(set.contains("c"));
	}

	@Test
	void retainAllNothingToRemoveReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		assertFalse(set.retainAll(Set.of("a", "b", "c")));
		assertEquals(2, set.size());
	}

	@Test
	void containsAll() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		assertTrue(set.containsAll(Set.of("a", "b")));
		assertFalse(set.containsAll(Set.of("a", "c")));
	}

	@Test
	void containsAllEmptyCollectionReturnsTrue() {
		ObjectSet<String> set = new ObjectSet<>();
		assertTrue(set.containsAll(Set.of()));
	}

	// ------------------------------------------------------------------
	// putAll (ObjectSet specific)
	// ------------------------------------------------------------------

	@Test
	void putAllCopiesElements() {
		ObjectSet<String> source = new ObjectSet<>();
		source.add("x");
		source.add("y");
		source.add("z");

		ObjectSet<String> dest = new ObjectSet<>();
		dest.add("x");
		dest.putAll(source);

		assertEquals(3, dest.size());
		assertTrue(dest.contains("x"));
		assertTrue(dest.contains("y"));
		assertTrue(dest.contains("z"));
	}

	// ------------------------------------------------------------------
	// equals / hashCode
	// ------------------------------------------------------------------

	@Test
	void equalsSelfReturnsTrue() {
		ObjectSet<String> set = new ObjectSet<>();
		assertEquals(set, set);
	}

	@Test
	void equalsSameElementsReturnsTrue() {
		ObjectSet<String> a = new ObjectSet<>();
		a.add("a");
		a.add("b");
		ObjectSet<String> b = new ObjectSet<>();
		b.add("a");
		b.add("b");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equalsDifferentSizeReturnsFalse() {
		ObjectSet<String> a = new ObjectSet<>();
		a.add("a");
		ObjectSet<String> b = new ObjectSet<>();
		b.add("a");
		b.add("b");
		assertNotEquals(a, b);
	}

	@Test
	void equalsDifferentElementsReturnsFalse() {
		ObjectSet<String> a = new ObjectSet<>();
		a.add("a");
		a.add("b");
		ObjectSet<String> b = new ObjectSet<>();
		b.add("a");
		b.add("c");
		assertNotEquals(a, b);
	}

	@Test
	void equalsNonSetReturnsFalse() {
		ObjectSet<String> set = new ObjectSet<>();
		assertNotEquals("not a set", set);
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsElements() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("hello");
		set.add("world");
		String s = set.toString();
		assertTrue(s.contains("hello"));
		assertTrue(s.contains("world"));
	}

	@Test
	void emptyToString() {
		ObjectSet<String> set = new ObjectSet<>();
		assertEquals("[]", set.toString());
	}

	// ------------------------------------------------------------------
	// toArray
	// ------------------------------------------------------------------

	@Test
	void toArrayReturnsElements() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("x");
		set.add("y");
		Object[] arr = set.toArray();
		assertEquals(2, arr.length);
		Set<?> elements = new HashSet<>(java.util.Arrays.asList(arr));
		assertTrue(elements.contains("x"));
		assertTrue(elements.contains("y"));
	}

	@Test
	void toArrayEmptyReturnsEmptyArray() {
		ObjectSet<String> set = new ObjectSet<>();
		Object[] arr = set.toArray();
		assertEquals(0, arr.length);
	}

	@Test
	void toArrayTypedReturnsElements() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("x");
		set.add("y");
		String[] arr = set.toArray(new String[0]);
		assertEquals(2, arr.length);
		assertTrue(java.util.Arrays.asList(arr).contains("x"));
		assertTrue(java.util.Arrays.asList(arr).contains("y"));
	}

	@Test
	void toArrayTypedLargeArray() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("x");
		set.add("y");
		String[] arr = set.toArray(new String[10]);
		assertEquals(10, arr.length);
		assertTrue(java.util.Arrays.asList(arr[0], arr[1]).contains("x"));
		assertTrue(java.util.Arrays.asList(arr[0], arr[1]).contains("y"));
		assertNull(arr[2]);
	}

	@Test
	void toArrayTypedExactArray() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("x");
		set.add("y");
		String[] arr = new String[2];
		String[] result = set.toArray(arr);
		assertEquals(arr, (Object) result);
		assertTrue(java.util.Arrays.asList(arr).contains("x"));
		assertTrue(java.util.Arrays.asList(arr).contains("y"));
	}

	@Test
	void toArrayTypedEmptyReturnsSameArray() {
		ObjectSet<String> set = new ObjectSet<>();
		String[] arr = new String[3];
		String[] result = set.toArray(arr);
		assertEquals(arr, (Object) result);
		assertNull(arr[0]);
	}

	// ------------------------------------------------------------------
	// Collision stress test
	// ------------------------------------------------------------------

	@Test
	void largeNumberOfEntries() {
		ObjectSet<String> set = new ObjectSet<>();
		for (int i = 0; i < 1000; i++) {
			assertTrue(set.add("item-" + i), "should add item-" + i);
		}
		assertEquals(1000, set.size());
		for (int i = 0; i < 1000; i++) {
			assertTrue(set.contains("item-" + i), "should contain item-" + i);
		}
	}

	@Test
	void repeatedAddAndRemoveStress() {
		ObjectSet<String> set = new ObjectSet<>();
		for (int round = 0; round < 5; round++) {
			for (int i = 0; i < 200; i++) {
				set.add("r" + round + "-" + i);
			}
			for (int i = 0; i < 200; i++) {
				assertTrue(set.contains("r" + round + "-" + i));
			}
			assertEquals(200, set.size());
			set.clear();
		}
		assertEquals(0, set.size());
	}

	// ------------------------------------------------------------------
	// Misc
	// ------------------------------------------------------------------

	@Test
	void zeroCapacityCreatesEmptySet() {
		ObjectSet<String> set = new ObjectSet<>(0);
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	void implementsSetInterface() {
		Set<String> set = new ObjectSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		assertEquals(3, set.size());
		assertTrue(set.contains("b"));
		assertTrue(set.remove("b"));
		assertEquals(2, set.size());
	}

	@Test
	void iteratorSizeMatches() {
		ObjectSet<String> set = new ObjectSet<>();
		for (int i = 0; i < 100; i++) {
			set.add("el-" + i);
		}
		ArrayList<String> collected = new ArrayList<>();
		for (String e : set) {
			collected.add(e);
		}
		assertEquals(set.size(), collected.size());
		for (String e : collected) {
			assertTrue(set.contains(e));
		}
	}

	@Test
	void handlesCollisionsGracefully() {
		// Strings with similar hash codes
		ObjectSet<String> set = new ObjectSet<>();
		set.add("Aa");
		set.add("BB");
		set.add("CC");
		set.add("aA");
		set.add("bB");
		set.add("cC");
		assertEquals(6, set.size());
		assertTrue(set.contains("Aa"));
		assertTrue(set.contains("BB"));
		assertTrue(set.contains("CC"));
		assertTrue(set.contains("aA"));
		assertTrue(set.contains("bB"));
		assertTrue(set.contains("cC"));
	}

	@Test
	void removeDoesNotBreakOtherEntries() {
		ObjectSet<String> set = new ObjectSet<>();
		set.add("alpha");
		set.add("beta");
		set.add("gamma");
		assertTrue(set.remove("beta"));
		assertEquals(2, set.size());
		assertTrue(set.contains("alpha"));
		assertTrue(set.contains("gamma"));
		assertFalse(set.contains("beta"));
	}

	@Test
	void primitiveAdd0AndRemove0() {
		ObjectSet<Integer> set = new ObjectSet<>();
		assertTrue(set.add0(1));
		assertTrue(set.contains0(1));
		assertTrue(set.remove0(1));
		assertFalse(set.contains0(1));
	}
}
