package ru.snake.primitive.list;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A resizable list of {@code int} values backed by a plain {@code int[]}.
 *
 * <p>
 * Implements {@link java.util.List<Integer>} so it can be used wherever a
 * standard list is expected. {@code null} elements are not supported --
 * {@link NullPointerException} will be thrown if one is passed to a primitive
 * method.
 * </p>
 *
 * <p>
 * This class is not thread-safe.
 * </p>
 */
public final class IntList extends AbstractList<Integer> implements RandomAccess {

	/** Default initial capacity. */
	private static final int DEFAULT_CAPACITY = 10;

	/** Backing array. */
	private int[] data;

	/** Number of live elements. */
	private int size;

	// ------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------

	/**
	 * Constructs an empty list with the default initial capacity (10).
	 */
	public IntList() {
		data = new int[DEFAULT_CAPACITY];
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IllegalArgumentException if {@code initialCapacity} is negative
	 */
	public IntList(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
		}

		data = new int[initialCapacity];
	}

	/**
	 * Constructs a list containing the elements of the given collection, in the
	 * order returned by its iterator.
	 *
	 * @param c the collection whose elements are to be placed into this list
	 * @throws NullPointerException if the collection is null
	 */
	public IntList(Collection<? extends Integer> c) {
		data = new int[c.size()];
		int i = 0;

		for (Integer e : c) {
			Objects.requireNonNull(e, "element must not be null");
			data[i++] = e;
		}

		size = i;
	}

	// ------------------------------------------------------------------
	// Primitive convenience methods
	// ------------------------------------------------------------------

	/**
	 * Returns the element at the specified position without boxing.
	 *
	 * @param index the index
	 * @return the element at that position
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public int getInt(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		return data[index];
	}

	/**
	 * Replaces the element at the specified position without boxing.
	 *
	 * @param index index of the element to replace
	 * @param value new element value
	 * @return the element previously at the specified position
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public int setInt(int index, int value) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		int oldValue = data[index];
		data[index] = value;
		return oldValue;
	}

	/**
	 * Appends the given value to the end of the list.
	 *
	 * @param value the value to add
	 * @return {@code true} (always, for consistency with
	 *         {@link Collection#add})
	 */
	public boolean addInt(int value) {
		add(size, value);
		return true;
	}

	/**
	 * Inserts the given value at the specified position.
	 *
	 * @param index index at which to insert
	 * @param value the value to insert
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public void insertInt(int index, int value) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		add(index, value);
	}

	// ------------------------------------------------------------------
	// AbstractList / List implementation
	// ------------------------------------------------------------------

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer)) {
			return false;
		}

		int value = (Integer) o;

		for (int i = 0; i < size; i++) {
			if (data[i] == value) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Integer get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		return data[index];
	}

	@Override
	public Integer set(int index, Integer element) {
		Objects.requireNonNull(element, "element must not be null");

		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		int oldValue = data[index];
		data[index] = element;
		return oldValue;
	}

	@Override
	public void add(int index, Integer element) {
		Objects.requireNonNull(element, "element must not be null");

		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		if (size == data.length) {
			grow(size + 1);
		}

		if (index < size) {
			System.arraycopy(data, index, data, index + 1, size - index);
		}

		data[index] = element;
		size++;
	}

	private void add(int index, int value) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		if (size == data.length) {
			grow(size + 1);
		}

		if (index < size) {
			System.arraycopy(data, index, data, index + 1, size - index);
		}

		data[index] = value;
		size++;
	}

	@Override
	public Integer remove(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		int oldValue = data[index];

		int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(data, index + 1, data, index, numMoved);
		}

		size--;
		return oldValue;
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Integer)) {
			return false;
		}

		int value = (Integer) o;

		for (int i = 0; i < size; i++) {
			if (data[i] == value) {
				int numMoved = size - i - 1;
				if (numMoved > 0) {
					System.arraycopy(data, i + 1, data, i, numMoved);
				}

				data[--size] = 0;
				return true;
			}
		}

		return false;
	}

	@Override
	public void clear() {
		// No need to zero out primitive array, but reset size.
		size = 0;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Integer> c) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		int numNew = c.size();
		if (numNew == 0) {
			return false;
		}

		if (size + numNew > data.length) {
			grow(size + numNew);
		}

		// Shift existing elements to the right
		if (index < size) {
			System.arraycopy(data, index, data, index + numNew, size - index);
		}

		int i = 0;
		for (Integer e : c) {
			Objects.requireNonNull(e, "element must not be null");
			data[index + i++] = e;
		}

		size += numNew;
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c);

		boolean changed = false;
		int write = 0;

		for (int i = 0; i < size; i++) {
			if (!c.contains(data[i])) {
				data[write++] = data[i];
			} else {
				changed = true;
			}
		}

		for (int i = write; i < size; i++) {
			data[i] = 0;
		}

		if (changed) {
			size = write;
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c);

		boolean changed = false;
		int write = 0;

		for (int i = 0; i < size; i++) {
			if (c.contains(data[i])) {
				data[write++] = data[i];
			} else {
				changed = true;
			}
		}

		for (int i = write; i < size; i++) {
			data[i] = 0;
		}

		if (changed) {
			size = write;
		}

		return changed;
	}

	@Override
	public void replaceAll(java.util.function.UnaryOperator<Integer> operator) {
		Objects.requireNonNull(operator);

		for (int i = 0; i < size; i++) {
			Integer newValue = operator.apply(data[i]);
			Objects.requireNonNull(newValue, "operator returned null");
			data[i] = newValue;
		}
	}

	@Override
	public void forEach(java.util.function.Consumer<? super Integer> action) {
		Objects.requireNonNull(action);

		for (int i = 0; i < size; i++) {
			action.accept(data[i]);
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[size];
		for (int i = 0; i < size; i++) {
			result[i] = data[i];
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object[] collected = new Object[size];
		for (int i = 0; i < size; i++) {
			collected[i] = Integer.valueOf(data[i]);
		}

		if (a.length >= size) {
			System.arraycopy(collected, 0, a, 0, size);

			if (a.length > size) {
				a[size] = null;
			}

			return a;
		}

		T[] result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		System.arraycopy(collected, 0, result, 0, size);
		return result;
	}

	@Override
	public int hashCode() {
		int h = 1;

		for (int i = 0; i < size; i++) {
			h = 31 * h + data[i];
		}

		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof List<?>)) {
			return false;
		}

		List<?> that = (List<?>) o;

		if (that.size() != size) {
			return false;
		}

		if (that instanceof IntList) {
			IntList other = (IntList) that;

			for (int i = 0; i < size; i++) {
				if (data[i] != other.data[i]) {
					return false;
				}
			}

			return true;
		}

		ListIterator<?> it = that.listIterator();
		for (int i = 0; i < size; i++) {
			if (!Objects.equals(data[i], it.next())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		if (size == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(data[0]);

		for (int i = 1; i < size; i++) {
			sb.append(", ");
			sb.append(data[i]);
		}

		sb.append(']');
		return sb.toString();
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
		return addAll(size, c);
	}

	@Override
	public boolean add(Integer element) {
		return addInt(element);
	}

	// ------------------------------------------------------------------
	// Iterators
	// ------------------------------------------------------------------

	@Override
	public Iterator<Integer> iterator() {
		return new IntListIterator(0);
	}

	@Override
	public ListIterator<Integer> listIterator() {
		return new IntListIterator(0);
	}

	@Override
	public ListIterator<Integer> listIterator(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}

		return new IntListIterator(index);
	}

	// ------------------------------------------------------------------
	// Capacity management
	// ------------------------------------------------------------------

	private void grow(int minCapacity) {
		int oldCapacity = data.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5x growth

		if (newCapacity < minCapacity) {
			newCapacity = minCapacity;
		}

		// Handle overflow
		if (newCapacity < 0) {
			newCapacity = minCapacity == Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
		}

		data = java.util.Arrays.copyOf(data, newCapacity);
	}

	// ------------------------------------------------------------------
	// Iterator implementation
	// ------------------------------------------------------------------

	private final class IntListIterator implements ListIterator<Integer> {

		/**
		 * Index of the element that would be returned by the next call to
		 * next().
		 */
		private int cursor;

		/**
		 * Index of the element that was last returned by next() or previous().
		 */
		private int lastRet = -1;

		IntListIterator(int index) {
			cursor = index;
		}

		@Override
		public boolean hasNext() {
			return cursor != size;
		}

		@Override
		public Integer next() {
			if (cursor == size) {
				throw new java.util.NoSuchElementException();
			}

			lastRet = cursor++;
			return data[lastRet];
		}

		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		@Override
		public Integer previous() {
			if (cursor == 0) {
				throw new java.util.NoSuchElementException();
			}

			lastRet = --cursor;
			return data[lastRet];
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void set(Integer element) {
			Objects.requireNonNull(element, "element must not be null");

			if (lastRet < 0) {
				throw new java.lang.IllegalStateException();
			}

			data[lastRet] = element;
			IntList.this.modCount++;
		}

		@Override
		public void add(Integer element) {
			Objects.requireNonNull(element, "element must not be null");

			IntList.this.add(cursor++, element);
			lastRet = -1;
		}

		@Override
		public void remove() {
			if (lastRet < 0) {
				throw new java.lang.IllegalStateException();
			}

			IntList.this.remove(lastRet);
			cursor = lastRet;
			lastRet = -1;
		}

	}
}
