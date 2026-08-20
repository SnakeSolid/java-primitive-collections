package ru.snake.primitive.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * A generic hash map from {@code K} keys to {@code V} values, backed by
 * parallel {@code Object[]} arrays. Uses Java's standard hash strategy
 * ({@code hashCode &amp; mask}) with linear probing for collision resolution.
 *
 * <p>
 * Implements {@link Map<K, V>} so it can be used wherever a standard map is
 * expected. {@code null} keys and values are not supported —
 * {@link NullPointerException} will be thrown if one is passed.
 *
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class ObjectMap<K, V> implements Map<K, V> {

	/** Initial table capacity. Always a power of two. */
	private static final int DEFAULT_CAPACITY = 16;

	/** Maximum table capacity. */
	private static final int MAX_CAPACITY = 1 << 30;

	/**
	 * Load factor threshold — resize when size exceeds capacity * LOAD_FACTOR.
	 */
	private static final float LOAD_FACTOR = 0.75f;

	/** Keys array. */
	private K[] keys;

	/** Values array — parallel to {@code keys}. */
	private V[] values;

	/** Number of live key-value mappings. */
	private int size;

	// ------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------

	/**
	 * Constructs an empty map with the default initial capacity (16) and load
	 * factor (0.75).
	 */
	public ObjectMap() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs an empty map whose initial table size is the smallest power of
	 * two not less than {@code initialCapacity}.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IllegalArgumentException if {@code initialCapacity} is negative
	 */
	@SuppressWarnings("unchecked")
	public ObjectMap(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
		}

		int cap = tableSizeFor(initialCapacity);
		keys = (K[]) new Object[cap];
		values = (V[]) new Object[cap];
	}

	// ------------------------------------------------------------------
	// Core operations
	// ------------------------------------------------------------------

	/**
	 * Associates the specified value with the specified key. If the key is
	 * already present, the old value is replaced.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the previous value associated with {@code key}, or {@code null}
	 *         if there was no mapping (use {@link #containsKey} to
	 *         disambiguate)
	 */
	@Override
	public V put(K key, V value) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(value, "value must not be null");

		int result = find(key);
		if (result >= 0) {
			int index = result;
			V old = values[index];
			values[index] = value;
			return old;
		}

		// key not found – decode the first empty slot and insert directly
		int index = -result - 1;
		keys[index] = key;
		values[index] = value;
		size++;

		if (size > (int) (keys.length * LOAD_FACTOR)) {
			resize(keys.length * 2);
		}

		return null;
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value, or {@code null} if no mapping exists (use
	 *         {@link #containsKey} to disambiguate)
	 */
	@Override
	public V get(Object key) {
		Objects.requireNonNull(key, "key must not be null");
		int index = find(key);
		return index < 0 ? null : values[index];
	}

	/**
	 * Returns the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @param key          the key whose associated value is to be returned
	 * @param defaultValue the value to return if no mapping exists
	 * @return the value, or {@code defaultValue} if no mapping exists
	 */
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		if (key == null) {
			return defaultValue;
		}

		int index = find(key);
		return index < 0 ? defaultValue : values[index];
	}

	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * key.
	 *
	 * @param key the key whose presence is tested
	 * @return {@code true} if the key is present
	 */
	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}

		return find(key) >= 0;
	}

	/**
	 * Returns {@code true} if this map maps one or more keys to the specified
	 * value.
	 *
	 * @param value the value whose presence is tested
	 * @return {@code true} if the value is present
	 */
	@Override
	public boolean containsValue(Object value) {
		if (value == null) {
			return false;
		}

		return containsValue0(value);
	}

	private boolean containsValue0(Object value) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && values[i] != null && values[i].equals(value)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes the mapping for the specified key (if present).
	 *
	 * @param key the key to remove
	 * @return the previous value, or {@code null} if no mapping existed
	 */
	@Override
	public V remove(Object key) {
		if (key == null) {
			return null;
		}

		return remove0(key);
	}

	private V remove0(Object key) {
		int index = find(key);

		if (index < 0) {
			return null;
		}

		V old = values[index];
		size--;
		keys[index] = null;
		values[index] = null;
		shiftBack(index);

		return old;
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of mappings
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns {@code true} if this map contains no key-value mappings.
	 *
	 * @return {@code true} if empty
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes all mappings from this map.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < keys.length; i++) {
			keys[i] = null;
			values[i] = null;
		}

		size = 0;
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	/**
	 * Copies all key-value pairs from the given map into this map.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		Objects.requireNonNull(m, "map must not be null");

		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	// ------------------------------------------------------------------
	// Views
	// ------------------------------------------------------------------

	@Override
	public Set<K> keySet() {
		return new AbstractSet<>() {
			@Override
			public int size() {
				return size;
			}

			@Override
			public boolean contains(Object o) {
				return o != null && find(o) >= 0;
			}

			@Override
			public boolean remove(Object o) {
				if (o == null) {
					return false;
				}

				int index = find(o);
				if (index < 0) {
					return false;
				}

				size--;
				keys[index] = null;
				values[index] = null;
				shiftBack(index);
				return true;
			}

			@Override
			public void clear() {
				ObjectMap.this.clear();
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;

				for (int i = keys.length - 1; i >= 0; i--) {
					if (keys[i] != null) {
						if (!c.contains(keys[i])) {
							keys[i] = null;
							values[i] = null;
							size--;
							shiftBack(i);
							changed = true;
						}
					}
				}

				return changed;
			}

			@Override
			public Iterator<K> iterator() {
				return new KeyIterator();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new java.util.AbstractCollection<V>() {
			@Override
			public int size() {
				return size;
			}

			@Override
			public Iterator<V> iterator() {
				return new ValueIterator();
			}
		};
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
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
				if (e.getKey() == null) {
					return false;
				}

				int index = find(e.getKey());
				return (index >= 0
						&& (values[index] == null ? e.getValue() == null : values[index].equals(e.getValue())));
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry<?, ?>)) {
					return false;
				}

				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				if (e.getKey() == null) {
					return false;
				}

				int index = find(e.getKey());
				if (index < 0) {
					return false;
				}

				// Only remove if the value also matches
				if (values[index] == null ? e.getValue() != null : !values[index].equals(e.getValue())) {
					return false;
				}

				size--;
				keys[index] = null;
				values[index] = null;
				shiftBack(index);
				return true;
			}

			@Override
			public void clear() {
				ObjectMap.this.clear();
			}

			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}
		};
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		Objects.requireNonNull(action, "action must not be null");

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null) {
				K k = keys[i];
				V v = values[i];
				action.accept(k, v);
			}
		}
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		Objects.requireNonNull(function, "function must not be null");

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == null) {
				continue;
			}

			K k = keys[i];
			V v = values[i];
			V result = function.apply(k, v);
			if (result == null) {
				throw new NullPointerException("function must not return null");
			}

			values[i] = result;
		}
	}

	@Override
	public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(mappingFunction, "mappingFunction must not be null");
		int index = find(key);
		if (index >= 0) {
			return values[index];
		}

		V newValue = mappingFunction.apply(key);
		if (newValue == null) {
			throw new NullPointerException("mappingFunction must not return null");
		}

		put(key, newValue);
		return newValue;
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(remappingFunction, "remappingFunction must not be null");
		int index = find(key);

		if (index < 0) {
			return null;
		}

		V current = values[index];
		V newValue = remappingFunction.apply(key, current);

		if (newValue == null) {
			size--;
			keys[index] = null;
			values[index] = null;
			shiftBack(index);
			return null;
		}

		values[index] = newValue;

		return newValue;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(remappingFunction, "remappingFunction must not be null");
		int index = find(key);
		V current = index >= 0 ? values[index] : null;
		V newValue = remappingFunction.apply(key, current);

		if (newValue == null) {
			if (index >= 0) {
				size--;
				keys[index] = null;
				values[index] = null;
				shiftBack(index);
			}

			return null;
		}

		put(key, newValue);

		return newValue;
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(value, "value must not be null");
		Objects.requireNonNull(remappingFunction, "remappingFunction must not be null");
		int index = find(key);

		if (index < 0) {
			put(key, value);
			return value;
		}

		V newValue = remappingFunction.apply(values[index], value);

		if (newValue == null) {
			size--;
			keys[index] = null;
			values[index] = null;
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
	 * Compute the hash for a key object.
	 */
	private static int hash(Object key) {
		int h = key.hashCode();
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
	private int find(Object key) {
		int mask = keys.length - 1;
		int index = hash(key) & mask;

		while (keys[index] != null) {
			if (keys[index].equals(key)) {
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
			if (keys[next] == null) {
				// Chain ends — nothing left to shift
				break;
			}

			K key = keys[next];
			int rehash = hash(key) & mask;

			// The entry at 'next' can be shifted back into 'hole' iff its
			// true hash position wraps through 'hole' before reaching 'next'.
			if (distance(rehash, hole, mask) < distance(rehash, next, mask)) {
				keys[hole] = key;
				values[hole] = values[next];
				keys[next] = null;
				values[next] = null;
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

	@SuppressWarnings("unchecked")
	private void resize(int newCapacity) {
		if (newCapacity > MAX_CAPACITY) {
			return;
		}
		K[] oldKeys = keys;
		V[] oldValues = values;

		keys = (K[]) new Object[newCapacity];
		values = (V[]) new Object[newCapacity];
		size = 0;

		int mask = newCapacity - 1;
		for (int i = 0; i < oldKeys.length; i++) {
			if (oldKeys[i] == null) {
				continue;
			}

			K k = oldKeys[i];
			V v = oldValues[i];
			int index = hash(k) & mask;

			while (keys[index] != null) {
				index = (index + 1) & mask;
			}

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
			if (keys[i] == null) {
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
	// Iterators
	// ------------------------------------------------------------------

	/**
	 * Iterator over the keys of this map. Walks the internal arrays directly,
	 * skipping empty slots.
	 */
	private final class KeyIterator implements Iterator<K> {

		private int index = 0;

		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (keys[i] != null) {
					return true;
				}
			}

			return false;
		}

		@Override
		public K next() {
			while (index < keys.length && keys[index] == null) {
				index++;
			}

			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}

			lastIndex = index;
			return (K) keys[index++];
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}

			Object key = keys[lastIndex];
			ObjectMap.this.remove0(key);
			index = lastIndex;
			lastIndex = -1;
		}
	}

	/**
	 * Iterator over the values of this map. Walks the internal arrays directly.
	 */
	private final class ValueIterator implements Iterator<V> {

		private int index = 0;

		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (keys[i] != null) {
					return true;
				}
			}

			return false;
		}

		@Override
		public V next() {
			while (index < keys.length && keys[index] == null) {
				index++;
			}

			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}

			lastIndex = index;
			return (V) values[index++];
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}

			Object key = keys[lastIndex];
			ObjectMap.this.remove0(key);
			index = lastIndex;
			lastIndex = -1;
		}
	}

	/**
	 * Iterator over the entries of this map.
	 */
	private final class EntryIterator implements Iterator<Map.Entry<K, V>> {

		private int index = 0;

		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			for (int i = index; i < keys.length; i++) {
				if (keys[i] != null) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Map.Entry<K, V> next() {
			while (index < keys.length && keys[index] == null) {
				index++;
			}

			if (index >= keys.length) {
				throw new java.util.NoSuchElementException();
			}

			lastIndex = index;
			return new KeyValueEntry((K) keys[index], (V) values[index++]);
		}

		@Override
		public void remove() {
			if (lastIndex < 0) {
				throw new IllegalStateException();
			}

			Object key = keys[lastIndex];
			ObjectMap.this.remove0(key);
			index = lastIndex;
			lastIndex = -1;
		}
	}

	// ------------------------------------------------------------------
	// Mutable entry used by entrySet iterator
	// ------------------------------------------------------------------

	/**
	 * A mutable map entry that delegates value changes back to the backing map.
	 */
	private final class KeyValueEntry implements Map.Entry<K, V> {

		private final K key;

		private V value;

		KeyValueEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			Objects.requireNonNull(value, "value must not be null");
			V old = this.value;

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
			} else if (!(o instanceof Map.Entry<?, ?>)) {
				return false;
			}

			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return ((key == null ? e.getKey() == null : key.equals(e.getKey()))
					&& (value == null ? e.getValue() == null : value.equals(e.getValue())));
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
