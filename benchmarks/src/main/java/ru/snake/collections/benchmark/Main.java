package ru.snake.collections.benchmark;

import java.util.concurrent.ThreadLocalRandom;

import ru.snake.collections.set.IntSet;

public class Main {

	public int capacity = 1000000;
	public int fillPercent = 25;

	public IntSet intSet;

	public int[] presentKeys;
	public int[] absentKeys;
	public int[] insertKeys;

	public void setup() {
		int count = (int) ((capacity * fillPercent) / 100.0);
		int absentCount = Math.max(count / 2, 100);
		int insertCount = Math.min(count, 1000);

		intSet = new IntSet(capacity);

		ThreadLocalRandom rng = ThreadLocalRandom.current();

		// Build a shuffled index array — values are spread across the
		// full positive int range to exercise the 27-bit key hashing.
		int[] allIndices = new int[capacity];
		for (int i = 0; i < capacity; i++) {
			allIndices[i] = i * 32; // spacing avoids slot collisions
		}
		shuffle(allIndices, rng);

		// Populate present keys
		presentKeys = new int[count];
		for (int i = 0; i < count; i++) {
			int key = allIndices[i];
			intSet.add(key);
			presentKeys[i] = key;
		}

		// Absent keys
		absentKeys = new int[absentCount];
		for (int i = 0; i < absentCount; i++) {
			absentKeys[i] = allIndices[count + i];
		}

		// Insert keys
		insertKeys = new int[insertCount];
		for (int i = 0; i < insertCount; i++) {
			insertKeys[i] = allIndices[count + absentCount + i];
		}
	}

	private void shuffle(int[] a, ThreadLocalRandom rng) {
		for (int i = a.length - 1; i > 0; i--) {
			int j = rng.nextInt(i + 1);
			int tmp = a[i];
			a[i] = a[j];
			a[j] = tmp;
		}
	}

	public boolean intSet_remove() {
		boolean changed = false;
		for (int key : presentKeys) {
			if (intSet.remove(key)) {
				changed = true;
			}
		}
		return changed;
	}

	public static void main(String[] args) {
		Main main = new Main();
		main.setup();
		System.out.println(main.intSet_remove());
	}

}
