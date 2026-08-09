package ru.snake.primitive.benchmark;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for {@link HashSet}&lt;{@link Integer}&gt;.
 * <p>
 * Extracted from {@link IntBitSetBenchmark} and {@link IntSetBenchmark} so the
 * Java-side of the comparison is not duplicated across primitive-set
 * benchmarks. Use this class as a standalone baseline for {@code Set<Integer>}
 * performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaIntSetBenchmark
 * }</pre>
 */
public class JavaIntSetBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public Set<Integer> hashSet;

		/** Random keys that are present in the set. */
		public int[] presentKeys;
		/** Random keys that are absent from the set. */
		public int[] absentKeys;
		/** Random keys used for insertion benchmarks. */
		public int[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			hashSet = new HashSet<>(capacity);
			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.sequentialIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				hashSet.add(key);
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

		public Set<Integer> hashSet;

		@Setup
		public void setup(BenchmarkState state) {
			hashSet = new HashSet<>(state.hashSet);
		}
	}

	// ------------------------------------------------------------------
	// Contains — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long hashSet_containsPresent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.presentKeys) {
			if (data.hashSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Contains — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long hashSet_containsAbsent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.absentKeys) {
			if (data.hashSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Add — unique elements
	// ------------------------------------------------------------------

	@Benchmark
	public boolean hashSet_add(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.insertKeys) {
			if (ts.hashSet.add(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Remove — elements that are present
	// ------------------------------------------------------------------

	@Benchmark
	public boolean hashSet_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.presentKeys) {
			if (ts.hashSet.remove(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements
	// ------------------------------------------------------------------

	@Benchmark
	public long hashSet_iterate(BenchmarkState data) {
		long sum = 0;
		for (Integer v : data.hashSet) {
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterator with remove()
	// ------------------------------------------------------------------

	@Benchmark
	public void hashSet_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
		ts.hashSet.clear();
		ts.hashSet.addAll(data.hashSet);
		Iterator<Integer> it = ts.hashSet.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		Blackhole.consumeCPU(1);
	}

	// ------------------------------------------------------------------
	// Bulk containsAll
	// ------------------------------------------------------------------

	@Benchmark
	public boolean hashSet_containsAll(BenchmarkState data) {
		return data.hashSet.containsAll(data.hashSet);
	}
}
