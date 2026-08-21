package ru.snake.primitive.benchmark;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import ru.snake.primitive.list.IntList;

/**
 * Benchmarks for {@link IntList}.
 * <p>
 * Java's {@code ArrayList<Integer>} baseline is provided by
 * {@link JavaIntListBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 *
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntListBenchmark
 * }</pre>
 */
public class IntListBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		public IntList intList;

		/** Random indices used for get/set operations. */
		public int[] randomIndices;

		/** Values used for set operations. */
		public int[] setValues;

		@Setup
		public void setup() {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			int count = capacity;

			intList = new IntList(count);
			for (int i = 0; i < count; i++) {
				intList.addInt(i * 7);
			}

			// Generate random indices for get/set benchmarks
			int sampleSize = Math.min(count, 1000);
			randomIndices = new int[sampleSize];
			setValues = new int[sampleSize];
			for (int i = 0; i < sampleSize; i++) {
				randomIndices[i] = rng.nextInt(count);
				setValues[i] = rng.nextInt();
			}
		}
	}

	@State(Scope.Thread)
	public static class ThreadState {

		public IntList intList;

		@Setup
		public void setup(BenchmarkState state) {
			intList = new IntList(state.capacity);
			for (int i = 0; i < state.capacity; i++) {
				intList.addInt(i * 7);
			}
		}
	}

	// ------------------------------------------------------------------
	// Get — random index read (primitive)
	// ------------------------------------------------------------------

	@Benchmark
	public long intList_get(BenchmarkState data) {
		long sum = 0;
		for (int idx : data.randomIndices) {
			sum += data.intList.getInt(idx);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Get — random index read (boxed, via List interface)
	// ------------------------------------------------------------------

	@Benchmark
	public long intList_getBoxed(BenchmarkState data) {
		long sum = 0;
		for (int idx : data.randomIndices) {
			Integer v = data.intList.get(idx);
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Set — random index write (primitive)
	// ------------------------------------------------------------------

	@Benchmark
	public int intList_setInt(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int i = 0; i < data.randomIndices.length; i++) {
			int old = ts.intList.setInt(data.randomIndices[i], data.setValues[i]);
			sum += old;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Add — append (primitive)
	// ------------------------------------------------------------------

	@Benchmark
	public int intList_addAppend(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int i = 0; i < 100; i++) {
			ts.intList.addInt(sum + i);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements (primitive getInt)
	// ------------------------------------------------------------------

	@Benchmark
	public long intList_iterate(BenchmarkState data) {
		long sum = 0;
		int size = data.intList.size();
		for (int i = 0; i < size; i++) {
			sum += data.intList.getInt(i);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements (enhanced for-each, boxed)
	// ------------------------------------------------------------------

	@Benchmark
	public long intList_iterateBoxed(BenchmarkState data) {
		long sum = 0;
		for (Integer v : data.intList) {
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Contains — linear scan
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intList_contains(BenchmarkState data) {
		return data.intList.contains(data.capacity);
	}

	// ------------------------------------------------------------------
	// Iterator with remove()
	// ------------------------------------------------------------------

	@Benchmark
	public void intList_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
		Iterator<Integer> it = ts.intList.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		Blackhole.consumeCPU(1);
	}
}
