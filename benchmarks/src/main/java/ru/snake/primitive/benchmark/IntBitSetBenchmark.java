package ru.snake.primitive.benchmark;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import ru.snake.primitive.set.IntBitSet;

/**
 * Benchmarks for {@link IntBitSet}.
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
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark
 * }</pre>
 */
public class IntBitSetBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public IntBitSet intBitSet;

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

			intBitSet = new IntBitSet(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.sequentialIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				intBitSet.set(key);
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

		public IntBitSet intBitSet;
		public Set<Integer> hashSet;

		@Setup
		public void setup(BenchmarkState state) {
			intBitSet = new IntBitSet(state.capacity);
			for (int k : state.presentKeys) {
				intBitSet.set(k);
			}
		}
	}

	// ------------------------------------------------------------------
	// Contains — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long intBitSet_containsPresent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.presentKeys) {
			if (data.intBitSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Contains — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long intBitSet_containsAbsent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.absentKeys) {
			if (data.intBitSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Add — unique elements
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intBitSet_add(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.insertKeys) {
			if (ts.intBitSet.add(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Remove — elements that are present
	// ------------------------------------------------------------------

	@Benchmark
	public boolean intBitSet_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (int key : data.presentKeys) {
			if (ts.intBitSet.remove(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements
	// ------------------------------------------------------------------

	@Benchmark
	public long intBitSet_iterate(BenchmarkState data) {
		long sum = 0;
		for (Integer v : data.intBitSet) {
			sum += v;
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterator with remove()
	// ------------------------------------------------------------------

	@Benchmark
	public void intBitSet_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
		ts.intBitSet.clear();
		for (int k : data.presentKeys) {
			ts.intBitSet.set(k);
		}
		Iterator<Integer> it = ts.intBitSet.iterator();
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
	public boolean intBitSet_containsAll(BenchmarkState data) {
		return data.intBitSet.containsAll(data.intBitSet);
	}
}
