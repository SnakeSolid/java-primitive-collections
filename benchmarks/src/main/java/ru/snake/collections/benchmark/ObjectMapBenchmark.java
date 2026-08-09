package ru.snake.collections.benchmark;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import ru.snake.collections.map.ObjectMap;

/**
 * Benchmarks for {@link ObjectMap}.
 * <p>
 * Java's {@code Map<String, String>} baseline is provided by
 * {@link JavaStringMapBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectMapBenchmark
 * }</pre>
 */
public class ObjectMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public ObjectMap<String, String> objectMap;

		/** Keys that are present in both maps. */
		public String[] presentKeys;
		/** Keys that are absent from both maps. */
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

			objectMap = new ObjectMap<>(capacity);

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
				objectMap.put(key, value);
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

		public ObjectMap<String, String> objectMap;
		public Map<String, String> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			objectMap = new ObjectMap<>(state.capacity);
			for (int i = 0; i < state.presentKeys.length; i++) {
				objectMap.put(state.presentKeys[i], state.presentValues[i]);
			}
		}
	}

	// ------------------------------------------------------------------
	// Get — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long objectMap_getPresent(BenchmarkState data) {
		long hits = 0;
		for (String key : data.presentKeys) {
			String v = data.objectMap.get(key);
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
	public long objectMap_getAbsent(BenchmarkState data) {
		long hits = 0;
		for (String key : data.absentKeys) {
			String v = data.objectMap.get(key);
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
	public boolean objectMap_put(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.insertKeys) {
			String oldValue = ts.objectMap.put(key, "value_" + key);
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
	public boolean objectMap_remove(ThreadState ts, BenchmarkState data) {
		boolean changed = false;
		for (String key : data.presentKeys) {
			String removed = ts.objectMap.remove(key);
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
	public long objectMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, String> entry : data.objectMap.entrySet()) {
			sum += entry.getValue().length();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over keySet
	// ------------------------------------------------------------------

	@Benchmark
	public long objectMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (String key : data.objectMap.keySet()) {
			sum += key.length();
		}
		return sum;
	}
}
