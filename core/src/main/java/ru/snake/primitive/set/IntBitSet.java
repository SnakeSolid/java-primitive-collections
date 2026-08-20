package ru.snake.primitive.set;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A set backed by an {@code int[]} where each array element stores 32 boolean
 * values as individual bits. A bit set to {@code 1} means the corresponding
 * index is present in the set.
 *
 * <p>
 * Implements {@link Set<Integer>} so it can be used wherever a standard set is
 * expected. Elements are non-negative integers.
 * </p>
 */
public final class IntBitSet implements Set<Integer> {

	/** Number of bits in one array element. */
	private static final int WORD_BITS = 32;

	/** Bit array. */
	private int[] words;

	/** Total number of set bits. */
	private int size;

	/** Total number of addressable bits. */
	private int capacity;

	/**
	 * Constructs a set that can hold {@code capacity} boolean values (indexed
	 * from 0 to {@code capacity - 1}).
	 *
	 * @param capacity the number of values
	 * @throws IllegalArgumentException if negative
	 */
	public IntBitSet(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity: " + capacity);
		}

		this.capacity = capacity;
		this.words = new int[wordCount(capacity)];
	}

	// ------------------------------------------------------------------
	// Core bit operations
	// ------------------------------------------------------------------

	/** Word index for bit position {@code i}. */
	private static int wordIndex(int i) {
		return i >>> 5; // i / 32
	}

	/** Bit mask for bit position {@code i}. */
	private static int bitMask(int i) {
		return 1 << (i & 0x1F); // i % 32
	}

	/** Returns true if bit {@code i} is set. */
	public boolean get(int i) {
		return (words[wordIndex(i)] & bitMask(i)) != 0;
	}

	/** Sets bit {@code i} to {@code true}. */
	public void set(int i) {
		int mask = bitMask(i);

		if ((words[wordIndex(i)] & mask) == 0) {
			words[wordIndex(i)] |= mask;
			size++;
		}
	}

	/** Sets bit {@code i} to {@code false}. */
	public void clear(int i) {
		int mask = bitMask(i);

		if ((words[wordIndex(i)] & mask) != 0) {
			words[wordIndex(i)] &= ~mask;
			size--;
		}
	}

	/** Returns the number of set bits. */
	public int size() {
		return size;
	}

	/** Returns true if no bits are set. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Clears all bits. */
	public void clearAll() {
		java.util.Arrays.fill(words, 0);
		size = 0;
	}

	// ------------------------------------------------------------------
	// Set<Integer> implementation
	// ------------------------------------------------------------------

	@Override
	public boolean add(Integer element) {
		if (element == null) {
			throw new NullPointerException();
		}

		if (element < 0 || element >= capacity) {
			throw new IllegalArgumentException("element: " + element);
		}

		int mask = bitMask(element);

		if ((words[wordIndex(element)] & mask) == 0) {
			words[wordIndex(element)] |= mask;
			size++;

			return true;
		}

		return false;
	}

	@Override
	public boolean remove(Object element) {
		if (!(element instanceof Integer)) {
			return false;
		}

		int i = (Integer) element;
		if (i < 0 || i >= capacity) {
			return false;
		}

		int mask = bitMask(i);
		if ((words[wordIndex(i)] & mask) != 0) {
			words[wordIndex(i)] &= ~mask;
			size--;

			return true;
		}

		return false;
	}

	@Override
	public boolean contains(Object element) {
		if (!(element instanceof Integer)) {
			return false;
		}

		int i = (Integer) element;
		if (i < 0 || i >= capacity) {
			return false;
		}

		return (words[wordIndex(i)] & bitMask(i)) != 0;
	}

	@Override
	public java.util.Iterator<Integer> iterator() {
		return new IntBitSetIterator();
	}

	// ------------------------------------------------------------------
	// Iterator
	// ------------------------------------------------------------------

	private final class IntBitSetIterator implements java.util.Iterator<Integer> {

		/** Current word index being scanned. */
		private int wordIdx = 0;
		/**
		 * Copy of the current word, progressively cleared as bits are visited.
		 */
		private int word = wordIdx < words.length ? words[wordIdx] : 0;

		/** The next element to return, or -1 if exhausted. */
		private int next = findNext();

		/** The element last returned by {@code next()}, or -1. */
		private int last = -1;

		private int findNext() {
			while (wordIdx < words.length) {
				while (word != 0) {
					int bit = Integer.numberOfTrailingZeros(word);
					word &= ~(1 << bit);
					int idx = (wordIdx << 5) + bit;

					if (idx < capacity) {
						return idx;
					}
				}

				wordIdx++;

				if (wordIdx < words.length) {
					word = words[wordIdx];
				}
			}

			return -1;
		}

		@Override
		public boolean hasNext() {
			return next != -1;
		}

		@Override
		public Integer next() {
			if (next == -1) {
				throw new NoSuchElementException();
			}

			last = next;
			next = findNext();
			return last;
		}

		@Override
		public void remove() {
			if (last == -1) {
				throw new IllegalStateException();
			}

			// Clear the bit. Because the iterator already advanced past
			// this bit in 'word', we must also clear it from the live
			// copy so that a duplicate next() wouldn't re-emit it.
			IntBitSet.this.clear(last);
			last = -1;
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[size];
		int idx = 0;

		for (int w = 0; w < words.length; w++) {
			int word = words[w];

			while (word != 0) {
				int bit = Integer.numberOfTrailingZeros(word);
				result[idx++] = (w << 5) + bit;
				word &= ~(1 << bit);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object[] collected = new Object[size];
		int idx = 0;

		for (int w = 0; w < words.length; w++) {
			int word = words[w];

			while (word != 0) {
				int bit = Integer.numberOfTrailingZeros(word);
				collected[idx++] = (w << 5) + bit;
				word &= ~(1 << bit);
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
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;

		for (int i = this.capacity - 1; i >= 0; i--) {
			if (get(i) && !c.contains(i)) {
				clear(i);
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (int i = this.capacity - 1; i >= 0; i--) {
			if (get(i) && c.contains(i)) {
				clear(i);
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public void clear() {
		clearAll();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof Set<?>)) {
			return false;
		}

		Set<?> that = (Set<?>) o;

		if (that.size() != this.size()) {
			return false;
		}

		return containsAll(that);
	}

	@Override
	public int hashCode() {
		int h = 0;

		for (int w = 0; w < words.length; w++) {
			int word = words[w];

			while (word != 0) {
				int bit = Integer.numberOfTrailingZeros(word);
				h += Integer.hashCode((w << 5) + bit);
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

		for (int w = 0; w < words.length; w++) {
			int word = words[w];

			while (word != 0) {
				int bit = Integer.numberOfTrailingZeros(word);

				if (!first) {
					sb.append(", ");
				}

				sb.append((w << 5) + bit);
				first = false;
				word &= ~(1 << bit);
			}
		}

		sb.append(']');
		return sb.toString();
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	private static int wordCount(int capacity) {
		return (capacity + WORD_BITS - 1) >>> 5;
	}
}
