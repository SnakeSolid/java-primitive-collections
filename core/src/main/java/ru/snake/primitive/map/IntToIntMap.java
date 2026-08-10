package ru.snake.primitive.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import ru.snake.primitive.set.IntBitSet;

/**
 * A simple hash map from {@code int} keys to {@code int} values, backed by
 * parallel primitive arrays. Uses Java's standard hash strategy
 * ({@code hashCode &amp; mask}) with linear probing for collision resolution.
 *
 * <p>
 * Implements {@link Map<Integer, Integer>} so it can be used wherever a
 * standard map is expected. {@code null} keys and values are not supported —
 * {@link NullPointerException} will be thrown if one is passed.
 *
 * <p>
 * This class is not thread-safe.
 * </p>
 */
public final class IntToIntMap implements Map<Integer, Integer> {

	/** Initial table capacity. Always a power of two. */
	private static final int DEFAULT_CAPACITY = 16;

	/** Maximum table capacity. */
	private static final int MAX_CAPACITY = 1 << 30;

	/**
	 * Load factor threshold — resize when size exceeds capacity * LOAD_FACTOR.
	 */
	private static final float LOAD_FACTOR = 0.75f;

	/** Keys array. */
	private int[] keys;

	/** Values array — parallel to {@code keys}. */
	private int[] values;

	/**
	 * Tracks which table slots hold live entries.
	 */
	private IntBitSet occupied;

	/** Number of live key-value mappings. */
	private int size;

	// ------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------

	/**
	 * Constructs an empty map with the default initial capacity (16) and load
	 * factor (0.75).
	 */
	public IntToIntMap() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs an empty map whose initial table size is the smallest power of
	 * two not less than {@code initialCapacity}.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is negative
	 */
	public IntToIntMap(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException(
				"initialCapacity: " + initialCapacity
			);
		}
		int cap = tableSizeFor(initialCapacity);
		keys = new int[cap];
		values = new int[cap];
		occupied = new IntBitSet(cap);
	}

	// ------------------------------------------------------------------
	// Core primitive operations
	// ------------------------------------------------------------------

	/**
	 * Associates the specified value with the specified key. If the key is
	 * already present, the old value is replaced.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the previous value associated with {@code key}, or 0 if there was
	 *         no mapping (use {@link #containsKey} to disambiguate)
	 */
	public int put(int key, int value) {
		int result = find(key);
		if (result >= 0) {
			int index = result;
			int old = values[index];
			values[index] = value;
			return old;
		}

		// key not found – decode the first empty slot and insert directly
		int index = -result - 1;
		occupied.set(index);
		keys[index] = key;
		values[index] = value;
		size++;

		if (size > (int) (keys.length * LOAD_FACTOR)) {
			resize(keys.length * 2);
		}

		return 0;
	}

	/**
	 * Returns the value to which the specified key is mapped, or 0 if this map
	 * contains no mapping for the key.
	 *
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value, or 0 if no mapping exists (use {@link #containsKey} to
	 *         disambiguate)
	 */
	public int get(int key) {
		int index = find(key);
		return index < 0 ? 0 : values[index];
	}

	/**
	 * Returns the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @param key
	 *            the key whose associated value is to be returned
	 * @param defaultValue
	 *            the value to return if no mapping exists
	 * @return the value, or {@code defaultValue} if no mapping exists
	 */
	public int getOrDefault(int key, int defaultValue) {
		int index = find(key);
		return index < 0 ? defaultValue : values[index];
	}

	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * key.
	 *
	 * @param key
	 *            the key whose presence is tested
	 * @return {@code true} if the key is present
	 */
	public boolean containsKey(int key) {
		return find(key) >= 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return key instanceof Integer && find((Integer) key) >= 0;
	}

	/**
	 * Returns {@code true} if this map maps one or more keys to the specified
	 * value.
	 *
	 * @param value
	 *            the value whose presence is tested
	 * @return {@code true} if the value is present
	 */
	public boolean containsValue(int value) {
		return containsValue0(value);
	}

	@Override
	public boolean containsValue(Object value) {
		if (!(value instanceof Integer)) {
			return false;
		}
		return containsValue0((Integer) value);
	}

	private boolean containsValue0(int value) {
		for (int i = 0; i < keys.length; i++) {
			if (occupied.get(i) && values[i] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the mapping for the specified key (if present).
	 *
	 * @param key
	 *            the key to remove
	 * @return the previous value, or 0 if no mapping existed
	 */
	public int remove(int key) {
		return remove0(key);
	}

	private int remove0(int key) {
		int index = find(key);
		if (index < 0) {
			return 0;
		}
		int old = values[index];
		size--;
		occupied.clear(index);
		keys[index] = 0;
		values[index] = 0;
		shiftBack(index);
		return old;
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of mappings
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns {@code true} if this map contains no key-value mappings.
	 *
	 * @return {@code true} if empty
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes all mappings from this map.
	 */
	public void clear() {
		occupied.clearAll();
		size = 0;
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	/**
	 * Copies all key-value pairs from the given map into this map.
	 */
	public void putAll(IntToIntMap other) {
		for (int i = 0; i < other.keys.length; i++) {
			if (other.occupied.get(i)) {
				put(other.keys[i], other.values[i]);
			}
		}
	}

	// ------------------------------------------------------------------
	// Map<Integer, Integer> implementation (boxing/unboxing)
	// ------------------------------------------------------------------

	@Override
	public Integer put(Integer key, Integer value) {
		if (key == null || value == null) {
			throw new NullPointerException();
		}
		int k = key;
		int v = value; // unbox
		int mask = keys.length - 1;
		int index = hash(k) & mask;

		while (occupied.get(index)) {
			if (keys[index] == k) {
				int old = values[index];
				values[index] = v;
				return old; // box
			}
			index = (index + 1) & mask;
		}

		occupied.set(index);
		keys[index] = k;
		values[index] = v;
		size++;

		if (size > (int) (keys.length * LOAD_FACTOR)) {
			resize(keys.length * 2);
		}

		return null;
	}

	@Override
	public Integer get(Object key) {
		if (!(key instanceof Integer)) {
			return null;
		}
		int index = find((Integer) key);
		return index < 0 ? null : values[index];
	}

	@Override
	public Integer getOrDefault(Object key, Integer defaultValue) {
		if (!(key instanceof Integer)) {
			return defaultValue;
		}
		int index = find((Integer) key);
		return index < 0 ? defaultValue : values[index];
	}

	@Override
	public Integer remove(Object key) {
		if (!(key instanceof Integer)) {
			return null;
		}
		int index = find((Integer) key);
		if (index < 0) {
			return null;
		}
		int old = values[index];
		size--;
		occupied.clear(index);
		keys[index] = 0;
		values[index] = 0;
		shiftBack(index);
		return old;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Integer> map) {
		for (Map.Entry<
			? extends Integer,
			? extends Integer
		> e : map.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public Set<Integer> keySet() {
		return new AbstractSet<>() {
			@Override
			public int size() {
				return size;
			}

			@Override
			public boolean contains(Object o) {
				return o instanceof Integer && find((Integer) o) >= 0;
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Integer)) {
					return false;
				}
				int index = find((Integer) o);
				if (index < 0) {
					return false;
				}
				size--;
				occupied.clear(index);
				keys[index] = 0;
				values[index] = 0;
				shiftBack(index);
				return true;
			}

			@Override
			public void clear() {
				IntToIntMap.this.clear();
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				for (int i = keys.length - 1; i >= 0; i--) {
					if (occupied.get(i)) {
						if (!c.contains(keys[i])) {
							occupied.clear(i);
							keys[i] = 0;
							values[i] = 0;
							size--;
							shiftBack(i);
							changed = true;
						}
					}
				}
				return changed;
			}

			@Override
			public Iterator<Integer> iterator() {
				return new KeyIterator();
			}
		};
	}

	@Override
	public Collection<Integer> values() {
		return new java.util.AbstractCollection<Integer>() {
			@Override
			public int size() {
				return size;
			}

			@Override
			public Iterator<Integer> iterator() {
				return new ValueIterator();
			}
		};
	}

	@Override
	public Set<Map.Entry<Integer, Integer>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public int size() {
				return size;
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry<?, ?>)) {
					return false;
				}
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				if (!(e.getKey() instanceof Integer)) {
					return false;
				}
				int index = find((Integer) e.getKey());
				return (
					index >= 0 &&
					e.getValue() != null &&
					e.getValue().equals(values[index])
				);
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry<?, ?>)) {
					return false;
				}
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				if (!(e.getKey() instanceof Integer)) {
					return false;
				}
				int index = find((Integer) e.getKey());
				if (index < 0) {
					return false;
				}
				// Only remove if the value also matches
				if (
					e.getValue() == null || !e.getValue().equals(values[index])
				) {
					return false;
				}
				size--;
				occupied.clear(index);
				keys[index] = 0;
				values[index] = 0;
				shiftBack(index);
				return true;
			}

			@Override
			public void clear() {
				IntToIntMap.this.clear();
			}

			@Override
			public Iterator<Map.Entry<Integer, Integer>> iterator() {
				return new EntryIterator();
			}
		};
	}

	/**
	 * Iterator over the keys of this map. Walks the internal arrays directly,
	 * skipping empty slots.
	 */
	private final class KeyIterator implements Iterator<Integer> {

		private int index = 0;
		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (occupied.get(i)) return true;
			}
			return false;
		}

		@Override
		public Integer next() {
			while (index < keys.length && !occupied.get(index)) {
				index++;
			}
			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}
			lastIndex = index;
			return keys[index++];
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}
			int key = keys[lastIndex];
			IntToIntMap.this.remove0(key);
			// After shiftBack, re-sync from current position
			index = lastIndex;
			lastIndex = -1;
		}
	}

	/**
	 * Iterator over the values of this map. Walks the internal arrays directly.
	 */
	private final class ValueIterator implements Iterator<Integer> {

		private int index = 0;
		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (occupied.get(i)) return true;
			}
			return false;
		}

		@Override
		public Integer next() {
			while (index < keys.length && !occupied.get(index)) {
				index++;
			}
			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}
			lastIndex = index;
			return values[index++];
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}
			int key = keys[lastIndex];
			IntToIntMap.this.remove0(key);
			index = lastIndex;
			lastIndex = -1;
		}
	}

	/**
	 * Iterator over the entries of this map.
	 */
	private final class EntryIterator
		implements Iterator<Map.Entry<Integer, Integer>>
	{

		private int index = 0;
		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (occupied.get(i)) return true;
			}
			return false;
		}

		@Override
		public Map.Entry<Integer, Integer> next() {
			while (index < keys.length && !occupied.get(index)) {
				index++;
			}
			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}
			lastIndex = index;
			return new IntIntEntry(keys[index], values[index++]);
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}
			int key = keys[lastIndex];
			IntToIntMap.this.remove0(key);
			index = lastIndex;
			lastIndex = -1;
		}
	}

	@Override
	public void forEach(BiConsumer<? super Integer, ? super Integer> action) {
		Objects.requireNonNull(action);
		for (int i = 0; i < keys.length; i++) {
			if (occupied.get(i)) {
				action.accept(keys[i], values[i]);
			}
		}
	}

	@Override
	public void replaceAll(
		BiFunction<? super Integer, ? super Integer, ? extends Integer> function
	) {
		Objects.requireNonNull(function);
		for (int i = 0; i < keys.length; i++) {
			if (!occupied.get(i)) {
				continue;
			}
			Integer result = function.apply(keys[i], values[i]);
			if (result == null) {
				throw new NullPointerException();
			}
			values[i] = result;
		}
	}

	@Override
	public Integer computeIfAbsent(
		Integer key,
		java.util.function.Function<
			? super Integer,
			? extends Integer
		> mappingFunction
	) {
		Objects.requireNonNull(mappingFunction);
		if (key == null) {
			throw new NullPointerException();
		}
		int index = find(key);
		if (index >= 0) {
			return values[index];
		}
		Integer newValue = mappingFunction.apply(key);
		if (newValue == null) {
			throw new NullPointerException();
		}
		put(key, newValue);
		return newValue;
	}

	@Override
	public Integer computeIfPresent(
		Integer key,
		BiFunction<
			? super Integer,
			? super Integer,
			? extends Integer
		> remappingFunction
	) {
		Objects.requireNonNull(remappingFunction);
		if (key == null) {
			throw new NullPointerException();
		}
		int index = find(key);
		if (index < 0) {
			return null;
		}
		Integer newValue = remappingFunction.apply(key, values[index]);
		if (newValue == null) {
			size--;
			occupied.clear(index);
			keys[index] = 0;
			values[index] = 0;
			shiftBack(index);
			return null;
		}
		values[index] = newValue;
		return newValue;
	}

	@Override
	public Integer compute(
		Integer key,
		BiFunction<
			? super Integer,
			? super Integer,
			? extends Integer
		> remappingFunction
	) {
		Objects.requireNonNull(remappingFunction);
		if (key == null) {
			throw new NullPointerException();
		}
		int index = find(key);
		Integer current = index >= 0 ? values[index] : null;
		Integer newValue = remappingFunction.apply(key, current);
		if (newValue == null) {
			if (index >= 0) {
				size--;
				occupied.clear(index);
				keys[index] = 0;
				values[index] = 0;
				shiftBack(index);
			}
			return null;
		}
		put(key, newValue);
		return newValue;
	}

	@Override
	public Integer merge(
		Integer key,
		Integer value,
		BiFunction<
			? super Integer,
			? super Integer,
			? extends Integer
		> remappingFunction
	) {
		Objects.requireNonNull(remappingFunction);
		if (key == null || value == null) {
			throw new NullPointerException();
		}
		int index = find(key);
		if (index < 0) {
			put(key, value);
			return value;
		}
		Integer newValue = remappingFunction.apply(values[index], value);
		if (newValue == null) {
			size--;
			occupied.clear(index);
			keys[index] = 0;
			values[index] = 0;
			shiftBack(index);
			return null;
		}
		values[index] = newValue;
		return newValue;
	}

	// ------------------------------------------------------------------
	// Hashing — Java HashMap style
	// ------------------------------------------------------------------

	/**
	 * Compute the hash for an int key.
	 */
	private static int hash(int h) {
		h ^= (h >>> 20) ^ (h >>> 12);
		h ^= (h >>> 7) ^ (h >>> 4);
		return h;
	}

	/**
	 * Find the table index for the given key, or encode the first empty slot.
	 *
	 * <p>
	 * Returns a non-negative index if the key is found. If not found, returns
	 * {@code -(firstEmpty + 1)}, where {@code firstEmpty} is the index of the
	 * first empty slot encountered during the linear probe. The caller can
	 * recover the insertion point as {@code -returnValue - 1}.
	 * </p>
	 */
	private int find(int key) {
		int mask = keys.length - 1;
		int index = hash(key) & mask;

		while (occupied.get(index)) {
			if (keys[index] == key) {
				return index;
			}
			index = (index + 1) & mask;
		}
		// key not found; index is the first empty slot
		return -(index + 1);
	}

	/**
	 * Shift probe-chain entries backward to fill the hole at {@code hole},
	 * keeping chains intact without tombstones.
	 */
	private void shiftBack(int hole) {
		int mask = keys.length - 1;

		while (true) {
			int next = (hole + 1) & mask;
			if (!occupied.get(next)) {
				// Chain ends — nothing left to shift
				break;
			}

			int key = keys[next];
			int rehash = hash(key) & mask;

			// The entry at 'next' can be shifted back into 'hole' iff its
			// true hash position wraps through 'hole' before reaching 'next'.
			if (distance(rehash, hole, mask) < distance(rehash, next, mask)) {
				keys[hole] = key;
				values[hole] = values[next];
				occupied.set(hole);
				occupied.clear(next);
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

	// ------------------------------------------------------------------
	// Resizing
	// ------------------------------------------------------------------

	private void resize(int newCapacity) {
		if (newCapacity > MAX_CAPACITY) {
			return;
		}
		int[] oldKeys = keys;
		int[] oldValues = values;
		IntBitSet oldOccupied = occupied;

		keys = new int[newCapacity];
		values = new int[newCapacity];
		occupied = new IntBitSet(newCapacity);
		size = 0;

		int mask = newCapacity - 1;
		for (int i = 0; i < oldKeys.length; i++) {
			if (!oldOccupied.get(i)) {
				continue;
			}
			int k = oldKeys[i];
			int v = oldValues[i];
			int index = hash(k) & mask;
			while (occupied.get(index)) {
				index = (index + 1) & mask;
			}
			occupied.set(index);
			keys[index] = k;
			values[index] = v;
			size++;
		}
	}

	// ------------------------------------------------------------------
	// Capacity helpers
	// ------------------------------------------------------------------

	/**
	 * Returns the smallest power of two greater than or equal to {@code cap},
	 * matching {@link java.util.HashMap#tableSizeFor(int)}.
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
	// toString
	// ------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (int i = 0; i < keys.length; i++) {
			if (!occupied.get(i)) {
				continue;
			}
			if (!first) {
				sb.append(", ");
			}
			sb.append(keys[i]).append('=').append(values[i]);
			first = false;
		}
		sb.append('}');
		return sb.toString();
	}

	// ------------------------------------------------------------------
	// Mutable entry used by entrySet iterator
	// ------------------------------------------------------------------

	/**
	 * A mutable map entry that delegates value changes back to the backing map.
	 */
	private final class IntIntEntry implements Map.Entry<Integer, Integer> {

		private final int key;
		private int value;

		IntIntEntry(int key, int value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Integer getKey() {
			return key;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public Integer setValue(Integer value) {
			Objects.requireNonNull(value);
			int old = this.value;
			// Find and update in the backing array
			int idx = find(key);
			if (idx >= 0) {
				values[idx] = value;
			}
			this.value = value;
			return old;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Map.Entry<?, ?>)) {
				return false;
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return (
				e.getKey() != null &&
				e.getValue() != null &&
				e.getKey().equals(key) &&
				e.getValue().equals(value)
			);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(value);
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
}
