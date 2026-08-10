package ru.snake.primitive.map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
 * Tests for {@link ObjectMap}.
 *
 * Mirrors the structure of {@link IntToIntMapTest} but adapted for the generic
 * {@code <K, V>} variant.
 */
class ObjectMapTest {

	// ------------------------------------------------------------------
	// Construction
	// ------------------------------------------------------------------

	@Test
	void defaultConstructorCreatesEmptyMap() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void customCapacityConstructor() {
		ObjectMap<String, String> map = new ObjectMap<>(32);
		assertEquals(0, map.size());
		map.put("a", "b");
		assertEquals(1, map.size());
	}

	@Test
	void negativeCapacityThrows() {
		assertThrows(IllegalArgumentException.class, () ->
			new ObjectMap<String, String>(-1)
		);
	}

	@Test
	void zeroCapacityConstructor() {
		ObjectMap<String, String> map = assertDoesNotThrow(() ->
			new ObjectMap<>(0)
		);
		map.put("x", "y");
		assertEquals(1, map.size());
		assertEquals("y", map.get("x"));
	}

	// ------------------------------------------------------------------
	// Core operations
	// ------------------------------------------------------------------

	@Test
	void putAndGetSingleEntry() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("one", 1);
		assertEquals(1, map.size());
		assertEquals(1, map.get("one"));
	}

	@Test
	void putReturnsPreviousValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertNull(map.put("key", "value1"));
		assertEquals("value1", map.put("key", "value2"));
		assertEquals("value2", map.get("key"));
	}

	@Test
	void putMultipleEntries() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "alpha");
		map.put("b", "beta");
		map.put("c", "gamma");
		assertEquals(3, map.size());
		assertEquals("alpha", map.get("a"));
		assertEquals("beta", map.get("b"));
		assertEquals("gamma", map.get("c"));
	}

	@Test
	void getMissingKeyReturnsNull() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		assertNull(map.get("missing"));
	}

	@Test
	void getOrDefaultReturnsDefaultValueForMissingKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("existing", "present");
		assertEquals("default", map.getOrDefault("missing", "default"));
		assertEquals("present", map.getOrDefault("existing", "default"));
	}

	@Test
	void containsKeyPresentAndAbsent() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("key", 1);
		assertTrue(map.containsKey("key"));
		assertFalse(map.containsKey("missing"));
	}

	@Test
	void containsValuePresentAndAbsent() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("key", "value");
		assertTrue(map.containsValue("value"));
		assertFalse(map.containsValue("other"));
	}

	@Test
	void removeExistingKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("key", "value");
		assertEquals("value", map.remove("key"));
		assertEquals(0, map.size());
		assertNull(map.get("key"));
	}

	@Test
	void removeMissingKeyReturnsNull() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertNull(map.remove("nonexistent"));
	}

	@Test
	void removeDoesNotBreakOtherEntries() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "alpha");
		map.put("b", "beta");
		map.put("c", "gamma");
		map.remove("b");
		assertEquals(2, map.size());
		assertEquals("alpha", map.get("a"));
		assertEquals("gamma", map.get("c"));
		assertNull(map.get("b"));
	}

	@Test
	void clearEmptiesTheMap() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		map.put("b", "2");
		map.put("c", "3");
		map.clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void isEmptyAfterClear() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.isEmpty());
		map.clear();
		assertTrue(map.isEmpty());
	}

	@Test
	void putAllCopiesEntries() {
		ObjectMap<String, String> source = new ObjectMap<>();
		source.put("a", "1");
		source.put("b", "2");

		ObjectMap<String, String> dest = new ObjectMap<>();
		dest.put("c", "3");
		dest.putAll(source);

		assertEquals(3, dest.size());
		assertEquals("1", dest.get("a"));
		assertEquals("2", dest.get("b"));
		assertEquals("3", dest.get("c"));
	}

	// ------------------------------------------------------------------
	// Collisions and stress
	// ------------------------------------------------------------------

	@Test
	void handlesCollisionsGracefully() {
		ObjectMap<String, Integer> map = new ObjectMap<>(2);
		String[] keys = new String[50];
		for (int i = 0; i < 50; i++) {
			keys[i] = String.valueOf(i);
			map.put(keys[i], i);
		}
		for (int i = 0; i < 50; i++) {
			assertEquals(i, map.get(keys[i]));
			assertTrue(map.containsKey(keys[i]));
		}
		assertEquals(50, map.size());
	}

	@Test
	void largeNumberOfEntriesSurvivesCollisions() {
		ObjectMap<String, String> map = new ObjectMap<>();
		for (int i = 0; i < 1000; i++) {
			map.put("key-" + i, "value-" + i);
		}
		assertEquals(1000, map.size());
		for (int i = 0; i < 1000; i++) {
			assertEquals("value-" + i, map.get("key-" + i));
		}
	}

	@Test
	void repeatedPutAndRemoveStress() {
		ObjectMap<Integer, String> map = new ObjectMap<>();
		for (int round = 0; round < 10; round++) {
			for (int i = 0; i < 50; i++) {
				map.put(i, "v-" + i);
			}
			for (int i = 0; i < 50; i++) {
				assertEquals("v-" + i, map.get(i));
			}
			for (int i = 0; i < 50; i++) {
				assertEquals("v-" + i, map.remove(i));
			}
			assertEquals(0, map.size());
		}
	}

	// ------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------

	@Test
	void toStringContainsEntries() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 1);
		map.put("b", 2);
		String s = map.toString();
		assertTrue(s.contains("a=1"));
		assertTrue(s.contains("b=2"));
		assertTrue(s.startsWith("{"));
		assertTrue(s.endsWith("}"));
	}

	@Test
	void emptyToString() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		assertEquals("{}", map.toString());
	}

	// ------------------------------------------------------------------
	// Map interface implementation
	// ------------------------------------------------------------------

	@Test
	void implementsMapInterface() {
		Map<String, Integer> map = new ObjectMap<>();
		map.put("x", 10);
		assertEquals(10, map.get("x"));
		assertEquals(1, map.size());
	}

	@Test
	void mapPutReturnsPreviousValue() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		assertNull(map.put("k", 1));
		assertEquals(1, map.put("k", 2));
	}

	@Test
	void mapRemoveReturnsNullForMissingKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertNull(map.remove("missing"));
	}

	@Test
	void mapRemoveReturnsValueForExistingKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("k", "v");
		assertEquals("v", map.remove("k"));
	}

	@Test
	void mapPutAllFromOtherMap() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
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
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("x", "found");
		assertEquals("found", map.getOrDefault("x", "def"));
		assertEquals("def", map.getOrDefault("y", "def"));
	}

	@Test
	void mapContainsKeyAndValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("k", "v");
		assertTrue(map.containsKey("k"));
		assertFalse(map.containsKey("no"));
		assertTrue(map.containsValue("v"));
		assertFalse(map.containsValue("no"));
	}

	@Test
	void mapKeySet() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Set<String> keys = map.keySet();
		assertEquals(2, keys.size());
		assertTrue(keys.contains("a"));
		assertTrue(keys.contains("b"));
	}

	@Test
	void mapValues() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 1);
		map.put("b", 2);
		var vals = map.values();
		assertEquals(2, vals.size());
		assertTrue(vals.contains(1));
		assertTrue(vals.contains(2));
	}

	@Test
	void mapEntrySet() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Set<Map.Entry<String, Integer>> entries = map.entrySet();
		assertEquals(2, entries.size());
		boolean foundA = false,
			foundB = false;
		for (Map.Entry<String, Integer> e : entries) {
			if (e.getKey().equals("a") && e.getValue() == 1) foundA = true;
			if (e.getKey().equals("b") && e.getValue() == 2) foundB = true;
		}
		assertTrue(foundA);
		assertTrue(foundB);
	}

	@Test
	void mapForEach() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
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
	void mapNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.put(null, "v"));
	}

	@Test
	void mapNullValueThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.put("k", null));
	}

	@Test
	void getNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.get(null));
	}

	// ------------------------------------------------------------------
	// Key set / entry set view mutation
	// ------------------------------------------------------------------

	@Test
	void keySetRemoveExisting() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		map.put("b", "2");
		assertTrue(map.keySet().remove("a"));
		assertEquals(1, map.size());
		assertFalse(map.containsKey("a"));
		assertEquals("2", map.get("b"));
	}

	@Test
	void keySetRemoveNullReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.keySet().remove(null));
	}

	@Test
	void keySetRemoveMissingReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.keySet().remove("missing"));
	}

	@Test
	void keySetClearClearsMap() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		map.put("b", "2");
		map.keySet().clear();
		assertEquals(0, map.size());
	}

	@Test
	void entrySetContainsNonEntryReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.entrySet().contains((Object) "not an entry"));
	}

	@Test
	void entrySetContainsNullKeyReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertFalse(
			map.entrySet().contains(new AbstractMap.SimpleEntry<>(null, "v"))
		);
	}

	@Test
	void entrySetContainsValueMismatchReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(
			map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", "2"))
		);
	}

	@Test
	void entrySetContainsMissingKeyReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(
			map.entrySet().contains(new AbstractMap.SimpleEntry<>("b", "1"))
		);
	}

	@Test
	void entrySetRemoveExisting() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		map.put("b", "2");
		assertTrue(
			map.entrySet().remove(new AbstractMap.SimpleEntry<>("a", "1"))
		);
		assertEquals(1, map.size());
		assertFalse(map.containsKey("a"));
	}

	@Test
	void entrySetRemoveNonEntryReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.entrySet().remove((Object) "not an entry"));
	}

	@Test
	void entrySetRemoveNullKeyReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertFalse(
			map.entrySet().remove(new AbstractMap.SimpleEntry<>(null, "v"))
		);
	}

	@Test
	void entrySetRemoveMissingKeyReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(
			map.entrySet().remove(new AbstractMap.SimpleEntry<>("b", "1"))
		);
	}

	@Test
	void entrySetRemoveValueMismatchReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(
			map.entrySet().remove(new AbstractMap.SimpleEntry<>("a", "2"))
		);
		assertEquals(1, map.size());
	}

	@Test
	void entrySetClearClearsMap() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		map.put("b", "2");
		map.entrySet().clear();
		assertEquals(0, map.size());
	}

	// ------------------------------------------------------------------
	// Functional operations
	// ------------------------------------------------------------------

	@Test
	void forEachNullActionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.forEach(null));
	}

	@Test
	void replaceAllUpdatesValues() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.replaceAll((k, v) -> v * 10);
		assertEquals(10, map.get("a"));
		assertEquals(20, map.get("b"));
	}

	@Test
	void replaceAllNullFunctionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.replaceAll(null));
	}

	@Test
	void replaceAllFunctionReturnsNullThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertThrows(NullPointerException.class, () ->
			map.replaceAll((k, v) -> null)
		);
	}

	@Test
	void computeIfAbsentKeyPresentReturnsValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertEquals("1", map.computeIfAbsent("a", k -> "new"));
		assertEquals("1", map.get("a"));
	}

	@Test
	void computeIfAbsentKeyAbsentPutsAndReturns() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertEquals("new", map.computeIfAbsent("a", k -> "new"));
		assertEquals("new", map.get("a"));
	}

	@Test
	void computeIfAbsentNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent(null, k -> "v")
		);
	}

	@Test
	void computeIfAbsentNullFunctionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent("k", null)
		);
	}

	@Test
	void computeIfAbsentFunctionReturnsNullThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfAbsent("k", k -> null)
		);
	}

	@Test
	void computeIfPresentKeyPresentUpdatesValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertEquals("updated", map.computeIfPresent("a", (k, v) -> "updated"));
		assertEquals("updated", map.get("a"));
	}

	@Test
	void computeIfPresentKeyAbsentReturnsNull() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertNull(map.computeIfPresent("a", (k, v) -> "updated"));
		assertEquals(0, map.size());
	}

	@Test
	void computeIfPresentReturnsNullRemovesKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertNull(map.computeIfPresent("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeIfPresentNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfPresent(null, (k, v) -> "v")
		);
	}

	@Test
	void computeIfPresentNullFunctionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.computeIfPresent("k", null)
		);
	}

	@Test
	void computeKeyPresentUpdatesValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertEquals("updated", map.compute("a", (k, v) -> "updated"));
		assertEquals("updated", map.get("a"));
	}

	@Test
	void computeKeyPresentReturnsNullRemovesKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertNull(map.compute("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeKeyAbsentPutsValue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertEquals("new", map.compute("a", (k, v) -> "new"));
		assertEquals("new", map.get("a"));
	}

	@Test
	void computeKeyAbsentReturnsNullNoOp() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertNull(map.compute("a", (k, v) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void computeNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.compute(null, (k, v) -> "v")
		);
	}

	@Test
	void computeNullFunctionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.compute("k", null));
	}

	@Test
	void mergeKeyAbsentPutsValue() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		assertEquals(1, map.merge("a", 1, (oldVal, newVal) -> oldVal + newVal));
		assertEquals(1, map.get("a"));
	}

	@Test
	void mergeKeyPresentUpdatesValue() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		assertEquals(
			11,
			map.merge("a", 1, (oldVal, newVal) -> oldVal + newVal)
		);
		assertEquals(11, map.get("a"));
	}

	@Test
	void mergeReturnsNullRemovesKey() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertNull(map.merge("a", "2", (o, n) -> null));
		assertEquals(0, map.size());
	}

	@Test
	void mergeNullKeyThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.merge(null, "v", (o, n) -> o + n)
		);
	}

	@Test
	void mergeNullValueThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.merge("k", null, (o, n) -> o + n)
		);
	}

	@Test
	void mergeNullFunctionThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () ->
			map.merge("k", "v", null)
		);
	}

	// ------------------------------------------------------------------
	// Entry semantics
	// ------------------------------------------------------------------

	@Test
	void entrySetValueUpdatesBackingMap() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		entry.setValue("updated");
		assertEquals("updated", map.get("a"));
	}

	@Test
	void entrySetValueNullThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		assertThrows(NullPointerException.class, () -> entry.setValue(null));
	}

	@Test
	void entryEqualsSelfReturnsTrue() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		assertTrue(entry.equals(entry));
	}

	@Test
	void entryEqualsNonEntryReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		assertFalse(entry.equals("not an entry"));
	}

	@Test
	void entryEqualsDifferentMapEntry() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		AbstractMap.SimpleEntry<String, String> other =
			new AbstractMap.SimpleEntry<>("a", "1");
		assertEquals(entry, other);
	}

	@Test
	void entryHashCode() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		AbstractMap.SimpleEntry<String, String> other =
			new AbstractMap.SimpleEntry<>("a", "1");
		assertEquals(entry.hashCode(), other.hashCode());
	}

	@Test
	void entryToString() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		Map.Entry<String, String> entry = map.entrySet().iterator().next();
		assertEquals("a=1", entry.toString());
	}

	// ------------------------------------------------------------------
	// Iterator behavior
	// ------------------------------------------------------------------

	@Test
	void keySetIterator() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
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
		ObjectMap<String, Integer> map = new ObjectMap<>();
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
	// Null handling in getOrDefault / containsKey / containsValue
	// ------------------------------------------------------------------

	@Test
	void getOrDefaultNullKeyReturnsDefault() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertEquals("def", map.getOrDefault(null, "def"));
	}

	@Test
	void containsKeyNullReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.containsKey(null));
	}

	@Test
	void containsValueNullReturnsFalse() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertFalse(map.containsValue(null));
	}

	@Test
	void removeNullKeyReturnsNull() {
		ObjectMap<String, String> map = new ObjectMap<>();
		map.put("a", "1");
		assertNull(map.remove(null));
	}

	// ------------------------------------------------------------------
	// putAll null guard
	// ------------------------------------------------------------------

	@Test
	void putAllNullMapThrowsNpe() {
		ObjectMap<String, String> map = new ObjectMap<>();
		assertThrows(NullPointerException.class, () -> map.putAll(null));
	}

	// ------------------------------------------------------------------
	// Custom key/value types
	// ------------------------------------------------------------------

	@Test
	void customKeyAndValueTypes() {
		ObjectMap<Person, Address> map = new ObjectMap<>();
		Person p1 = new Person("Alice", 30);
		Person p2 = new Person("Bob", 25);
		Address a1 = new Address("NYC");
		Address a2 = new Address("LA");

		map.put(p1, a1);
		map.put(p2, a2);

		assertEquals(a1, map.get(p1));
		assertEquals(a2, map.get(p2));
		assertEquals(2, map.size());

		// Verify equality-based lookup works
		Person p1Clone = new Person("Alice", 30);
		assertEquals(a1, map.get(p1Clone));
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
			if (this == o) return true;
			if (!(o instanceof Person)) return false;
			Person p = (Person) o;
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

	static class Address {

		final String city;

		Address(String city) {
			this.city = city;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Address)) return false;
			Address a = (Address) o;
			return city.equals(a.city);
		}

		@Override
		public int hashCode() {
			return city.hashCode();
		}

		@Override
		public String toString() {
			return city;
		}
	}

	// ------------------------------------------------------------------
	// Equality with other maps
	// ------------------------------------------------------------------

	@Test
	void emptyMapEquality() {
		ObjectMap<String, String> map = new ObjectMap<>();
		HashMap<String, String> hm = new HashMap<>();
		// ObjectMap does NOT implement Map.equals, so inequality is expected
		assertNotEquals(map, hm);
	}

	// ------------------------------------------------------------------
	// Iterator remove()
	// ------------------------------------------------------------------

	@Test
	void keySetIteratorRemove() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);
		map.put("c", 30);

		Iterator<String> it = map.keySet().iterator();
		String first = it.next();
		it.remove();
		assertFalse(map.containsKey(first));
		assertEquals(2, map.size());

		// Continue iterating
		java.util.List<String> rest = new java.util.ArrayList<>();
		while (it.hasNext()) rest.add(it.next());
		assertEquals(2, rest.size());
	}

	@Test
	void keySetIteratorRemoveBeforeNextThrows() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		Iterator<String> it = map.keySet().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void keySetIteratorRemoveTwiceThrows() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);
		Iterator<String> it = map.keySet().iterator();
		it.next();
		it.remove();
		it.next();
		it.remove();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void valuesIteratorRemove() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);
		map.put("c", 30);

		Iterator<Integer> it = map.values().iterator();
		it.next();
		it.remove();
		assertEquals(2, map.size());
	}

	@Test
	void valuesIteratorRemoveBeforeNextThrows() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		Iterator<Integer> it = map.values().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void entrySetIteratorRemove() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);
		map.put("c", 30);

		Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
		Map.Entry<String, Integer> entry = it.next();
		it.remove();
		assertFalse(map.containsKey(entry.getKey()));
		assertEquals(2, map.size());
	}

	@Test
	void entrySetIteratorRemoveBeforeNextThrows() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	// ------------------------------------------------------------------
	// keySet retainAll, contains, clear
	// ------------------------------------------------------------------

	@Test
	void keySetRetainAll() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);
		map.put("c", 30);

		Set<String> keySet = map.keySet();
		assertTrue(keySet.retainAll(java.util.Set.of("a", "c")));
		assertEquals(2, map.size());
		assertTrue(map.containsKey("a"));
		assertFalse(map.containsKey("b"));
		assertTrue(map.containsKey("c"));
	}

	@Test
	void keySetRetainAllKeepNone() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);

		Set<String> keySet = map.keySet();
		assertTrue(keySet.retainAll(java.util.Set.of()));
		assertEquals(0, map.size());
	}

	@Test
	void keySetRetainAllKeepAll() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);

		Set<String> keySet = map.keySet();
		assertFalse(keySet.retainAll(java.util.Set.of("a", "b", "c")));
		assertEquals(2, map.size());
	}

	@Test
	void keySetContains() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);

		Set<String> keySet = map.keySet();
		assertTrue(keySet.contains("a"));
		assertFalse(keySet.contains("c"));
		assertFalse(keySet.contains(null));
	}

	@Test
	void keySetClear() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);

		map.keySet().clear();
		assertEquals(0, map.size());
	}

	@Test
	void entrySetClear() {
		ObjectMap<String, Integer> map = new ObjectMap<>();
		map.put("a", 10);
		map.put("b", 20);

		map.entrySet().clear();
		assertEquals(0, map.size());
	}
}
