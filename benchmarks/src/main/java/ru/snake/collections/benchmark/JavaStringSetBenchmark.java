package ru.snake.collections.benchmark;

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
 * Benchmarks for {@link HashSet}&lt;{@link String}&gt;.
 * <p>
 * Extracted from {@link ObjectSetBenchmark} so the Java-side of the comparison
 * is not duplicated. Use this class as a standalone baseline for
 * {@code Set<String>} performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaStringSetBenchmark
 * }</pre>
 */
public class JavaStringSetBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public Set<String> hashSet;

		/** Keys that are present in the set. */
		public String[] presentKeys;
		/** Keys that are absent from the set. */
		public String[] absentKeys;
		/** Keys used for insertion benchmarks. */
		public String[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			hashSet = new HashSet<>(capacity);
			ThreadLocalRandom rng = ThreadLocalRandom.current();

			String[] allKeys = BenchmarkDataHelper.stringKeys(capacity);
			BenchmarkDataHelper.shuffle(allKeys, rng);

			presentKeys = new String[count];
			for (int i = 0; i < count; i++) {
				String key = allKeys[i];
				hashSet.add(key);
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

		public Set<String> hashSet;

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
		for (String key : data.presentKeys) {
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
		for (String key : data.absentKeys) {
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
		for (String key : data.insertKeys) {
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
		for (String key : data.presentKeys) {
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
		for (String v : data.hashSet) {
			sum += v.hashCode();
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
		Iterator<String> it = ts.hashSet.iterator();
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
