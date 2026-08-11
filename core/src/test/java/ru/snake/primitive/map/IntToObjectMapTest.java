package ru.snake.primitive.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntToObjectMapTest {

	// ------------------------------------------------------------------
	// Construction & empty state
	// ------------------------------------------------------------------

	@Test
	void defaultConstructorCreatesEmptyMap() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		IntToObjectMap<String> map = new IntToObjectMap<>(32);
		assertEquals(0, map.size());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () ->
			new IntToObjectMap<String>(-1)
		);
	}

	// ------------------------------------------------------------------
	// put / get
	// ------------------------------------------------------------------

	@Test
	void putAndGetSingleEntry() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertEquals("one", map.get(1));
		assertEquals(1, map.size());
	}

	@Test
	void putReturnsPreviousValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		String first = map.put(1, "one");
		assertNull(first);
		String second = map.put(1, "ONE");
		assertEquals("one", second);
		assertEquals("ONE", map.get(1));
	}

	@Test
	void putMultipleEntries() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		for (int i = 0; i < 100; i++) {
			map.put(i, "val-" + i);
		}
		assertEquals(100, map.size());
		for (int i = 0; i < 100; i++) {
			assertEquals("val-" + i, map.get(i));
		}
	}

	@Test
	void getMissingKeyReturnsNull() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.get(999));
	}

	@Test
	void getOrDefaultReturnsDefaultValueForMissingKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertEquals("default", map.getOrDefault(999, "default"));
		assertEquals("one", map.getOrDefault(1, "default"));
	}

	@Test
	void zeroIsAValidKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(0, "zero");
		assertTrue(map.containsKey(0));
		assertEquals("zero", map.get(0));
	}

	// ------------------------------------------------------------------
	// containsKey / containsValue
	// ------------------------------------------------------------------

	@Test
	void containsKeyPresentAndAbsent() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(5, "five");
		assertTrue(map.containsKey(5));
		assertFalse(map.containsKey(6));
	}

	@Test
	void containsValuePresentAndAbsent() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(5, "five");
		assertTrue(map.containsValue("five"));
		assertFalse(map.containsValue("six"));
	}

	// ------------------------------------------------------------------
	// remove
	// ------------------------------------------------------------------

	@Test
	void removeExistingKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		String removed = map.remove(1);
		assertEquals("one", removed);
		assertEquals(0, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void removeMissingKeyReturnsNull() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertNull(map.remove(999));
		assertEquals(0, map.size());
	}

	@Test
	void removeDoesNotBreakOtherEntries() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		for (int i = 0; i < 50; i++) {
			map.put(i, "val-" + i);
		}
		map.remove(25);
		assertEquals(49, map.size());
		assertFalse(map.containsKey(25));
		for (int i = 0; i < 50; i++) {
			if (i == 25) {
				assertFalse(map.containsKey(i));
			} else {
				assertEquals("val-" + i, map.get(i));
			}
		}
	}

	// ------------------------------------------------------------------
	// clear / isEmpty
	// ------------------------------------------------------------------

	@Test
	void clearEmptiesTheMap() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		for (int i = 0; i < 100; i++) {
			map.put(i, "val-" + i);
		}
		map.clear();
		assertEquals(0, map.size());
		for (int i = 0; i < 100; i++) {
			assertFalse(map.containsKey(i));
		}
	}

	@Test
	void isEmptyAfterClear() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.clear();
		assertTrue(map.isEmpty());
	}

	// ------------------------------------------------------------------
	// putAll
	// ------------------------------------------------------------------

	@Test
	void putAllCopiesEntries() {
		IntToObjectMap<String> source = new IntToObjectMap<>();
		for (int i = 0; i < 50; i++) {
			source.put(i, "val-" + i);
		}

		IntToObjectMap<String> target = new IntToObjectMap<>();
		target.putAll(source);

		assertEquals(50, target.size());
		for (int i = 0; i < 50; i++) {
			assertEquals("val-" + i, target.get(i));
		}
	}

	// ------------------------------------------------------------------
	// Collisions & stress
	// ------------------------------------------------------------------

	@Test
	void handlesCollisionsGracefully() {
		IntToObjectMap<String> map = new IntToObjectMap<>(16);
		int n = 0;
		for (int i = 0; i < 1000; i += 17) {
			map.put(i, "val-" + i);
			n++;
		}
		for (int i = 0; i < 1000; i += 17) {
			assertEquals("val-" + i, map.get(i));
		}
		assertEquals(n, map.size());
	}

	@Test
	void largeNumberOfEntriesSurvivesCollisions() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		for (int i = 0; i < 10_000; i++) {
			map.put(i, "val-" + i);
		}
		assertEquals(10_000, map.size());
		for (int i = 0; i < 10_000; i++) {
			assertEquals("val-" + i, map.get(i));
		}
	}

	@Test
	void negativeKeysWork() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(-1, "neg-one");
		map.put(Integer.MIN_VALUE, "min");
		map.put(-999999, "neg-big");
		assertEquals("neg-one", map.get(-1));
		assertEquals("min", map.get(Integer.MIN_VALUE));
		assertEquals("neg-big", map.get(-999999));
		assertEquals(3, map.size());
	}

	@Test
	void repeatedPutAndRemoveStress() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		for (int i = 0; i < 2000; i++) {
			map.put(i, "val-" + i);
		}
		for (int i = 0; i < 2000; i += 3) {
			map.remove(i);
		}
		for (int i = 0; i < 2000; i++) {
			if (i % 3 == 0) {
				assertFalse(map.containsKey(i));
			} else {
				assertEquals("val-" + i, map.get(i));
			}
		}
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsEntries() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		String s = map.toString();
		assertTrue(s.contains("1=one"));
		assertTrue(s.contains("2=two"));
	}

	@Test
	void emptyToString() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertEquals("{}", map.toString());
	}

	// ------------------------------------------------------------------
	// Map<Integer, V> interface
	// ------------------------------------------------------------------

	@Test
	void implementsMapInterface() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		assertEquals(2, map.size());
		assertEquals("one", map.get(1));
	}

	@Test
	void mapPutReturnsPreviousValue() {
		Map<Integer, String> map = new IntToObjectMap<>();
		assertNull(map.put(1, "one"));
		assertEquals("one", map.put(1, "ONE"));
		assertEquals("ONE", map.get(1));
	}

	@Test
	void mapRemoveReturnsNullForMissingKey() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.remove(999));
	}

	@Test
	void mapRemoveReturnsValueForExistingKey() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertEquals("one", map.remove(1));
	}

	@Test
	void mapPutAllFromOtherMap() {
		Map<Integer, String> map = new IntToObjectMap<>();
		Map<Integer, String> source = new HashMap<>();
		source.put(1, "one");
		source.put(2, "two");
		source.put(3, "three");
		map.putAll(source);
		assertEquals(3, map.size());
		assertEquals("one", map.get(1));
		assertEquals("two", map.get(2));
		assertEquals("three", map.get(3));
	}

	@Test
	void mapGetOrDefault() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertEquals("default", map.getOrDefault(999, "default"));
		assertEquals("one", map.getOrDefault(1, "default"));
	}

	@Test
	void mapContainsKeyAndValue() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(5, "five");
		assertTrue(map.containsKey(5));
		assertFalse(map.containsKey(6));
		assertTrue(map.containsValue("five"));
		assertFalse(map.containsValue("six"));
	}

	@Test
	void mapKeySet() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Integer> ks = map.keySet();
		assertEquals(2, ks.size());
		assertTrue(ks.contains(1));
		assertTrue(ks.contains(2));
	}

	@Test
	void mapValues() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		var vals = map.values();
		assertEquals(2, vals.size());
		assertTrue(vals.contains("one"));
		assertTrue(vals.contains("two"));
	}

	@Test
	void mapEntrySet() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Map.Entry<Integer, String>> entries = map.entrySet();
		assertEquals(2, entries.size());
	}

	@Test
	void mapForEach() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		Map<Integer, String> seen = new HashMap<>();
		map.forEach((k, v) -> seen.put(k, v));
		assertEquals(3, seen.size());
		assertEquals("one", seen.get(1));
		assertEquals("two", seen.get(2));
		assertEquals("three", seen.get(3));
	}

	@Test
	void mapNullKeyThrowsNpe() {
		Map<Integer, String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.put(null, "v"));
	}

	@Test
	void mapNullValueThrowsNpe() {
		Map<Integer, String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.put(1, null));
	}

	// ------------------------------------------------------------------
	// zero capacity
	// ------------------------------------------------------------------

	@Test
	void zeroCapacityConstructor() {
		IntToObjectMap<String> map = new IntToObjectMap<>(0);
		assertEquals(0, map.size());
		map.put(1, "one");
		assertEquals("one", map.get(1));
	}

	// ------------------------------------------------------------------
	// Non-Integer key handling (via Map interface)
	// ------------------------------------------------------------------

	@Test
	void containsKeyNonIntegerReturnsFalse() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertFalse(map.containsKey("not an integer"));
	}

	@Test
	void containsValueNonStringReturnsFalse() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertFalse(map.containsValue(123));
	}

	@Test
	void getObjectNonIntegerReturnsNull() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.get("not an integer"));
	}

	@Test
	void getObjectOrDefaultNonIntegerReturnsDefault() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertEquals("default", map.getOrDefault("not an integer", "default"));
	}

	@Test
	void removeObjectNonIntegerReturnsNull() {
		Map<Integer, String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.remove("not an integer"));
		assertEquals(1, map.size());
	}

	// ------------------------------------------------------------------
	// keySet operations
	// ------------------------------------------------------------------

	@Test
	void keySetRemoveExisting() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Integer> ks = map.keySet();
		assertTrue(ks.remove(1));
		assertEquals(1, map.size());
		assertFalse(map.containsKey(1));
		assertTrue(map.containsKey(2));
	}

	@Test
	void keySetRemoveNonIntegerReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Integer> ks = map.keySet();
		assertFalse(ks.remove("not an integer"));
		assertEquals(1, map.size());
	}

	@Test
	void keySetRemoveMissingReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Integer> ks = map.keySet();
		assertFalse(ks.remove(999));
		assertEquals(1, map.size());
	}

	// ------------------------------------------------------------------
	// entrySet operations
	// ------------------------------------------------------------------

	@Test
	void entrySetContainsNonEntryReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		assertFalse(es.contains("not an entry"));
	}

	@Test
	void entrySetContainsNonIntegerKeyReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Object, Object> fake = new HashMap<>();
		fake.put("not int", "one");
		assertFalse(es.contains(fake.entrySet().iterator().next()));
	}

	@Test
	void entrySetContainsValueMismatchReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Integer, String> fake = new HashMap<>();
		fake.put(1, "wrong");
		assertFalse(es.contains(fake.entrySet().iterator().next()));
	}

	@Test
	void entrySetContainsMissingKeyReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Integer, String> fake = new HashMap<>();
		fake.put(999, "one");
		assertFalse(es.contains(fake.entrySet().iterator().next()));
	}

	@Test
	void entrySetRemoveExisting() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Integer, String> fake = new HashMap<>();
		fake.put(1, "one");
		assertTrue(es.remove(fake.entrySet().iterator().next()));
		assertEquals(1, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void entrySetRemoveNonEntryReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		assertFalse(es.remove("not an entry"));
	}

	@Test
	void entrySetRemoveNonIntegerKeyReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Object, Object> fake = new HashMap<>();
		fake.put("not int", "one");
		assertFalse(es.remove(fake.entrySet().iterator().next()));
	}

	@Test
	void entrySetRemoveMissingKeyReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Integer, String> fake = new HashMap<>();
		fake.put(999, "one");
		assertFalse(es.remove(fake.entrySet().iterator().next()));
	}

	@Test
	void entrySetRemoveValueMismatchReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Set<Map.Entry<Integer, String>> es = map.entrySet();
		HashMap<Integer, String> fake = new HashMap<>();
		fake.put(1, "wrong");
		assertFalse(es.remove(fake.entrySet().iterator().next()));
		assertEquals(1, map.size());
	}

	// ------------------------------------------------------------------
	// forEach / replaceAll
	// ------------------------------------------------------------------

	@Test
	void forEachNullActionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.forEach(null));
	}

	@Test
	void replaceAllUpdatesValues() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.replaceAll((k, v) -> v.toUpperCase());
		assertEquals("ONE", map.get(1));
		assertEquals("TWO", map.get(2));
	}

	@Test
	void replaceAllNullFunctionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertThrows(NullPointerException.class, () -> map.replaceAll(null));
	}

	@Test
	void replaceAllFunctionReturnsNullThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertThrows(NullPointerException.class, () ->
			map.replaceAll((k, v) -> null)
		);
	}

	// ------------------------------------------------------------------
	// computeIfAbsent
	// ------------------------------------------------------------------

	@Test
	void computeIfAbsentKeyPresentReturnsValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		String result = map.computeIfAbsent(1, k -> "new");
		assertEquals("one", result);
		assertEquals(1, map.size());
	}

	@Test
	void computeIfAbsentKeyAbsentPutsAndReturns() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		String result = map.computeIfAbsent(1, k -> "val-" + k);
		assertEquals("val-1", result);
		assertEquals("val-1", map.get(1));
		assertEquals(1, map.size());
	}

	@Test
	void computeIfAbsentNullKeyThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent(null, k -> "v")
		);
	}

	@Test
	void computeIfAbsentNullFunctionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent(1, null)
		);
	}

	@Test
	void computeIfAbsentFunctionReturnsNullThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent(1, k -> null)
		);
	}

	// ------------------------------------------------------------------
	// computeIfPresent
	// ------------------------------------------------------------------

	@Test
	void computeIfPresentKeyPresentUpdatesValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		String result = map.computeIfPresent(1, (k, v) -> v.toUpperCase());
		assertEquals("ONE", result);
		assertEquals("ONE", map.get(1));
	}

	@Test
	void computeIfPresentKeyAbsentReturnsNull() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertNull(map.computeIfPresent(1, (k, v) -> v + "!"));
		assertEquals(0, map.size());
	}

	@Test
	void computeIfPresentReturnsNullRemovesKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.computeIfPresent(1, (k, v) -> null));
		assertEquals(0, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void computeIfPresentNullKeyThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfPresent(null, (k, v) -> v)
		);
	}

	@Test
	void computeIfPresentNullFunctionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfPresent(1, null)
		);
	}

	// ------------------------------------------------------------------
	// compute
	// ------------------------------------------------------------------

	@Test
	void computeKeyPresentUpdatesValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		String result = map.compute(1, (k, v) -> v.toUpperCase());
		assertEquals("ONE", result);
		assertEquals("ONE", map.get(1));
	}

	@Test
	void computeKeyPresentReturnsNullRemovesKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.compute(1, (k, v) -> null));
		assertEquals(0, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void computeKeyAbsentPutsValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		String result = map.compute(1, (k, v) -> "val-" + k);
		assertEquals("val-1", result);
		assertEquals("val-1", map.get(1));
	}

	@Test
	void computeKeyAbsentReturnsNullNoOp() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertNull(map.compute(1, (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeNullKeyThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.compute(null, (k, v) -> "v")
		);
	}

	@Test
	void computeNullFunctionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.compute(1, null));
	}

	// ------------------------------------------------------------------
	// merge
	// ------------------------------------------------------------------

	@Test
	void mergeKeyAbsentPutsValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		String result = map.merge(1, "one", (v1, v2) -> v1 + v2);
		assertEquals("one", result);
		assertEquals("one", map.get(1));
	}

	@Test
	void mergeKeyPresentUpdatesValue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		String result = map.merge(1, "two", (v1, v2) -> v1 + "-" + v2);
		assertEquals("one-two", result);
		assertEquals("one-two", map.get(1));
	}

	@Test
	void mergeReturnsNullRemovesKey() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		assertNull(map.merge(1, "two", (v1, v2) -> null));
		assertEquals(0, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void mergeNullKeyThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.merge(null, "v", (v1, v2) -> v1)
		);
	}

	@Test
	void mergeNullValueThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.merge(1, null, (v1, v2) -> v1)
		);
	}

	@Test
	void mergeNullFunctionThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.merge(1, "v", null));
	}

	// ------------------------------------------------------------------
	// Entry operations
	// ------------------------------------------------------------------

	@Test
	void entrySetValueUpdatesBackingMap() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Map.Entry<Integer, String> entry = map.entrySet().iterator().next();
		String old = entry.setValue("ONE");
		assertEquals("one", old);
		assertEquals("ONE", map.get(1));
	}

	@Test
	void entrySetValueNullThrowsNpe() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Map.Entry<Integer, String> entry = map.entrySet().iterator().next();
		assertThrows(NullPointerException.class, () -> entry.setValue(null));
	}

	@Test
	void entryEqualsSelfReturnsTrue() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Map.Entry<Integer, String> entry = map.entrySet().iterator().next();
		assertEquals(entry, entry);
	}

	@Test
	void entryEqualsNonEntryReturnsFalse() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Map.Entry<Integer, String> entry = map.entrySet().iterator().next();
		assertFalse(entry.equals("not an entry"));
	}

	@Test
	void entryToString() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Map.Entry<Integer, String> entry = map.entrySet().iterator().next();
		assertEquals("1=one", entry.toString());
	}

	// ------------------------------------------------------------------
	// Iterator remove operations
	// ------------------------------------------------------------------

	@Test
	void keySetIteratorRemove() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		Iterator<Integer> it = map.keySet().iterator();
		assertTrue(it.hasNext());
		it.next();
		it.remove();
		assertEquals(2, map.size());
	}

	@Test
	void keySetIteratorRemoveBeforeNextThrows() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Iterator<Integer> it = map.keySet().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void keySetIteratorRemoveTwiceThrows() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Iterator<Integer> it = map.keySet().iterator();
		it.next();
		it.remove();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void valuesIteratorRemove() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Iterator<String> it = map.values().iterator();
		it.next();
		it.remove();
		assertEquals(1, map.size());
	}

	@Test
	void valuesIteratorRemoveBeforeNextThrows() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Iterator<String> it = map.values().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void entrySetIteratorRemove() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Iterator<Map.Entry<Integer, String>> it = map.entrySet().iterator();
		it.next();
		it.remove();
		assertEquals(1, map.size());
	}

	@Test
	void entrySetIteratorRemoveBeforeNextThrows() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		Iterator<Map.Entry<Integer, String>> it = map.entrySet().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	// ------------------------------------------------------------------
	// keySet retainAll / clear
	// ------------------------------------------------------------------

	@Test
	void keySetRetainAll() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		map.put(4, "four");
		Set<Integer> ks = map.keySet();
		boolean changed = ks.retainAll(Arrays.asList(1, 3));
		assertTrue(changed);
		assertEquals(2, map.size());
		assertTrue(map.containsKey(1));
		assertFalse(map.containsKey(2));
		assertTrue(map.containsKey(3));
		assertFalse(map.containsKey(4));
	}

	@Test
	void keySetRetainAllKeepNone() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Integer> ks = map.keySet();
		boolean changed = ks.retainAll(Arrays.asList(99, 100));
		assertTrue(changed);
		assertEquals(0, map.size());
	}

	@Test
	void keySetRetainAllKeepAll() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		Set<Integer> ks = map.keySet();
		boolean changed = ks.retainAll(Arrays.asList(1, 2, 3));
		assertFalse(changed);
		assertEquals(2, map.size());
	}

	@Test
	void keySetClear() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.keySet().clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void entrySetClear() {
		IntToObjectMap<String> map = new IntToObjectMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.entrySet().clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}
}
