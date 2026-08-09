package ru.snake.collections.benchmark;

import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import ru.snake.collections.set.ObjectSet;

/**
 * Benchmarks for {@link ObjectSet}.
 * <p>
 * Element type is {@link String}. Java's {@code Set<String>} baseline is
 * provided by {@link JavaStringSetBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectSetBenchmark
 * }</pre>
 */
public class ObjectSetBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public ObjectSet<String> objectSet;

		/** Keys that are present in both sets. */
		public String[] presentKeys;
		/** Keys that are absent from both sets. */
		public String[] absentKeys;
		/** Keys used for insertion benchmarks. */
		public String[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			objectSet = new ObjectSet<>(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			String[] allKeys = BenchmarkDataHelper.stringKeys(capacity);
			BenchmarkDataHelper.shuffle(allKeys, rng);

			presentKeys = new String[count];
			for (int i = 0; i < count; i++) {
				String key = allKeys[i];
				objectSet.add(key);
				presentKeys[i] = key;
			}

			absentKeys = new String[absentCount];
			for (int i = 0; i < absentCount; i++) {
				absentKeys[i] = allKeys[count + i];
			}

			insertKeys = new String[insertCount];
			for (int i = 0; i < insertCount; i++) {
				insertKeys[i] = allKeys[count + absentCount + i];
			}
		}
	}

	@State(Scope.Thread)
	public static class ThreadState {

		public ObjectSet<String> objectSet;

		@Setup
		public void setup(BenchmarkState state) {
			objectSet = new ObjectSet<>(state.capacity);
			for (String k : state.presentKeys) {
				objectSet.add(k);
			}
		}
	}

	// ------------------------------------------------------------------
	// Contains — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long objectSet_containsPresent(BenchmarkState data) {
		long hits = 0;
		for (String key : data.presentKeys) {
			if (data.objectSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Contains — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long objectSet_containsAbsent(BenchmarkState data) {
		long hits = 0;
		for (String key : data.absentKeys) {
			if (data.objectSet.contains(key)) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Add — unique elements
	// ------------------------------------------------------------------

	@Benchmark
	public boolean objectSet_add(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.insertKeys) {
			if (ts.objectSet.add(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Remove — elements that are present
	// ------------------------------------------------------------------

	@Benchmark
	public boolean objectSet_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.presentKeys) {
			if (ts.objectSet.remove(key)) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements
	// ------------------------------------------------------------------

	@Benchmark
	public long objectSet_iterate(BenchmarkState data) {
		long sum = 0;
		for (String v : data.objectSet) {
			sum += v.hashCode();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Bulk containsAll
	// ------------------------------------------------------------------

	@Benchmark
	public boolean objectSet_containsAll(BenchmarkState data) {
		return data.objectSet.containsAll(data.objectSet);
	}
}
