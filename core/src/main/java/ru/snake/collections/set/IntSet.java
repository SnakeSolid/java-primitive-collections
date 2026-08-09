package ru.snake.collections.set;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * A compact hash set of {@code int} values.
 *
 * <p>
 * Each stored integer is encoded so that the top 27 bits serve as the hash map
 * key and the bottom 5 bits select a single bit within the map value. One map
 * slot ({@code int}) can therefore hold up to 32 elements that share the same
 * 27-bit prefix.
 * </p>
 *
 * <p>
 * Slot occupancy is tracked directly in the {@code keys} array: a slot is
 * occupied if {@code keys[index] != -1} and empty if {@code keys[index] == -1}.
 * This sentinel is safe because valid keys have their lower 5 bits cleared
 * ({@code element & 0xFFFFFFE0}), so {@code -1} ({@code 0xFFFFFFFF}) can never
 * be a valid key.
 * </p>
 *
 * <p>
 * Implements {@link java.util.Set<Integer>} so it can be used wherever a
 * standard set is expected. {@code null} elements are not supported -
 * {@link NullPointerException} will be thrown if one is passed.
 * </p>
 *
 * <p>
 * This class is not thread-safe.
 * </p>
 */
public final class IntSet extends AbstractSet<Integer> {

    /** Mask for the bottom 5 bits - the bit position within a slot value. */
    private static final int BIT_MASK = 0x1F;

    /** Mask for the top 27 bits - the map key. */
    private static final int KEY_MASK = 0xFFFFFFE0;

    /** Initial table capacity. Always a power of two. */
    private static final int DEFAULT_CAPACITY = 16;

    /** Maximum table capacity. */
    private static final int MAX_CAPACITY = 1 << 30;

    /**
     * Load factor threshold - resize when occupied slots exceed this fraction.
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * Hash table keys - top 27 bits of each stored element.
     * A value of {@code -1} denotes an empty slot (safe sentinel because
     * valid keys always have their lower 5 bits cleared).
     */
    private int[] keys;

    /**
     * Hash table values - each {@code int} packs up to 32 elements. Bit
     * {@code j} being set means the element with key {@code keys[i]} and offset
     * {@code j} is present in the set.
     */
    private int[] values;

    /**
     * Tracks how many table slots are occupied (keys[i] != -1). Used for
     * load-factor checks and resizing.
     */
    private int occupiedCount;

    /** Number of distinct elements in the set. */
    private int size;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /**
     * Constructs an empty set with the default initial capacity (16) and load
     * factor (0.75).
     */
    public IntSet() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty set whose initial table size is the smallest power of
     * two not less than {@code initialCapacity}.
     *
     * @param initialCapacity
     *            the initial capacity
     * @throws IllegalArgumentException
     *             if {@code initialCapacity} is negative
     */
    public IntSet(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
                "initialCapacity: " + initialCapacity
            );
        }
        int cap = tableSizeFor(initialCapacity);
        keys = new int[cap];
        values = new int[cap];
        Arrays.fill(keys, -1);
        occupiedCount = 0;
    }

    // ------------------------------------------------------------------
    // Core helpers
    // ------------------------------------------------------------------

    /**
     * Extract the 27-bit key from an element value.
     */
    private static int keyOf(int element) {
        return element & KEY_MASK;
    }

    /**
     * Returns the bit mask for a given element's offset within its slot value.
     */
    private static int bitOf(int element) {
        return 1 << (element & BIT_MASK);
    }

    // ------------------------------------------------------------------
    // Set<Integer> implementation
    // ------------------------------------------------------------------

    @Override
    public boolean add(Integer element) {
        Objects.requireNonNull(element, "element must not be null");

        int key = keyOf(element);
        int bit = bitOf(element);

        int index = find(key);
        if (index >= 0) {
            // Slot exists - just set the bit
            if ((values[index] & bit) != 0) {
                return false; // already present
            }
            values[index] |= bit;
            size++;
            return true;
        }

        // Insert new slot
        index = insertSlot(key, bit);
        size++;

        // Resize if load factor exceeded (count live occupied slots)
        if (occupiedCount > (int) (keys.length * LOAD_FACTOR)) {
            resize(keys.length * 2);
        }

        return true;
    }

    @Override
    public boolean remove(Object element) {
        if (!(element instanceof Integer i)) {
            return false;
        }

        int key = keyOf(i);
        int bit = bitOf(i);

        int index = find(key);
        if (index < 0) {
            return false;
        }
        if ((values[index] & bit) == 0) {
            return false;
        }

        values[index] &= ~bit;
        size--;

        // If no bits remain in this slot, clear it and shift subsequent
        // probe-chain entries backward to keep chains intact without
        // needing tombstones.
        if (values[index] == 0) {
            keys[index] = -1;
            values[index] = 0;
            occupiedCount--;
            shiftBack(index);
        }

        return true;
    }

    @Override
    public boolean contains(Object element) {
        if (!(element instanceof Integer i)) {
            return false;
        }

        int key = keyOf(i);
        int bit = bitOf(i);

        int index = find(key);
        if (index < 0) {
            return false;
        }
        return (values[index] & bit) != 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        Arrays.fill(values, 0);
        Arrays.fill(keys, -1);
        occupiedCount = 0;
        size = 0;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntSetIterator();
    }

    @Override
    public Object[] toArray() {
        ArrayList<Integer> list = new ArrayList<>(size);
        collectAll(list);
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        ArrayList<Integer> list = new ArrayList<>(size);
        collectAll(list);
        return list.toArray(a);
    }

    /**
     * Appends all elements in this set to the given list.
     */
    private void collectAll(ArrayList<Integer> list) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == -1) {
                continue;
            }
            int base = keys[i];
            int word = values[i];
            while (word != 0) {
                int bit = Integer.numberOfTrailingZeros(word);
                list.add(base + bit);
                word &= ~(1 << bit);
            }
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean changed = false;
        for (Integer e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object e : c) {
            if (remove(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        // Collect indices of slots that become completely empty so we can
        // run backward-shift on them.
        java.util.ArrayList<Integer> emptySlots = new java.util.ArrayList<>();

        for (int i = keys.length - 1; i >= 0; i--) {
            if (keys[i] == -1) continue;
            int base = keys[i];
            int word = values[i];
            int newWord = word;
            while (word != 0) {
                int bit = Integer.numberOfTrailingZeros(word);
                int element = base + bit;
                if (!c.contains(element)) {
                    newWord &= ~(1 << bit);
                    size--;
                    changed = true;
                }
                word &= ~(1 << bit);
            }
            if (newWord == 0) {
                // Slot becomes empty — mark it and schedule backward-shift
                emptySlots.add(i);
            } else {
                values[i] = newWord;
            }
        }

        // Clear empty slots and shift chains backward.
        // Process from highest table index first so that shifts
        // for lower indices don't overwrite entries shifted by
        // higher-index shifts.
        for (int i = 0; i < emptySlots.size(); i++) {
            int slot = emptySlots.get(i);
            if (keys[slot] != -1) {
                keys[slot] = -1;
                values[slot] = 0;
                occupiedCount--;
                shiftBack(slot);
            }
        }
        return changed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof java.util.Set<?> that)) return false;
        if (that.size() != this.size()) return false;
        return containsAll(that);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == -1) {
                continue;
            }
            int base = keys[i];
            int word = values[i];
            while (word != 0) {
                int bit = Integer.numberOfTrailingZeros(word);
                h += Integer.hashCode(base + bit);
                word &= ~(1 << bit);
            }
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == -1) {
                continue;
            }
            int base = keys[i];
            int word = values[i];
            while (word != 0) {
                int bit = Integer.numberOfTrailingZeros(word);
                if (!first) sb.append(", ");
                sb.append(base + bit);
                first = false;
                word &= ~(1 << bit);
            }
        }
        sb.append(']');
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Hashing & lookup - Java HashMap style
    // ------------------------------------------------------------------

    /**
     * Compute the Java-style hash for a key.
     */
    private static int hash(int h) {
        return h ^ (h >>> 16);
    }

    /**
     * Find the table index for the given key, or -1 if not found.
     *
     * <p>Linear-probe through occupied slots. A slot is occupied if
     * {@code keys[index] != -1} (the sentinel {@code -1} denotes empty slots;
     * this is safe because valid keys have their lower 5 bits cleared).
     * Backward-shift deletion ensures chains never have gaps, so we stop at
     * the first empty slot.</p>
     */
    private int find(int key) {
        int mask = keys.length - 1;
        int index = hash(key) & mask;

        while (keys[index] != -1) {
            if (keys[index] == key) {
                return index;
            }
            index = (index + 1) & mask;
        }
        return -1;
    }

    /**
     * Insert a new key-value slot into the table using linear probing.
     *
     * @return the index where the entry was placed
     */
    private int insertSlot(int key, int value) {
        int mask = keys.length - 1;
        int index = hash(key) & mask;

        while (keys[index] != -1) {
            index = (index + 1) & mask;
        }

        keys[index] = key;
        values[index] = value;
        occupiedCount++;
        return index;
    }

    /**
     * Resize the table to a new capacity.
     */
    private void resize(int newCapacity) {
        if (newCapacity > MAX_CAPACITY) {
            return;
        }
        int[] oldKeys = keys;
        int[] oldValues = values;

        keys = new int[newCapacity];
        values = new int[newCapacity];
        Arrays.fill(keys, -1);
        occupiedCount = 0;
        size = 0;

        int mask = newCapacity - 1;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] == -1) {
                continue;
            }
            int k = oldKeys[i];
            int v = oldValues[i];
            int index = hash(k) & mask;
            while (keys[index] != -1) {
                index = (index + 1) & mask;
            }
            keys[index] = k;
            values[index] = v;
            occupiedCount++;
            size += Integer.bitCount(v);
        }
    }

    /**
     * Backward-shift deletion.
     *
     * <p>
     * After a slot has been cleared, scan forward through the probe chain. For
     * each subsequent entry whose original hash lands in the range that would
     * probe through the current hole, shift it back into the hole and move the
     * hole forward.  This keeps chains compact without tombstones.
     * </p>
     */
    private void shiftBack(int hole) {
        int mask = keys.length - 1;

        while (true) {
            int next = (hole + 1) & mask;
            if (keys[next] == -1) {
                // Chain ends — nothing left to shift
                break;
            }

            int key = keys[next];
            int rehash = hash(key) & mask;

            // The entry at 'next' can be shifted back into 'hole' iff its
            // true hash position wraps through 'hole' before reaching 'next'.
            // Equivalently: the distance from rehash to hole (wrapping)
            // is strictly less than the distance from rehash to next.
            if (distance(rehash, hole, mask) < distance(rehash, next, mask)) {
                keys[hole] = key;
                values[hole] = values[next];
                keys[next] = -1;
                values[next] = 0;
                hole = next;
            } else {
                break;
            }
        }
    }

    /**
     * Wrapping distance from {@code from} to {@code to} in a table of size
     * {@code mask + 1}.
     */
    private static int distance(int from, int to, int mask) {
        return (to - from) & mask;
    }

    /**
     * Returns the smallest power of two greater than or equal to {@code cap}.
     */
    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n < 0 ? 1 : n >= MAX_CAPACITY ? MAX_CAPACITY : n + 1;
    }

    // ------------------------------------------------------------------
    // Bulk operations
    // ------------------------------------------------------------------

    /**
     * Copies all elements from the given set into this set.
     */
    public void putAll(IntSet other) {
        for (int i = 0; i < other.keys.length; i++) {
            if (other.keys[i] == -1) {
                continue;
            }
            int base = other.keys[i];
            int word = other.values[i];
            while (word != 0) {
                int bit = Integer.numberOfTrailingZeros(word);
                add(base + bit);
                word &= ~(1 << bit);
            }
        }
    }

    // ------------------------------------------------------------------
    // Iterator
    // ------------------------------------------------------------------

    private final class IntSetIterator implements Iterator<Integer> {

        /** Current table index being scanned. */
        private int idx = 0;
        /** Remaining bits in values[idx] that haven't been visited yet. */
        private int remaining = 0;
        /** Whether there is a next element. */
        private boolean hasMore = false;
        /** The next element value to return. */
        private int nextValue = 0;

        IntSetIterator() {
            findNext();
        }

        private void findNext() {
            hasMore = false;

            while (idx < keys.length) {
                if (keys[idx] != -1 && values[idx] != 0) {
                    remaining = values[idx];
                    break;
                }
                idx++;
            }

            if (idx < keys.length) {
                int bit = Integer.numberOfTrailingZeros(remaining);
                nextValue = keys[idx] + bit;
                remaining &= ~(1 << bit);
                hasMore = true;
            }
        }

        @Override
        public boolean hasNext() {
            return hasMore;
        }

        @Override
        public Integer next() {
            if (!hasMore) {
                throw new java.util.NoSuchElementException();
            }
            int result = nextValue;
            if (remaining != 0) {
                // More bits in this slot
                int bit = Integer.numberOfTrailingZeros(remaining);
                nextValue = keys[idx] + bit;
                remaining &= ~(1 << bit);
                hasMore = true;
            } else {
                // Move to next slot
                idx++;
                findNext();
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
