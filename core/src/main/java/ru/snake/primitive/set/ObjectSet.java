package ru.snake.primitive.set;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import ru.snake.primitive.map.IntToIntMap;

/**
 * A simple hash set backed by parallel primitive arrays and linear probing,
 * similar to {@link IntToIntMap} but storing only elements (no associated
 * values).
 *
 * <p>
 * Implements {@link java.util.Set} so it can be used wherever a standard set is
 * expected. {@code null} elements are not supported —
 * {@link NullPointerException} will be thrown if one is passed.
 * </p>
 *
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @param <E> the element type
 */
public final class ObjectSet<E> extends AbstractSet<E> {

	/** Initial table capacity. Always a power of two. */
	private static final int DEFAULT_CAPACITY = 16;

	/** Maximum table capacity. */
	private static final int MAX_CAPACITY = 1 << 30;

	/**
	 * Load factor threshold — resize when size exceeds capacity * LOAD_FACTOR.
	 */
	private static final float LOAD_FACTOR = 0.75f;

	/** Keys array. */
	private E[] keys;

	/** Number of live elements. */
	private int size;

	// ------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------

	/**
	 * Constructs an empty set with the default initial capacity (16) and load
	 * factor (0.75).
	 */
	public ObjectSet() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs an empty set whose initial table size is the smallest power of
	 * two not less than {@code initialCapacity}.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IllegalArgumentException if {@code initialCapacity} is negative
	 */
	@SuppressWarnings("unchecked")
	public ObjectSet(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
		}

		int cap = tableSizeFor(initialCapacity);
		keys = (E[]) new Object[cap];
	}

	// ------------------------------------------------------------------
	// Core primitive operations
	// ------------------------------------------------------------------

	/**
	 * Adds the specified element to this set.
	 *
	 * @param element the element to add
	 * @return {@code true} if the set did not already contain the element
	 */
	public boolean add0(E element) {
		Objects.requireNonNull(element, "element must not be null");

		int result = find(element);
		if (result >= 0) {
			return false;
		}

		// element not found – decode the first empty slot and insert directly
		int index = -result - 1;
		keys[index] = element;
		size++;

		if (size > (int) (keys.length * LOAD_FACTOR)) {
			resize(keys.length * 2);
		}

		return true;
	}

	/**
	 * Removes the specified element from this set.
	 *
	 * @param element the element to remove
	 * @return {@code true} if the element was present
	 */
	public boolean remove0(Object element) {
		int index = find(element);

		if (index < 0) {
			return false;
		}

		size--;
		keys[index] = null;
		shiftBack(index);
		return true;
	}

	/**
	 * Returns {@code true} if this set contains the specified element.
	 *
	 * @param element whose presence is tested
	 * @return {@code true} if present
	 */
	public boolean contains0(Object element) {
		return find(element) >= 0;
	}

	/**
	 * Returns the number of elements in this set.
	 */
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes all elements from this set.
	 */
	public void clear() {
		for (int i = 0; i < keys.length; i++) {
			keys[i] = null;
		}

		size = 0;
	}

	// ------------------------------------------------------------------
	// Bulk operations
	// ------------------------------------------------------------------

	/**
	 * Copies all elements from the given set into this set.
	 */
	public void putAll(ObjectSet<? extends E> other) {
		for (int i = 0; i < other.keys.length; i++) {
			if (other.keys[i] != null) {
				E k = other.keys[i];
				add0(k);
			}
		}
	}

	// ------------------------------------------------------------------
	// Set<E> implementation
	// ------------------------------------------------------------------

	@Override
	public boolean add(E element) {
		return add0(element);
	}

	@Override
	public boolean remove(Object element) {
		if (element == null) {
			return false;
		}

		return remove0(element);
	}

	@Override
	public boolean contains(Object element) {
		if (element == null) {
			return false;
		}

		return contains0(element);
	}

	@Override
	public Iterator<E> iterator() {
		return new ObjectSetIterator<>();
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[size];
		int idx = 0;

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null) {
				result[idx++] = keys[i];
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object[] collected = new Object[size];
		int idx = 0;

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null) {
				collected[idx++] = keys[i];
			}
		}

		if (a.length >= size) {
			System.arraycopy(collected, 0, a, 0, size);

			if (a.length > size) {
				a[size] = null;
			}

			return a;
		}

		return (T[]) Arrays.copyOf(collected, size, a.getClass());
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
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (E e : c) {
			if (add0(e)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (Object e : c) {
			if (remove0(e)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// Remove non-retained elements using backward-shift deletion.
		// Iterate backward so that shifting earlier slots does not affect
		// the scan of later slots.
		boolean changed = false;

		for (int i = keys.length - 1; i >= 0; i--) {
			if (keys[i] != null) {
				if (!c.contains(keys[i])) {
					keys[i] = null;
					size--;
					shiftBack(i);
					changed = true;
				}
			}
		}

		return changed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof java.util.Set<?>)) {
			return false;
		}

		java.util.Set<?> that = (java.util.Set<?>) o;

		if (that.size() != this.size()) {
			return false;
		}

		return containsAll(that);
	}

	@Override
	public int hashCode() {
		int h = 0;

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null) {
				h += keys[i].hashCode();
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
			if (keys[i] == null) {
				continue;
			}

			if (!first) {
				sb.append(", ");
			}

			sb.append(keys[i]);
			first = false;
		}

		sb.append(']');
		return sb.toString();
	}

	// ------------------------------------------------------------------
	// Hashing & lookup — Java HashMap style
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
	 * Find the table index for the given element, or encode the first empty
	 * slot.
	 *
	 * <p>
	 * Returns a non-negative index if the element is found. If not found,
	 * returns {@code -(firstEmpty + 1)}, where {@code firstEmpty} is the index
	 * of the first empty slot encountered during the linear probe. The caller
	 * can recover the insertion point as {@code -returnValue - 1}.
	 * </p>
	 */
	private int find(Object element) {
		int mask = keys.length - 1;
		int index = hash(element) & mask;

		while (keys[index] != null) {
			if (keys[index].equals(element)) {
				return index;
			}

			index = (index + 1) & mask;
		}

		// element not found; index is the first empty slot
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

			E key = keys[next];
			int rehash = hash(key) & mask;

			// The entry at 'next' can be shifted back into 'hole' iff its
			// true hash position wraps through 'hole' before reaching 'next'.
			if (distance(rehash, hole, mask) < distance(rehash, next, mask)) {
				keys[hole] = key;
				keys[next] = null;
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
	 * Resize the table to a new capacity.
	 */
	@SuppressWarnings("unchecked")
	private void resize(int newCapacity) {
		if (newCapacity > MAX_CAPACITY) {
			return;
		}

		E[] oldKeys = keys;
		keys = (E[]) new Object[newCapacity];
		size = 0;

		int mask = newCapacity - 1;
		for (int i = 0; i < oldKeys.length; i++) {
			if (oldKeys[i] == null) {
				continue;
			}

			E k = oldKeys[i];
			int index = hash(k) & mask;

			while (keys[index] != null) {
				index = (index + 1) & mask;
			}

			keys[index] = k;
			size++;
		}
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
	// Iterator
	// ------------------------------------------------------------------

	private final class ObjectSetIterator<T> implements Iterator<T> {

		/** Current table index being scanned. */
		private int idx = 0;

		/** Element returned by the last call to {@link #next()}. */
		private E lastElement = null;

		/**
		 * {@code true} if {@link #next()} has been called and {@link #remove()}
		 * is permitted.
		 */
		private boolean hasLast = false;

		ObjectSetIterator() {
			// advance to first occupied slot
			while (idx < keys.length && keys[idx] == null) {
				idx++;
			}
		}

		@Override
		public boolean hasNext() {
			return idx < keys.length;
		}

		@Override
		@SuppressWarnings("unchecked")
		public T next() {
			if (idx >= keys.length) {
				throw new java.util.NoSuchElementException();
			}

			lastElement = (E) keys[idx];
			hasLast = true;
			idx++;

			while (idx < keys.length && keys[idx] == null) {
				idx++;
			}

			return (T) lastElement;
		}

		@Override
		public void remove() {
			if (!hasLast) {
				throw new java.lang.IllegalStateException();
			}

			ObjectSet.this.remove0(lastElement);

			while (idx < keys.length && keys[idx] == null) {
				idx++;
			}

			lastElement = null;
			hasLast = false;
		}
	}
}
