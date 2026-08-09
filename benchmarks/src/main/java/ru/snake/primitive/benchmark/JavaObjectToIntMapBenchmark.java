package ru.snake.primitive.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for {@link HashMap}&lt;{@link String}, {@link Integer}&gt;.
 * <p>
 * Extracted from {@link ObjectToIntMapBenchmark} so the Java-side of the
 * comparison is not duplicated. Use this class as a standalone baseline for
 * {@code Map<String, Integer>} performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaObjectToIntMapBenchmark
 * }</pre>
 */
public class JavaObjectToIntMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public HashMap<String, Integer> hashMap;

		/** Keys that are present in the map. */
		public String[] presentKeys;
		/** Keys that are absent from the map. */
		public String[] absentKeys;
		/** Keys used for insertion benchmarks. */
		public String[] insertKeys;
		/** Values associated with {@link #presentKeys}. */
		public int[] presentValues;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			hashMap = new HashMap<>(capacity);
			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.spacedIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new String[count];
			presentValues = new int[count];
			for (int i = 0; i < count; i++) {
				int value = allIndices[i];
				String key = "key_" + value;
				presentKeys[i] = key;
				presentValues[i] = value;
				hashMap.put(key, value);
			}

			absentKeys = new String[absentCount];
			for (int i = 0; i < absentCount; i++) {
				absentKeys[i] = "key_" + allIndices[count + i];
			}

			insertKeys = new String[insertCount];
			for (int i = 0; i < insertCount; i++) {
				insertKeys[i] = "key_" + allIndices[count + absentCount + i];
			}
		}
	}

	@State(Scope.Thread)
	public static class ThreadState {

		public HashMap<String, Integer> hashMap;

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
		for (String key : data.presentKeys) {
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
		long sum = 0;
		for (String key : data.absentKeys) {
			Integer v = data.hashMap.get(key);
			if (v != null) {
				sum += v;
			}
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Put — new key-value pairs
	// ------------------------------------------------------------------

	@Benchmark
	public void hashMap_put(ThreadState ts, BenchmarkState data) {
		for (int i = 0; i < data.insertKeys.length; i++) {
			ts.hashMap.put(data.insertKeys[i], data.presentValues[i]);
		}
		Blackhole.consumeCPU(1);
	}

	// ------------------------------------------------------------------
	// Remove — keys that are present
	// ------------------------------------------------------------------

	@Benchmark
	public void hashMap_remove(ThreadState ts, BenchmarkState data) {
		for (String key : data.presentKeys) {
			ts.hashMap.remove(key);
		}
		Blackhole.consumeCPU(1);
	}

	// ------------------------------------------------------------------
	// Iterate over entrySet — sum values
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, Integer> e : data.hashMap.entrySet()) {
			sum += e.getValue();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over keySet
	// ------------------------------------------------------------------

	@SuppressWarnings("unused")
	@Benchmark
	public void hashMap_keySetIterate(BenchmarkState data) {
		for (String key : data.hashMap.keySet()) {
			Blackhole.consumeCPU(1);
		}
	}
}
