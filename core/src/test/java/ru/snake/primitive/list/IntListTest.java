package ru.snake.primitive.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.Test;

class IntListTest {

	@Test
	void defaultConstructorCreatesEmptyList() {
		IntList list = new IntList();
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		IntList list = new IntList(32);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IntList(-1));
	}

	@Test
	void constructorFromCollection() {
		List<Integer> source = Arrays.asList(1, 2, 3);
		IntList list = new IntList(source);
		assertEquals(3, list.size());
		assertEquals(1, list.getInt(0));
		assertEquals(2, list.getInt(1));
		assertEquals(3, list.getInt(2));
	}

	// ------------------------------------------------------------------
	// add / get / set / remove
	// ------------------------------------------------------------------

	@Test
	void addAndGet() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(30);
		assertEquals(10, list.get(0));
		assertEquals(20, list.get(1));
		assertEquals(30, list.get(2));
		assertEquals(3, list.size());
	}

	@Test
	void addAtPosition() {
		IntList list = new IntList();
		list.add(10);
		list.add(30);
		list.add(1, 20);
		assertEquals(10, list.get(0));
		assertEquals(20, list.get(1));
		assertEquals(30, list.get(2));
	}

	@Test
	void setReplacesElement() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		Integer old = list.set(0, 99);
		assertEquals(10, old);
		assertEquals(99, list.get(0));
		assertEquals(20, list.get(1));
	}

	@Test
	void removeByIndexReturnsElement() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(30);
		Integer removed = list.remove(1);
		assertEquals(20, removed);
		assertEquals(2, list.size());
		assertEquals(10, list.get(0));
		assertEquals(30, list.get(1));
	}

	@Test
	void removeByObjectRemovesFirstMatch() {
		List<Integer> list = new IntList();
		list.add(10);
		list.add(20);
		list.add(20);
		assertTrue(list.remove(Integer.valueOf(20)));
		assertEquals(2, list.size());
		assertEquals(10, list.get(0));
		assertEquals(20, list.get(1));
	}

	@Test
	void removeNonExistentReturnsFalse() {
		IntList list = new IntList();
		list.add(10);
		assertFalse(list.remove(Integer.valueOf(99)));
		assertEquals(1, list.size());
	}

	@Test
	void removeNonIntegerReturnsFalse() {
		List<Integer> list = new IntList();
		list.add(10);
		assertFalse(list.remove("not an int"));
	}

	@Test
	void getOutOfBoundsThrows() {
		IntList list = new IntList();
		list.add(10);
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
	}

	@Test
	void setOutOfBoundsThrows() {
		IntList list = new IntList();
		list.add(10);
		assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, 20));
	}

	@Test
	void addAtOutOfBoundsThrows() {
		IntList list = new IntList();
		list.add(10);
		assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 20));
		assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 20));
	}

	// ------------------------------------------------------------------
	// Primitive convenience methods
	// ------------------------------------------------------------------

	@Test
	void getIntReturnsPrimitive() {
		IntList list = new IntList();
		list.add(42);
		assertEquals(42, list.getInt(0));
	}

	@Test
	void getIntOutOfBoundsThrows() {
		IntList list = new IntList();
		list.add(42);
		assertThrows(IndexOutOfBoundsException.class, () -> list.getInt(1));
	}

	@Test
	void setIntReplacesPrimitive() {
		IntList list = new IntList();
		list.add(10);
		int old = list.setInt(0, 99);
		assertEquals(10, old);
		assertEquals(99, list.getInt(0));
	}

	@Test
	void addIntAppends() {
		IntList list = new IntList();
		list.addInt(10);
		list.addInt(20);
		assertEquals(2, list.size());
		assertEquals(10, list.getInt(0));
		assertEquals(20, list.getInt(1));
	}

	@Test
	void insertIntAtPosition() {
		IntList list = new IntList();
		list.addInt(10);
		list.addInt(30);
		list.insertInt(1, 20);
		assertEquals(3, list.size());
		assertEquals(10, list.getInt(0));
		assertEquals(20, list.getInt(1));
		assertEquals(30, list.getInt(2));
	}

	// ------------------------------------------------------------------
	// null handling
	// ------------------------------------------------------------------

	@Test
	void addNullThrows() {
		IntList list = new IntList();
		assertThrows(NullPointerException.class, () -> list.add(null));
	}

	@Test
	void setNullThrows() {
		IntList list = new IntList();
		list.add(10);
		assertThrows(NullPointerException.class, () -> list.set(0, null));
	}

	@Test
	void containsNullReturnsFalse() {
		IntList list = new IntList();
		list.add(10);
		assertFalse(list.contains(null));
	}

	// ------------------------------------------------------------------
	// clear / isEmpty
	// ------------------------------------------------------------------

	@Test
	void clearEmptiesTheList() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		list.clear();
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	// ------------------------------------------------------------------
	// contains
	// ------------------------------------------------------------------

	@Test
	void containsPresentElement() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		assertTrue(list.contains(10));
		assertTrue(list.contains(20));
		assertFalse(list.contains(30));
	}

	@Test
	void containsNonIntegerReturnsFalse() {
		List<Integer> list = new IntList();
		list.add(10);
		assertFalse(list.contains("10"));
	}

	// ------------------------------------------------------------------
	// iterator
	// ------------------------------------------------------------------

	@Test
	void iteratorVisitsAllElements() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		int count = 0;
		int sum = 0;
		for (Integer e : list) {
			sum += e;
			count++;
		}
		assertEquals(3, count);
		assertEquals(6, sum);
	}

	@Test
	void iteratorOnEmptyList() {
		IntList list = new IntList();
		Iterator<Integer> it = list.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	void iteratorRemove() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		Iterator<Integer> it = list.iterator();
		assertEquals(1, it.next());
		it.remove();
		assertEquals(2, list.size());
		assertEquals(2, list.get(0));
		assertEquals(3, list.get(1));
	}

	@Test
	void iteratorRemoveWithoutNext() {
		IntList list = new IntList();
		list.add(1);
		Iterator<Integer> it = list.iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void listIteratorForward() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(30);
		ListIterator<Integer> it = list.listIterator();
		assertEquals(10, it.next());
		assertEquals(20, it.next());
		assertEquals(30, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	void listIteratorBackward() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(30);
		ListIterator<Integer> it = list.listIterator(3);
		assertEquals(30, it.previous());
		assertEquals(20, it.previous());
		assertEquals(10, it.previous());
		assertFalse(it.hasPrevious());
	}

	@Test
	void listIteratorAdd() {
		IntList list = new IntList();
		list.add(10);
		list.add(30);
		ListIterator<Integer> it = list.listIterator(1);
		it.add(20);
		assertEquals(3, list.size());
		assertEquals(10, list.get(0));
		assertEquals(20, list.get(1));
		assertEquals(30, list.get(2));
	}

	@Test
	void listIteratorSet() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		ListIterator<Integer> it = list.listIterator();
		it.next();
		it.set(99);
		assertEquals(99, list.get(0));
		assertEquals(20, list.get(1));
	}

	@Test
	void listIteratorOutOfBoundsThrows() {
		IntList list = new IntList();
		list.add(10);
		assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(2));
		assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
	}

	@Test
	void listIteratorNextIndexAndPreviousIndex() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		ListIterator<Integer> it = list.listIterator();
		assertEquals(0, it.nextIndex());
		assertEquals(-1, it.previousIndex());
		it.next();
		assertEquals(1, it.nextIndex());
		assertEquals(0, it.previousIndex());
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	@Test
	void addAll() {
		IntList list = new IntList();
		list.add(1);
		assertTrue(list.addAll(Arrays.asList(2, 3, 4)));
		assertEquals(4, list.size());
		assertEquals(4, list.get(3));
	}

	@Test
	void addAllAtPosition() {
		IntList list = new IntList();
		list.add(1);
		list.add(4);
		list.addAll(1, Arrays.asList(2, 3));
		assertEquals(4, list.size());
		assertEquals(1, list.get(0));
		assertEquals(2, list.get(1));
		assertEquals(3, list.get(2));
		assertEquals(4, list.get(3));
	}

	@Test
	void addAllEmptyCollection() {
		IntList list = new IntList();
		list.add(1);
		assertFalse(list.addAll(Arrays.asList()));
		assertEquals(1, list.size());
	}

	@Test
	void removeAll() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		assertTrue(list.removeAll(Arrays.asList(2, 4)));
		assertEquals(2, list.size());
		assertEquals(1, list.get(0));
		assertEquals(3, list.get(1));
	}

	@Test
	void removeAllNothingToRemove() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		assertFalse(list.removeAll(Arrays.asList(5, 6)));
		assertEquals(2, list.size());
	}

	@Test
	void retainAll() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		assertTrue(list.retainAll(Arrays.asList(2, 4)));
		assertEquals(2, list.size());
		assertEquals(2, list.get(0));
		assertEquals(4, list.get(1));
	}

	@Test
	void retainAllNothingToRemove() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		assertFalse(list.retainAll(Arrays.asList(1, 2, 3)));
		assertEquals(2, list.size());
	}

	@Test
	void containsAll() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		assertTrue(list.containsAll(Arrays.asList(1, 2)));
		assertFalse(list.containsAll(Arrays.asList(1, 4)));
	}

	@Test
	void containsAllEmptyCollectionReturnsTrue() {
		IntList list = new IntList();
		list.add(1);
		assertTrue(list.containsAll(Arrays.asList()));
	}

	@Test
	void removeAllNullThrows() {
		IntList list = new IntList();
		assertThrows(NullPointerException.class, () -> list.removeAll(null));
	}

	@Test
	void retainAllNullThrows() {
		IntList list = new IntList();
		assertThrows(NullPointerException.class, () -> list.retainAll(null));
	}

	// ------------------------------------------------------------------
	// replaceAll / forEach
	// ------------------------------------------------------------------

	@Test
	void replaceAll() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		list.replaceAll(x -> x * 10);
		assertEquals(10, list.get(0));
		assertEquals(20, list.get(1));
		assertEquals(30, list.get(2));
	}

	@Test
	void replaceAllNullOperatorThrows() {
		IntList list = new IntList();
		assertThrows(NullPointerException.class, () -> list.replaceAll(null));
	}

	@Test
	void forEach() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		int[] sum = { 0 };
		list.forEach(x -> sum[0] += x);
		assertEquals(6, sum[0]);
	}

	// ------------------------------------------------------------------
	// equals / hashCode
	// ------------------------------------------------------------------

	@Test
	void equalsSelfReturnsTrue() {
		IntList list = new IntList();
		assertEquals(list, list);
	}

	@Test
	void equalsSameElementsReturnsTrue() {
		IntList a = new IntList();
		a.add(1);
		a.add(2);
		IntList b = new IntList();
		b.add(1);
		b.add(2);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equalsDifferentSizeReturnsFalse() {
		IntList a = new IntList();
		a.add(1);
		IntList b = new IntList();
		b.add(1);
		b.add(2);
		assertNotEquals(a, b);
	}

	@Test
	void equalsDifferentElementsReturnsFalse() {
		IntList a = new IntList();
		a.add(1);
		a.add(2);
		IntList b = new IntList();
		b.add(1);
		b.add(3);
		assertNotEquals(a, b);
	}

	@Test
	void equalsArrayListWithSameElements() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		ArrayList<Integer> al = new ArrayList<>();
		al.add(1);
		al.add(2);
		assertEquals(list, al);
		assertEquals(al, list);
	}

	@Test
	void equalsNonListReturnsFalse() {
		IntList list = new IntList();
		assertFalse(list.equals("not a list"));
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsElements() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		String s = list.toString();
		assertTrue(s.contains("1"));
		assertTrue(s.contains("2"));
		assertTrue(s.contains("3"));
	}

	@Test
	void emptyToString() {
		IntList list = new IntList();
		assertEquals("[]", list.toString());
	}

	// ------------------------------------------------------------------
	// toArray
	// ------------------------------------------------------------------

	@Test
	void toArrayReturnsElements() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		Object[] arr = list.toArray();
		assertEquals(2, arr.length);
		assertEquals(1, arr[0]);
		assertEquals(2, arr[1]);
	}

	@Test
	void toArrayEmptyReturnsEmptyArray() {
		IntList list = new IntList();
		Object[] arr = list.toArray();
		assertEquals(0, arr.length);
	}

	@Test
	void toArrayTypedReturnsElements() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		Integer[] arr = list.toArray(new Integer[0]);
		assertEquals(2, arr.length);
		assertEquals(1, arr[0]);
		assertEquals(2, arr[1]);
	}

	@Test
	void toArrayTypedLargeArray() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		Integer[] arr = list.toArray(new Integer[10]);
		assertEquals(10, arr.length);
		assertEquals(1, arr[0]);
		assertEquals(2, arr[1]);
		assertNull(arr[2]);
	}

	@Test
	void toArrayTypedExactArray() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		Integer[] arr = new Integer[2];
		Integer[] result = list.toArray(arr);
		assertEquals(arr, (Object) result);
		assertEquals(1, arr[0]);
		assertEquals(2, arr[1]);
	}

	// ------------------------------------------------------------------
	// Stress and edge cases
	// ------------------------------------------------------------------

	@Test
	void largeNumberOfEntries() {
		IntList list = new IntList();
		for (int i = 0; i < 1000; i++) {
			list.add(i);
		}
		assertEquals(1000, list.size());
		for (int i = 0; i < 1000; i++) {
			assertEquals(i, list.get(i));
		}
	}

	@Test
	void repeatedAddAndRemoveStress() {
		IntList list = new IntList();
		for (int round = 0; round < 5; round++) {
			for (int i = 0; i < 200; i++) {
				list.add(i);
			}
			assertEquals(200, list.size());
			list.clear();
		}
		assertEquals(0, list.size());
	}

	@Test
	void zeroCapacityCreatesEmptyList() {
		IntList list = new IntList(0);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
		list.add(42);
		assertEquals(42, list.get(0));
	}

	@Test
	void implementsListInterface() {
		List<Integer> list = new IntList();
		list.add(10);
		list.add(20);
		list.add(30);
		assertEquals(3, list.size());
		assertEquals(20, list.get(1));
		assertTrue(list.remove(Integer.valueOf(20)));
		assertEquals(2, list.size());
	}

	@Test
	void randomAccess() {
		assertTrue(new IntList() instanceof java.util.RandomAccess);
	}

	@Test
	void indexOf() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(10);
		assertEquals(0, list.indexOf(10));
		assertEquals(1, list.indexOf(20));
		assertEquals(-1, list.indexOf(30));
	}

	@Test
	void lastIndexOf() {
		IntList list = new IntList();
		list.add(10);
		list.add(20);
		list.add(10);
		assertEquals(2, list.lastIndexOf(10));
		assertEquals(1, list.lastIndexOf(20));
		assertEquals(-1, list.lastIndexOf(30));
	}

	@Test
	void sublist() {
		IntList list = new IntList();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		List<Integer> sub = list.subList(1, 3);
		assertEquals(2, sub.size());
		assertEquals(2, sub.get(0));
		assertEquals(3, sub.get(1));
	}

	@Test
	void negativeValues() {
		IntList list = new IntList();
		list.add(-1);
		list.add(Integer.MIN_VALUE);
		list.add(Integer.MAX_VALUE);
		assertEquals(-1, list.get(0));
		assertEquals(Integer.MIN_VALUE, list.get(1));
		assertEquals(Integer.MAX_VALUE, list.get(2));
	}

	@Test
	void duplicateValues() {
		IntList list = new IntList();
		list.add(5);
		list.add(5);
		list.add(5);
		assertEquals(3, list.size());
		assertTrue(list.remove(Integer.valueOf(5)));
		assertEquals(2, list.size());
	}

	@Test
	void addAllWithNullsThrows() {
		IntList list = new IntList();
		List<Integer> source = new ArrayList<>();
		source.add(1);
		source.add(null);
		assertThrows(NullPointerException.class, () -> list.addAll(source));
	}
}
