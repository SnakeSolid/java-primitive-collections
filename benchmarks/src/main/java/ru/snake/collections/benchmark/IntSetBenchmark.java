package ru.snake.collections.benchmark;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import ru.snake.collections.set.IntSet;

/**
 * Benchmarks for {@link IntSet}.
 * <p>
 * Java's {@code Set<Integer>} baseline is provided by
 * {@link JavaIntSetBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntSetBenchmark
 * }</pre>
 */
public class IntSetBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public IntSet intSet;

		/** Random keys that are present in both sets. */
		public int[] presentKeys;
		/** Random keys that are absent from both sets. */
		public int[] absentKeys;
		/** Random keys used for insertion benchmarks. */
		public int[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			intSet = new IntSet(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.spacedIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				intSet.add(key);
				presentKeys[i] = key;
			}

			absentKeys = new int[absentCount];
			for (int i = 0; i < absentCount; i++) {
				absentKeys[i] = allIndices[count + i];
			}

			insertKeys = new int[insertCount];
			for (int i = 0; i < insertCount; i++) {
				insertKeys[i] = allIndices[count + absentCount + i];
			}
		}
	}

	@State(Scope.Thread)
	public static class ThreadState {

		public IntSet intSet;
		public Set<Integer> hashSet;

		@Setup
		public void setup(BenchmarkState state) {
			intSet = new IntSet(state.capacity);
			for (int k : state.presentKeys) {
				intSet.add(k);
			}
		}
	}

	// ------------------------------------------------------------------
	// Contains — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long intSet_containsPresent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.presentKeys) {
			if (data.intSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Contains — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long intSet_containsAbsent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.absentKeys) {
			if (data.intSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Add — unique elements
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intSet_add(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.insertKeys) {
			if (ts.intSet.add(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Remove — elements that are present
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intSet_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.presentKeys) {
			if (ts.intSet.remove(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements
	// ------------------------------------------------------------------

	@Benchmark
	public long intSet_iterate(BenchmarkState data) {
		long sum = 0;
		for (Integer v : data.intSet) {
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Bulk containsAll
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intSet_containsAll(BenchmarkState data) {
		return data.intSet.containsAll(data.intSet);
	}
}
