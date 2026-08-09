package ru.snake.collections.benchmark;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import ru.snake.collections.map.IntToIntMap;

/**
 * Benchmarks for {@link IntToIntMap}.
 * <p>
 * Java's {@code Map<Integer, Integer>} baseline is provided by
 * {@link JavaIntMapBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntToIntMapBenchmark
 * }</pre>
 */
public class IntToIntMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public IntToIntMap intToIntMap;

		/** Random keys that are present in both maps. */
		public int[] presentKeys;
		/** Random keys that are absent from both maps. */
		public int[] absentKeys;
		/** Random keys used for insertion benchmarks. */
		public int[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			intToIntMap = new IntToIntMap(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.spacedIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				intToIntMap.put(key, key);
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

		public IntToIntMap intToIntMap;
		public Map<Integer, Integer> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			intToIntMap = new IntToIntMap(state.capacity);
			for (int k : state.presentKeys) {
				intToIntMap.put(k, k);
			}
		}
	}

	// ------------------------------------------------------------------
	// Get — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long intToIntMap_getPresent(BenchmarkState data) {
		long sum = 0;
		for (int key : data.presentKeys) {
			sum += data.intToIntMap.get(key);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Get — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long intToIntMap_getAbsent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.absentKeys) {
			int v = data.intToIntMap.get(key);
			if (v != 0) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Put — new key-value pairs
	// ------------------------------------------------------------------

	@Benchmark
	public int intToIntMap_put(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.insertKeys) {
			sum += ts.intToIntMap.put(key, key);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Remove — keys that are present
	// ------------------------------------------------------------------

	@Benchmark
	public int intToIntMap_remove(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.presentKeys) {
			sum += ts.intToIntMap.remove(key);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over all elements (sum values)
	// ------------------------------------------------------------------

	@Benchmark
	public long intToIntMap_iterate(BenchmarkState data) {
		long sum = 0;
		for (int key : data.presentKeys) {
			sum += data.intToIntMap.get(key);
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over entrySet
	// ------------------------------------------------------------------

	@Benchmark
	public long intToIntMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<Integer, Integer> entry : data.intToIntMap.entrySet()) {
			sum += entry.getValue();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over keySet
	// ------------------------------------------------------------------

	@Benchmark
	public long intToIntMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (Integer key : data.intToIntMap.keySet()) {
			sum += key;
		}
		return sum;
	}
}
