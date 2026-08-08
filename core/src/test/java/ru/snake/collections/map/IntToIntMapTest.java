package ru.snake.collections.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntToIntMapTest {

    // ------------------------------------------------------------------
    // Construction & empty state
    // ------------------------------------------------------------------

    @Test
    void defaultConstructorCreatesEmptyMap() {
        IntToIntMap map = new IntToIntMap();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    void customCapacityConstructor() {
        IntToIntMap map = new IntToIntMap(32);
        assertEquals(0, map.size());
    }

    @Test
    void negativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IntToIntMap(-1));
    }

    // ------------------------------------------------------------------
    // put / get
    // ------------------------------------------------------------------

    @Test
    void putAndGetSingleEntry() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(10, map.get(1));
        assertEquals(1, map.size());
    }

    @Test
    void putReturnsPreviousValue() {
        IntToIntMap map = new IntToIntMap();
        int first = map.put(1, 10);
        assertEquals(0, first); // no previous mapping
        int second = map.put(1, 20);
        assertEquals(10, second);
        assertEquals(20, map.get(1));
    }

    @Test
    void putMultipleEntries() {
        IntToIntMap map = new IntToIntMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i * 10);
        }
        assertEquals(100, map.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i * 10, map.get(i));
        }
    }

    @Test
    void getMissingKeyReturnsZero() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(0, map.get(999));
    }

    @Test
    void getOrDefaultReturnsDefaultValueForMissingKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(-1, map.getOrDefault(999, -1));
        assertEquals(10, map.getOrDefault(1, -1));
    }

    @Test
    void zeroIsAValidKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(0, 42);
        assertTrue(map.containsKey(0));
        assertEquals(42, map.get(0));
    }

    @Test
    void zeroIsAValidValue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 0);
        assertTrue(map.containsKey(1));
        assertEquals(0, map.get(1));
        assertTrue(map.containsValue(0));
    }

    // ------------------------------------------------------------------
    // containsKey / containsValue
    // ------------------------------------------------------------------

    @Test
    void containsKeyPresentAndAbsent() {
        IntToIntMap map = new IntToIntMap();
        map.put(5, 50);
        assertTrue(map.containsKey(5));
        assertFalse(map.containsKey(6));
    }

    @Test
    void containsValuePresentAndAbsent() {
        IntToIntMap map = new IntToIntMap();
        map.put(5, 50);
        assertTrue(map.containsValue(50));
        assertFalse(map.containsValue(99));
    }

    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------

    @Test
    void removeExistingKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        int removed = map.remove(1);
        assertEquals(10, removed);
        assertEquals(0, map.size());
        assertFalse(map.containsKey(1));
    }

    @Test
    void removeMissingKeyReturnsZero() {
        IntToIntMap map = new IntToIntMap();
        assertEquals(0, map.remove(999));
        assertEquals(0, map.size());
    }

    @Test
    void removeDoesNotBreakOtherEntries() {
        IntToIntMap map = new IntToIntMap();
        // Insert a batch that may cause collisions
        for (int i = 0; i < 50; i++) {
            map.put(i, i * 3);
        }
        // Remove roughly half
        for (int i = 0; i < 50; i += 2) {
            int val = map.remove(i);
            assertEquals(i * 3, val);
            assertFalse(map.containsKey(i));
        }
        // Remaining entries must still be reachable
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
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(2));
    }

    @Test
    void isEmptyAfterClear() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.clear();
        assertTrue(map.isEmpty());
    }

    // ------------------------------------------------------------------
    // putAll
    // ------------------------------------------------------------------

    @Test
    void putAllCopiesEntries() {
        IntToIntMap source = new IntToIntMap();
        for (int i = 0; i < 30; i++) {
            source.put(i, i * 7);
        }
        IntToIntMap dest = new IntToIntMap();
        dest.putAll(source);
        for (int i = 0; i < 30; i++) {
            assertEquals(i * 7, dest.get(i));
        }
        assertEquals(30, dest.size());
    }

    // ------------------------------------------------------------------
    // Hash collisions — force many keys to the same bucket
    // ------------------------------------------------------------------

    @Test
    void handlesCollisionsGracefully() {
        // Keys that land at index 0 with cap=16 (mask=0xF):
        // hash(x) = x ^ (x >>> 16), then & 0xF.
        // x=0:    hash=0 → slot 0
        // x=15:   hash=15^0=15 → slot 15
        // x=16:   hash=16^0=16 → slot 0
        // x=31:   hash=31 → slot 15
        // So 0 and 16 collide at slot 0.
        IntToIntMap map = new IntToIntMap(16);
        int[] keys = { 0, 16, 32, 48, 64 };
        for (int k : keys) {
            map.put(k, k);
        }
        for (int k : keys) {
            assertEquals(k, map.get(k));
        }
        for (int k : keys) {
            assertEquals(k, map.remove(k));
        }
        assertEquals(0, map.size());
    }

    @Test
    void largeNumberOfEntriesSurvivesCollisions() {
        IntToIntMap map = new IntToIntMap();
        int n = 5_000;
        for (int i = 0; i < n; i++) {
            map.put(i, i * i);
        }
        assertEquals(n, map.size());
        for (int i = 0; i < n; i++) {
            assertEquals(i * i, map.get(i));
        }
    }

    // ------------------------------------------------------------------
    // Negative keys
    // ------------------------------------------------------------------

    @Test
    void negativeKeysWork() {
        IntToIntMap map = new IntToIntMap();
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
    // Resize stress
    // ------------------------------------------------------------------

    @Test
    void repeatedPutAndRemoveStress() {
        IntToIntMap map = new IntToIntMap(8);
        for (int round = 0; round < 10; round++) {
            for (int i = 0; i < 200; i++) {
                map.put(i, i);
            }
            for (int i = 0; i < 200; i++) {
                assertEquals(i, map.remove(i));
            }
            assertEquals(0, map.size());
        }
    }

    // ------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------

    @Test
    void toStringContainsEntries() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        String s = map.toString();
        assertTrue(s.contains("1=10"));
        assertTrue(s.contains("2=20"));
    }

    @Test
    void emptyToString() {
        IntToIntMap map = new IntToIntMap();
        assertEquals("{}", map.toString());
    }

    // ------------------------------------------------------------------
    // Map<Integer, Integer> interface
    // ------------------------------------------------------------------

    @Test
    void implementsMapInterface() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        assertEquals(2, map.size());
        assertEquals(10, map.get(1));
        assertEquals(20, map.get(2));
        assertNull(map.get(3));
    }

    @Test
    void mapPutReturnsPreviousValue() {
        Map<Integer, Integer> map = new IntToIntMap();
        assertNull(map.put(1, 10));
        assertEquals(10, map.put(1, 20));
        assertEquals(20, map.get(1));
    }

    @Test
    void mapRemoveReturnsNullForMissingKey() {
        Map<Integer, Integer> map = new IntToIntMap();
        assertNull(map.remove(1));
    }

    @Test
    void mapRemoveReturnsValueForExistingKey() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(10, map.remove(1));
        assertNull(map.get(1));
    }

    @Test
    void mapPutAllFromOtherMap() {
        Map<Integer, Integer> map = new IntToIntMap();
        Map<Integer, Integer> source = Map.of(1, 10, 2, 20);
        map.putAll(source);
        assertEquals(2, map.size());
        assertEquals(10, map.get(1));
        assertEquals(20, map.get(2));
    }

    @Test
    void mapGetOrDefault() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(10, map.getOrDefault(1, -1));
        assertEquals(-1, map.getOrDefault(2, -1));
    }

    @Test
    void mapContainsKeyAndValue() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        assertTrue(map.containsKey(1));
        assertFalse(map.containsKey(2));
        assertTrue(map.containsValue(10));
        assertFalse(map.containsValue(20));
    }

    @Test
    void mapKeySet() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        assertEquals(Set.of(1, 2), map.keySet());
    }

    @Test
    void mapValues() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        assertEquals(Set.of(10, 20), Set.copyOf(map.values()));
    }

    @Test
    void mapEntrySet() {
        Map<Integer, Integer> map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        assertEquals(2, map.entrySet().size());
        assertTrue(map.entrySet().contains(Map.entry(1, 10)));
        assertTrue(map.entrySet().contains(Map.entry(2, 20)));
    }

    @Test
    void mapForEach() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        int[] sum = { 0 };
        map.forEach((k, v) -> sum[0] += k + v);
        assertEquals(33, sum[0]); // (1+10) + (2+20)
    }

    @Test
    void mapNullKeyThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () -> map.put(null, 1));
    }

    @Test
    void mapNullValueThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () -> map.put(1, null));
    }

    // ------------------------------------------------------------------
    // constructor edge cases
    // ------------------------------------------------------------------

    @Test
    void zeroCapacityConstructor() {
        IntToIntMap map = new IntToIntMap(0);
        assertEquals(0, map.size());
        map.put(1, 10);
        assertEquals(10, map.get(1));
    }

    // ------------------------------------------------------------------
    // containsKey(Object) - non-Integer
    // ------------------------------------------------------------------

    @Test
    void containsKeyNonIntegerReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.containsKey((Object) "1"));
    }

    // ------------------------------------------------------------------
    // containsValue(Object) - non-Integer
    // ------------------------------------------------------------------

    @Test
    void containsValueNonIntegerReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.containsValue((Object) "10"));
    }

    // ------------------------------------------------------------------
    // Integer get(Object) - non-Integer
    // ------------------------------------------------------------------

    @Test
    void getObjectNonIntegerReturnsNull() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.get((Object) "1"));
    }

    // ------------------------------------------------------------------
    // Integer getOrDefault(Object, Integer) - non-Integer
    // ------------------------------------------------------------------

    @Test
    void getObjectOrDefaultNonIntegerReturnsDefault() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(-1, map.getOrDefault("1", -1));
    }

    // ------------------------------------------------------------------
    // Integer remove(Object) - non-Integer
    // ------------------------------------------------------------------

    @Test
    void removeObjectNonIntegerReturnsNull() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.remove((Object) "1"));
        assertEquals(1, map.size());
    }

    // ------------------------------------------------------------------
    // keySet() view - remove edge cases
    // ------------------------------------------------------------------

    @Test
    void keySetRemoveExisting() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        Set<Integer> ks = map.keySet();
        assertTrue(ks.remove(1));
        assertEquals(1, map.size());
        assertFalse(map.containsKey(1));
    }

    @Test
    void keySetRemoveNonIntegerReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Set<Integer> ks = map.keySet();
        assertFalse(ks.remove((Object) "1"));
        assertEquals(1, map.size());
    }

    @Test
    void keySetRemoveMissingReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Set<Integer> ks = map.keySet();
        assertFalse(ks.remove(99));
        assertEquals(1, map.size());
    }

    // ------------------------------------------------------------------
    // entrySet() view - contains edge cases
    // ------------------------------------------------------------------

    @Test
    void entrySetContainsNonEntryReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().contains((Object) "not an entry"));
    }

    @Test
    void entrySetContainsNonIntegerKeyReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().contains(Map.entry("1", 10)));
    }

    @Test
    void entrySetContainsValueMismatchReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().contains(Map.entry(1, 99)));
    }

    @Test
    void entrySetContainsMissingKeyReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().contains(Map.entry(99, 10)));
    }

    // ------------------------------------------------------------------
    // entrySet() view - remove edge cases
    // ------------------------------------------------------------------

    @Test
    void entrySetRemoveExisting() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        assertTrue(map.entrySet().remove(Map.entry(1, 10)));
        assertEquals(1, map.size());
        assertFalse(map.containsKey(1));
    }

    @Test
    void entrySetRemoveNonEntryReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().remove((Object) "not an entry"));
    }

    @Test
    void entrySetRemoveNonIntegerKeyReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().remove(Map.entry("1", 10)));
    }

    @Test
    void entrySetRemoveMissingKeyReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().remove(Map.entry(99, 10)));
    }

    @Test
    void entrySetRemoveValueMismatchReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertFalse(map.entrySet().remove(Map.entry(1, 99)));
    }

    // ------------------------------------------------------------------
    // forEach
    // ------------------------------------------------------------------

    @Test
    void forEachNullActionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertThrows(NullPointerException.class, () -> map.forEach(null));
    }

    // ------------------------------------------------------------------
    // replaceAll
    // ------------------------------------------------------------------

    @Test
    void replaceAllUpdatesValues() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        map.put(2, 20);
        map.replaceAll((k, v) -> v * 2);
        assertEquals(20, map.get(1));
        assertEquals(40, map.get(2));
    }

    @Test
    void replaceAllNullFunctionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () -> map.replaceAll(null));
    }

    @Test
    void replaceAllFunctionReturnsNullThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertThrows(NullPointerException.class, () ->
            map.replaceAll((k, v) -> null)
        );
    }

    // ------------------------------------------------------------------
    // computeIfAbsent
    // ------------------------------------------------------------------

    @Test
    void computeIfAbsentKeyPresentReturnsValue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(10, map.computeIfAbsent(1, k -> 99));
        assertEquals(10, map.get(1));
    }

    @Test
    void computeIfAbsentKeyAbsentPutsAndReturns() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(20, map.computeIfAbsent(2, k -> k * 10));
        assertEquals(20, map.get(2));
        assertEquals(2, map.size());
    }

    @Test
    void computeIfAbsentNullKeyThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.computeIfAbsent(null, k -> 1)
        );
    }

    @Test
    void computeIfAbsentNullFunctionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.computeIfAbsent(1, null)
        );
    }

    @Test
    void computeIfAbsentFunctionReturnsNullThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.computeIfAbsent(1, k -> null)
        );
    }

    // ------------------------------------------------------------------
    // computeIfPresent
    // ------------------------------------------------------------------

    @Test
    void computeIfPresentKeyPresentUpdatesValue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(30, map.computeIfPresent(1, (k, v) -> v * 3));
        assertEquals(30, map.get(1));
    }

    @Test
    void computeIfPresentKeyAbsentReturnsNull() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.computeIfPresent(2, (k, v) -> v * 3));
        assertEquals(1, map.size());
    }

    @Test
    void computeIfPresentReturnsNullRemovesKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.computeIfPresent(1, (k, v) -> null));
        assertEquals(0, map.size());
    }

    @Test
    void computeIfPresentNullKeyThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.computeIfPresent(null, (k, v) -> 1)
        );
    }

    @Test
    void computeIfPresentNullFunctionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertThrows(NullPointerException.class, () ->
            map.computeIfPresent(1, null)
        );
    }

    // ------------------------------------------------------------------
    // compute
    // ------------------------------------------------------------------

    @Test
    void computeKeyPresentUpdatesValue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(30, map.compute(1, (k, v) -> v * 3));
        assertEquals(30, map.get(1));
    }

    @Test
    void computeKeyPresentReturnsNullRemovesKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.compute(1, (k, v) -> null));
        assertEquals(0, map.size());
    }

    @Test
    void computeKeyAbsentPutsValue() {
        IntToIntMap map = new IntToIntMap();
        assertEquals(20, map.compute(1, (k, v) -> k * 20));
        assertEquals(20, map.get(1));
        assertEquals(1, map.size());
    }

    @Test
    void computeKeyAbsentReturnsNullNoOp() {
        IntToIntMap map = new IntToIntMap();
        assertNull(map.compute(1, (k, v) -> null));
        assertEquals(0, map.size());
    }

    @Test
    void computeNullKeyThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.compute(null, (k, v) -> 1)
        );
    }

    @Test
    void computeNullFunctionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () -> map.compute(1, null));
    }

    // ------------------------------------------------------------------
    // merge
    // ------------------------------------------------------------------

    @Test
    void mergeKeyAbsentPutsValue() {
        IntToIntMap map = new IntToIntMap();
        assertEquals(10, map.merge(1, 10, (old, newVal) -> old + newVal));
        assertEquals(10, map.get(1));
    }

    @Test
    void mergeKeyPresentUpdatesValue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertEquals(30, map.merge(1, 20, (old, newVal) -> old + newVal));
        assertEquals(30, map.get(1));
    }

    @Test
    void mergeReturnsNullRemovesKey() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        assertNull(map.merge(1, 20, (old, newVal) -> null));
        assertEquals(0, map.size());
    }

    @Test
    void mergeNullKeyThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.merge(null, 1, (a, b) -> a + b)
        );
    }

    @Test
    void mergeNullValueThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () ->
            map.merge(1, null, (a, b) -> a + b)
        );
    }

    @Test
    void mergeNullFunctionThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        assertThrows(NullPointerException.class, () -> map.merge(1, 10, null));
    }

    // ------------------------------------------------------------------
    // IntIntEntry
    // ------------------------------------------------------------------

    @Test
    void entrySetValueUpdatesBackingMap() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Map.Entry<Integer, Integer> entry = map.entrySet().iterator().next();
        Integer old = entry.setValue(42);
        assertEquals(10, old.intValue());
        assertEquals(42, map.get(1));
    }

    @Test
    void entrySetValueNullThrowsNpe() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Map.Entry<Integer, Integer> entry = map.entrySet().iterator().next();
        assertThrows(NullPointerException.class, () -> entry.setValue(null));
    }

    @Test
    void entryEqualsSelfReturnsTrue() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Map.Entry<Integer, Integer> entry = map.entrySet().iterator().next();
        assertEquals(entry, entry);
    }

    @Test
    void entryEqualsNonEntryReturnsFalse() {
        IntToIntMap map = new IntToIntMap();
        map.put(1, 10);
        Map.Entry<Integer, Integer> entry = map.entrySet().iterator().next();
        assertFalse(entry.equals("not an entry"));
    }

    @Test
    void entryToString() {
        IntToIntMap map = new IntToIntMap();
        map.put(42, 99);
        Map.Entry<Integer, Integer> entry = map.entrySet().iterator().next();
        assertEquals("42=99", entry.toString());
    }
}
