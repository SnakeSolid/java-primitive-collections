package ru.snake.primitive.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for {@link ArrayList}&lt;{@link Integer}&gt;.
 * <p>
 * Extracted from {@link IntListBenchmark} so the Java-side of the comparison is
 * not duplicated. Use this class as a standalone baseline for
 * {@code List<Integer>} performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 *
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaIntListBenchmark
 * }</pre>
 */
public class JavaIntListBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		public List<Integer> arrayList;

		/** Random indices used for get/set operations. */
		public int[] randomIndices;

		/** Values used for set operations. */
		public int[] setValues;

		@Setup
		public void setup() {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			int count = capacity;

			arrayList = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				arrayList.add(i * 7);
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

		public List<Integer> arrayList;

		@Setup
		public void setup(BenchmarkState state) {
			arrayList = new ArrayList<>(state.arrayList);
		}
	}

	// ------------------------------------------------------------------
	// Get — random index read
	// ------------------------------------------------------------------

	@Benchmark
	public long arrayList_get(BenchmarkState data) {
		long sum = 0;
		for (int idx : data.randomIndices) {
			sum += data.arrayList.get(idx);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Set — random index write
	// ------------------------------------------------------------------

	@Benchmark
	public int arrayList_set(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int i = 0; i < data.randomIndices.length; i++) {
			Integer old = ts.arrayList.set(data.randomIndices[i], data.setValues[i]);
			sum += old;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Add — append
	// ------------------------------------------------------------------

	@Benchmark
	public int arrayList_addAppend(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int i = 0; i < 100; i++) {
			ts.arrayList.add(sum + i);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements
	// ------------------------------------------------------------------

	@Benchmark
	public long arrayList_iterate(BenchmarkState data) {
		long sum = 0;
		for (Integer v : data.arrayList) {
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Contains — linear scan
	// ------------------------------------------------------------------

	@Benchmark
	public boolean arrayList_contains(BenchmarkState data) {
		return data.arrayList.contains(data.capacity);
	}

	// ------------------------------------------------------------------
	// Iterator with remove()
	// ------------------------------------------------------------------

	@Benchmark
	public void arrayList_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
		Iterator<Integer> it = ts.arrayList.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		Blackhole.consumeCPU(1);
	}
}
