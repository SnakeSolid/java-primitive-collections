package ru.snake.primitive.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ObjectToIntMap}.
 *
 * Combines the testing patterns from both {@link IntToIntMapTest} and
 * {@link ObjectMapTest} — Object keys with primitive int values.
 */
class ObjectToIntMapTest {

	// ------------------------------------------------------------------
	// Construction & empty state
	// ------------------------------------------------------------------

	@Test
	void defaultConstructorCreatesEmptyMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>(32);
		assertEquals(0, map.size());
		map.put("a", 1);
		assertEquals(1, map.size());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () -> new ObjectToIntMap<String>(-1));
	}

	@Test
	void zeroCapacityConstructor() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>(0);
		assertEquals(0, map.size());
		map.put("x", 42);
		assertEquals(1, map.size());
		assertEquals(42, map.get("x"));
	}

	// ------------------------------------------------------------------
	// Primitive put / get
	// ------------------------------------------------------------------

	@Test
	void putAndGetSingleEntry() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("one", 1);
		assertEquals(1, map.size());
		assertEquals(1, map.get("one"));
	}

	@Test
	void putReturnsPreviousValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		int first = map.putInt("key", 10);
		assertEquals(0, first); // no previous mapping
		int second = map.putInt("key", 20);
		assertEquals(10, second);
		assertEquals(20, map.get("key"));
	}

	@Test
	void putMultipleEntries() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 10);
		map.put("b", 20);
		map.put("c", 30);
		assertEquals(3, map.size());
		assertEquals(10, map.get("a"));
		assertEquals(20, map.get("b"));
		assertEquals(30, map.get("c"));
	}

	@Test
	void getMissingKeyReturnsZero() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("one", 1);
		assertEquals(0, map.getInt("missing"));
	}

	@Test
	void getOrDefaultReturnsDefaultValueForMissingKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("existing", 42);
		assertEquals(-1, map.getOrDefault("missing", -1));
		assertEquals(42, map.getOrDefault("existing", -1));
	}

	@Test
	void zeroIsAValidValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("zero", 0);
		assertTrue(map.containsKey("zero"));
		assertEquals(0, map.get("zero"));
		assertTrue(map.containsValue(0));
	}

	// ------------------------------------------------------------------
	// containsKey / containsValue
	// ------------------------------------------------------------------

	@Test
	void containsKeyPresentAndAbsent() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("key", 1);
		assertTrue(map.containsKey("key"));
		assertFalse(map.containsKey("missing"));
	}

	@Test
	void containsValuePresentAndAbsent() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("key", 50);
		assertTrue(map.containsValue(50));
		assertFalse(map.containsValue(99));
	}

	// ------------------------------------------------------------------
	// remove
	// ------------------------------------------------------------------

	@Test
	void removeExistingKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("key", 10);
		int removed = map.delete("key");
		assertEquals(10, removed);
		assertEquals(0, map.size());
		assertFalse(map.containsKey("key"));
	}

	@Test
	void removeMissingKeyReturnsZero() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(0, map.delete("nonexistent"));
		assertEquals(0, map.size());
	}

	@Test
	void removeDoesNotBreakOtherEntries() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		map.delete("b");
		assertEquals(2, map.size());
		assertEquals(1, map.get("a"));
		assertEquals(3, map.get("c"));
	}

	@Test
	void removeDoesNotBreakAfterManyInsertions() {
		ObjectToIntMap<Integer> map = new ObjectToIntMap<>();
		for (int i = 0; i < 50; i++) {
			map.put(i, i * 3);
		}
		for (int i = 0; i < 50; i += 2) {
			int val = map.delete(i);
			assertEquals(i * 3, val);
			assertFalse(map.containsKey(i));
		}
		for (int i = 1; i < 50; i += 2) {
			assertTrue(map.containsKey(i), "key " + i + " lost after removals");
			assertEquals(i * 3, map.get(i));
		}
	}

	// ------------------------------------------------------------------
	// clear / isEmpty / size
	// ------------------------------------------------------------------

	@Test
	void clearEmptiesTheMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
		assertFalse(map.containsKey("a"));
		assertFalse(map.containsKey("b"));
	}

	@Test
	void isEmptyAfterClear() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.isEmpty());
		map.clear();
		assertTrue(map.isEmpty());
	}

	// ------------------------------------------------------------------
	// putAll
	// ------------------------------------------------------------------

	@Test
	void putAllCopiesEntriesFromSameType() {
		ObjectToIntMap<String> source = new ObjectToIntMap<>();
		source.put("a", 1);
		source.put("b", 2);

		ObjectToIntMap<String> dest = new ObjectToIntMap<>();
		dest.put("c", 3);
		dest.putAll(source);

		assertEquals(3, dest.size());
		assertEquals(1, dest.get("a"));
		assertEquals(2, dest.get("b"));
		assertEquals(3, dest.get("c"));
	}

	// ------------------------------------------------------------------
	// Collisions and stress
	// ------------------------------------------------------------------

	@Test
	void handlesCollisionsGracefully() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>(2);
		String[] keys = new String[50];
		for (int i = 0; i < 50; i++) {
			keys[i] = String.valueOf(i);
			map.put(keys[i], i * 7);
		}
		for (int i = 0; i < 50; i++) {
			assertEquals(i * 7, map.get(keys[i]));
			assertTrue(map.containsKey(keys[i]));
		}
		assertEquals(50, map.size());
	}

	@Test
	void largeNumberOfEntriesSurvivesCollisions() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		for (int i = 0; i < 1000; i++) {
			map.put("key-" + i, i * i);
		}
		assertEquals(1000, map.size());
		for (int i = 0; i < 1000; i++) {
			assertEquals(i * i, map.get("key-" + i));
		}
	}

	@Test
	void repeatedPutAndRemoveStress() {
		ObjectToIntMap<Integer> map = new ObjectToIntMap<>();
		for (int round = 0; round < 10; round++) {
			for (int i = 0; i < 50; i++) {
				map.put(i, i);
			}
			for (int i = 0; i < 50; i++) {
				assertEquals(i, map.get(i));
			}
			for (int i = 0; i < 50; i++) {
				assertEquals(i, map.remove(i));
			}
			assertEquals(0, map.size());
		}
	}

	// ------------------------------------------------------------------
	// Negative keys (Integer keys)
	// ------------------------------------------------------------------

	@Test
	void negativeIntegerKeysWork() {
		ObjectToIntMap<Integer> map = new ObjectToIntMap<>();
		map.put(-1, 1);
		map.put(-2, 2);
		map.put(Integer.MIN_VALUE, 3);
		map.put(Integer.MAX_VALUE, 4);
		assertEquals(1, map.get(-1));
		assertEquals(2, map.get(-2));
		assertEquals(3, map.get(Integer.MIN_VALUE));
		assertEquals(4, map.get(Integer.MAX_VALUE));
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsEntries() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 10);
		map.put("b", 20);
		String s = map.toString();
		assertTrue(s.contains("a=10"));
		assertTrue(s.contains("b=20"));
		assertTrue(s.startsWith("{"));
		assertTrue(s.endsWith("}"));
	}

	@Test
	void emptyToString() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals("{}", map.toString());
	}

	// ------------------------------------------------------------------
	// Map<K, Integer> interface
	// ------------------------------------------------------------------

	@Test
	void implementsMapInterface() {
		Map<String, Integer> map = new ObjectToIntMap<>();
		map.put("x", 10);
		assertEquals(10, map.get("x"));
		assertEquals(1, map.size());
	}

	@Test
	void mapPutReturnsPreviousValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertNull(map.put("k", 1));
		assertEquals(1, map.put("k", 2));
	}

	@Test
	void mapRemoveReturnsNullForMissingKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertNull(map.remove("missing"));
	}

	@Test
	void mapRemoveReturnsValueForExistingKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("k", 10);
		assertEquals(10, map.remove("k"));
		assertNull(map.get("k"));
	}

	@Test
	void mapPutAllFromOtherMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		HashMap<String, Integer> source = new HashMap<>();
		source.put("a", 1);
		source.put("b", 2);
		map.putAll(source);
		assertEquals(2, map.size());
		assertEquals(1, map.get("a"));
		assertEquals(2, map.get("b"));
	}

	@Test
	void mapGetOrDefault() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("x", 10);
		assertEquals(10, map.getOrDefault("x", -1));
		assertEquals(-1, map.getOrDefault("y", -1));
	}

	@Test
	void mapContainsKeyAndValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("k", 10);
		assertTrue(map.containsKey("k"));
		assertFalse(map.containsKey("no"));
		assertTrue(map.containsValue(10));
		assertFalse(map.containsValue(20));
	}

	@Test
	void mapContainsValueNonIntegerReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("k", 10);
		assertFalse(map.containsValue((Object) "10"));
	}

	@Test
	void mapKeySet() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Set<String> keys = map.keySet();
		assertEquals(2, keys.size());
		assertTrue(keys.contains("a"));
		assertTrue(keys.contains("b"));
	}

	@Test
	void mapValues() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		var vals = map.values();
		assertEquals(2, vals.size());
		assertTrue(vals.contains(1));
		assertTrue(vals.contains(2));
	}

	@Test
	void mapEntrySet() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Set<Map.Entry<String, Integer>> entries = map.entrySet();
		assertEquals(2, entries.size());
		assertTrue(entries.contains(Map.entry("a", 1)));
		assertTrue(entries.contains(Map.entry("b", 2)));
	}

	@Test
	void mapForEach() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Set<String> keys = new HashSet<>();
		Set<Integer> vals = new HashSet<>();
		map.forEach((k, v) -> {
			keys.add(k);
			vals.add(v);
		});
		assertTrue(keys.containsAll(Set.of("a", "b")));
		assertTrue(vals.containsAll(Set.of(1, 2)));
	}

	// ------------------------------------------------------------------
	// Guard clauses — null keys/values
	// ------------------------------------------------------------------

	@Test
	void primitivePutNullKeyThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.put(null, 1));
	}

	@Test
	void mapPutNullKeyThrowsNpe() {
		Map<String, Integer> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.put(null, 1));
	}

	@Test
	void mapPutNullValueThrowsNpe() {
		Map<String, Integer> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.put("k", null));
	}

	@Test
	void getNullKeyReturnsZero() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(0, map.getInt(null));
	}

	@Test
	void mapGetNullKeyReturnsNull() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertNull(map.get((Object) null));
	}

	@Test
	void containsKeyNullReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.containsKey((Object) null));
	}

	@Test
	void removeNullKeyReturnsNull() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertNull(map.remove((Object) null));
	}

	@Test
	void getOrDefaultNullKeyReturnsDefault() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertEquals(-1, map.getOrDefault((Object) null, -1));
	}

	// ------------------------------------------------------------------
	// Key set / entry set view mutation
	// ------------------------------------------------------------------

	@Test
	void keySetRemoveExisting() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		assertTrue(map.keySet().remove("a"));
		assertEquals(1, map.size());
		assertFalse(map.containsKey("a"));
		assertEquals(2, map.get("b"));
	}

	@Test
	void keySetRemoveNullReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.keySet().remove(null));
	}

	@Test
	void keySetRemoveMissingReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.keySet().remove("missing"));
	}

	@Test
	void keySetClearClearsMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.keySet().clear();
		assertEquals(0, map.size());
	}

	@Test
	void entrySetContainsNonEntryReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().contains((Object) "not an entry"));
	}

	@Test
	void entrySetContainsNullKeyReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertFalse(map.entrySet().contains(new AbstractMap.SimpleEntry<>(null, 1)));
	}

	@Test
	void entrySetContainsValueMismatchReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", 2)));
	}

	@Test
	void entrySetContainsMissingKeyReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().contains(new AbstractMap.SimpleEntry<>("b", 1)));
	}

	@Test
	void entrySetRemoveExisting() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		assertTrue(map.entrySet().remove(new AbstractMap.SimpleEntry<>("a", 1)));
		assertEquals(1, map.size());
		assertFalse(map.containsKey("a"));
	}

	@Test
	void entrySetRemoveNonEntryReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().remove((Object) "not an entry"));
	}

	@Test
	void entrySetRemoveNullKeyReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertFalse(map.entrySet().remove(new AbstractMap.SimpleEntry<>(null, 1)));
	}

	@Test
	void entrySetRemoveMissingKeyReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().remove(new AbstractMap.SimpleEntry<>("b", 1)));
	}

	@Test
	void entrySetRemoveValueMismatchReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertFalse(map.entrySet().remove(new AbstractMap.SimpleEntry<>("a", 2)));
		assertEquals(1, map.size());
	}

	@Test
	void entrySetClearClearsMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.entrySet().clear();
		assertEquals(0, map.size());
	}

	// ------------------------------------------------------------------
	// Functional operations
	// ------------------------------------------------------------------

	@Test
	void forEachNullActionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.forEach(null));
	}

	@Test
	void forEachIteratesAllEntries() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 10);
		map.put("b", 20);
		int[] sum = { 0 };
		map.forEach((k, v) -> sum[0] += v);
		assertEquals(30, sum[0]);
	}

	@Test
	void replaceAllUpdatesValues() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.replaceAll((k, v) -> v * 10);
		assertEquals(10, map.get("a"));
		assertEquals(20, map.get("b"));
	}

	@Test
	void replaceAllNullFunctionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.replaceAll(null));
	}

	@Test
	void replaceAllFunctionReturnsNullThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertThrows(NullPointerException.class, () -> map.replaceAll((k, v) -> null));
	}

	@Test
	void computeIfAbsentKeyPresentReturnsValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertEquals(1, map.computeIfAbsent("a", k -> 99));
		assertEquals(1, map.get("a"));
	}

	@Test
	void computeIfAbsentKeyAbsentPutsAndReturns() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(99, map.computeIfAbsent("a", k -> 99));
		assertEquals(99, map.get("a"));
		assertEquals(1, map.size());
	}

	@Test
	void computeIfAbsentNullKeyThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.computeIfAbsent(null, k -> 1));
	}

	@Test
	void computeIfAbsentNullFunctionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.computeIfAbsent("k", null));
	}

	@Test
	void computeIfAbsentFunctionReturnsNullThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.computeIfAbsent("k", k -> null));
	}

	@Test
	void computeIfPresentKeyPresentUpdatesValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertEquals(10, map.computeIfPresent("a", (k, v) -> v * 10));
		assertEquals(10, map.get("a"));
	}

	@Test
	void computeIfPresentKeyAbsentReturnsNull() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertNull(map.computeIfPresent("b", (k, v) -> v * 10));
		assertEquals(1, map.size());
	}

	@Test
	void computeIfPresentReturnsNullRemovesKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertNull(map.computeIfPresent("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeIfPresentNullKeyThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.computeIfPresent(null, (k, v) -> 1));
	}

	@Test
	void computeIfPresentNullFunctionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.computeIfPresent("k", null));
	}

	@Test
	void computeKeyPresentUpdatesValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertEquals(10, map.compute("a", (k, v) -> v * 10));
		assertEquals(10, map.get("a"));
	}

	@Test
	void computeKeyPresentReturnsNullRemovesKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		assertNull(map.compute("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeKeyAbsentPutsValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(99, map.compute("a", (k, v) -> 99));
		assertEquals(99, map.get("a"));
		assertEquals(1, map.size());
	}

	@Test
	void computeKeyAbsentReturnsNullNoOp() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertNull(map.compute("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeNullKeyThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.compute(null, (k, v) -> 1));
	}

	@Test
	void computeNullFunctionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.compute("k", null));
	}

	@Test
	void mergeKeyAbsentPutsValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertEquals(10, map.merge("a", 10, (oldVal, newVal) -> oldVal + newVal));
		assertEquals(10, map.get("a"));
	}

	@Test
	void mergeKeyPresentUpdatesValue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 10);
		assertEquals(30, map.merge("a", 20, (oldVal, newVal) -> oldVal + newVal));
		assertEquals(30, map.get("a"));
	}

	@Test
	void mergeReturnsNullRemovesKey() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 10);
		assertNull(map.merge("a", 20, (oldVal, newVal) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void mergeNullKeyThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.merge(null, 1, (o, n) -> o + n));
	}

	@Test
	void mergeNullValueThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.merge("k", null, (o, n) -> o + n));
	}

	@Test
	void mergeNullFunctionThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.merge("k", 1, null));
	}

	// ------------------------------------------------------------------
	// Entry semantics
	// ------------------------------------------------------------------

	@Test
	void entrySetValueUpdatesBackingMap() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		Integer old = entry.setValue(42);
		assertEquals(1, old.intValue());
		assertEquals(42, map.get("a"));
	}

	@Test
	void entrySetValueNullThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		assertThrows(NullPointerException.class, () -> entry.setValue(null));
	}

	@Test
	void entryEqualsSelfReturnsTrue() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		assertTrue(entry.equals(entry));
	}

	@Test
	void entryEqualsNonEntryReturnsFalse() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		assertFalse(entry.equals("not an entry"));
	}

	@Test
	void entryEqualsDifferentMapEntry() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		AbstractMap.SimpleEntry<String, Integer> other = new AbstractMap.SimpleEntry<>("a", 1);
		assertEquals(entry, other);
	}

	@Test
	void entryHashCode() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		AbstractMap.SimpleEntry<String, Integer> other = new AbstractMap.SimpleEntry<>("a", 1);
		assertEquals(entry.hashCode(), other.hashCode());
	}

	@Test
	void entryToString() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 42);
		Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
		assertEquals("a=42", entry.toString());
	}

	// ------------------------------------------------------------------
	// Iterator behavior
	// ------------------------------------------------------------------

	@Test
	void keySetIterator() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);

		Set<String> collected = new HashSet<>();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			collected.add(it.next());
		}
		assertTrue(collected.containsAll(Set.of("a", "b", "c")));
	}

	@Test
	void entrySetIterator() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		map.put("x", 10);
		map.put("y", 20);

		Set<Map.Entry<String, Integer>> collected = new HashSet<>();
		Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			collected.add(it.next());
		}
		assertEquals(2, collected.size());
	}

	// ------------------------------------------------------------------
	// putAll null guard
	// ------------------------------------------------------------------

	@Test
	void putAllNullMapThrowsNpe() {
		ObjectToIntMap<String> map = new ObjectToIntMap<>();
		assertThrows(NullPointerException.class, () -> map.putAll(null));
	}

	// ------------------------------------------------------------------
	// Custom key/value types
	// ------------------------------------------------------------------

	@Test
	void customKeyTypes() {
		ObjectToIntMap<Person> map = new ObjectToIntMap<>();
		Person p1 = new Person("Alice", 30);
		Person p2 = new Person("Bob", 25);

		map.put(p1, 100);
		map.put(p2, 200);

		assertEquals(100, map.get(p1));
		assertEquals(200, map.get(p2));
		assertEquals(2, map.size());

		// Verify equality-based lookup works
		Person p1Clone = new Person("Alice", 30);
		assertEquals(100, map.get(p1Clone));
	}

	static class Person {

		final String name;
		final int age;

		Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Person p))
				return false;
			return name.equals(p.name) && age == p.age;
		}

		@Override
		public int hashCode() {
			return name.hashCode() * 31 + age;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
