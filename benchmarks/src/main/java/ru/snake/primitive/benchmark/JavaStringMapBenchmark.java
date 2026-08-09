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
 * Benchmarks for {@link HashMap}&lt;{@link String}, {@link String}&gt;.
 * <p>
 * Extracted from {@link ObjectMapBenchmark} so the Java-side of the comparison
 * is not duplicated. Use this class as a standalone baseline for
 * {@code Map<String, String>} performance.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar JavaStringMapBenchmark
 * }</pre>
 */
public class JavaStringMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public Map<String, String> hashMap;

		/** Keys that are present in the map. */
		public String[] presentKeys;
		/** Keys that are absent from the map. */
		public String[] absentKeys;
		/** Keys used for insertion benchmarks. */
		public String[] insertKeys;
		/** Values corresponding to {@link #presentKeys}. */
		public String[] presentValues;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			hashMap = new HashMap<>(capacity);
			ThreadLocalRandom rng = ThreadLocalRandom.current();

			Integer[] allIndices = new Integer[capacity];
			for (int i = 0; i < capacity; i++) {
				allIndices[i] = i;
			}
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new String[count];
			presentValues = new String[count];
			for (int i = 0; i < count; i++) {
				int idx = allIndices[i];
				String key = "key_" + idx;
				String value = "value_" + idx;
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

		public Map<String, String> hashMap;

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
		long hits = 0;
		for (String key : data.presentKeys) {
			String v = data.hashMap.get(key);
			if (v != null) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Get — key is absent
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_getAbsent(BenchmarkState data) {
		long hits = 0;
		for (String key : data.absentKeys) {
			String v = data.hashMap.get(key);
			if (v != null) {
				hits++;
			}
		}
		return hits;
	}

	// ------------------------------------------------------------------
	// Put — new key-value pairs
	// ------------------------------------------------------------------

	@Benchmark
	public boolean hashMap_put(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.insertKeys) {
			String oldValue = ts.hashMap.put(key, "value_" + key);
			if (oldValue == null) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Remove — keys that are present
	// ------------------------------------------------------------------

	@Benchmark
	public boolean hashMap_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.presentKeys) {
			String removed = ts.hashMap.remove(key);
			if (removed != null) {
				changed = true;
			}
		}
		return changed;
	}

	// ------------------------------------------------------------------
	// Iterate over entrySet
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, String> entry : data.hashMap.entrySet()) {
			sum += entry.getValue().length();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over keySet
	// ------------------------------------------------------------------

	@Benchmark
	public long hashMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (String key : data.hashMap.keySet()) {
			sum += key.length();
		}
		return sum;
	}
}
