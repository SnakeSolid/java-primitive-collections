package ru.snake.primitive.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks for {@link HashMap}&lt;{@link Integer}, {@link Integer}&gt;.
 * <p>
 * Extracted from {@link IntToIntMapBenchmark} so the Java-side of the
 * comparison is not duplicated. Use this class as a standalone baseline for
 * {@code Map<Integer, Integer>} performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaIntMapBenchmark
 * }</pre>
 */
public class JavaIntMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public Map<Integer, Integer> hashMap;

		/** Random keys that are present in the map. */
		public int[] presentKeys;
		/** Random keys that are absent from the map. */
		public int[] absentKeys;
		/** Random keys used for insertion benchmarks. */
		public int[] insertKeys;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			hashMap = new HashMap<>(capacity);
			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.spacedIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				hashMap.put(key, key);
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

		public Map<Integer, Integer> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			hashMap = new HashMap<>(state.hashMap);
		}
	}

	// ------------------------------------------------------------------
	// Get — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_getPresent(BenchmarkState data) {
		long sum = 0;
		for (int key : data.presentKeys) {
			Integer v = data.hashMap.get(key);
			if (v != null) {
				sum += v;
			}
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Get — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_getAbsent(BenchmarkState data) {
		long hits = 0;
		for (int key : data.absentKeys) {
			if (data.hashMap.get(key) != null) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Put — new key-value pairs
	// ------------------------------------------------------------------

	@Benchmark
	public int hashMap_put(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.insertKeys) {
			Integer v = ts.hashMap.put(key, key);
			if (v != null) {
				sum += v;
			}
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Remove — keys that are present
	// ------------------------------------------------------------------

	@Benchmark
	public int hashMap_remove(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.presentKeys) {
			Integer v = ts.hashMap.remove(key);
			if (v != null) {
				sum += v;
			}
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over entrySet — sum values
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<Integer, Integer> entry : data.hashMap.entrySet()) {
			sum += entry.getValue();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over keySet
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (Integer key : data.hashMap.keySet()) {
			sum += key;
		}
		return sum;
	}
}
