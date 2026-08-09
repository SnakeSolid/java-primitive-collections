package ru.snake.collections.benchmark;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility helpers for benchmark setup logic shared across multiple benchmark
 * classes.
 */
final class BenchmarkDataHelper {

	private BenchmarkDataHelper() {
		// utility class
	}

	/**
	 * Fisher-Yates shuffle for {@code int[]} using the provided random source.
	 */
	static void shuffle(int[] a, ThreadLocalRandom rng) {
		for (int i = a.length - 1; i > 0; i--) {
			int j = rng.nextInt(i + 1);
			int tmp = a[i];
			a[i] = a[j];
			a[j] = tmp;
		}
	}

	/**
	 * Fisher-Yates shuffle for {@code Integer[]} using the provided random
	 * source.
	 */
	static void shuffle(Integer[] a, ThreadLocalRandom rng) {
		for (int i = a.length - 1; i > 0; i--) {
			int j = rng.nextInt(i + 1);
			Integer tmp = a[i];
			a[i] = a[j];
			a[j] = tmp;
		}
	}

	/**
	 * Fisher-Yates shuffle for {@code String[]} using the provided random
	 * source.
	 */
	static void shuffle(String[] a, ThreadLocalRandom rng) {
		for (int i = a.length - 1; i > 0; i--) {
			int j = rng.nextInt(i + 1);
			String tmp = a[i];
			a[i] = a[j];
			a[j] = tmp;
		}
	}

	/**
	 * Build a sequential index array {@code [0, 1, ..., capacity-1]} ready to
	 * be shuffled.
	 */
	static int[] sequentialIndices(int capacity) {
		int[] indices = new int[capacity];
		for (int i = 0; i < capacity; i++) {
			indices[i] = i;
		}
		return indices;
	}

	/**
	 * Build a sequential index array with spacing ({@code i * 32}) to avoid
	 * hash slot collisions in 27-bit key hashing schemes.
	 */
	static int[] spacedIndices(int capacity) {
		int[] indices = new int[capacity];
		for (int i = 0; i < capacity; i++) {
			indices[i] = i * 32;
		}
		return indices;
	}

	/**
	 * Build a {@code String[]} of keys:
	 * {@code "key_0", "key_1", ..., "key_N-1"}.
	 */
	static String[] stringKeys(int capacity) {
		String[] keys = new String[capacity];
		for (int i = 0; i < capacity; i++) {
			keys[i] = "key_" + i;
		}
		return keys;
	}
}
