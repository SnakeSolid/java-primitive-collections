package ru.snake.collections.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import ru.snake.collections.map.ObjectMap;

/**
 * Benchmarks comparing {@link ObjectMap} with {@link HashMap}.
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectMapBenchmark
 * }</pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public class ObjectMapBenchmark {

	// ------------------------------------------------------------------
	// Shared state — one instance per invocation thread
	// ------------------------------------------------------------------

	/**
	 * Holds all benchmark parameters and pre-built data sets so that the warm /
	 * measurement loops work on identical, hot data.
	 */
	@State(Scope.Benchmark)
	public static class BenchmarkState {

		/** Number of distinct elements the map contains. */
		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		/** Fill-factor expressed as a percentage (0-100). */
		@Param({ "25" })
		public int fillPercent;

		public ObjectMap<String, String> objectMap;
		public Map<String, String> hashMap;

		/** Random keys that are present in both maps. */
		public String[] presentKeys;
		/** Random keys that are absent from both maps. */
		public String[] absentKeys;
		/** Random keys used for insertion benchmarks. */
		public String[] insertKeys;

		/** Values corresponding to {@link #presentKeys}. */
		public String[] presentValues;

		@Setup
		public void setup() {
			int count = (int) ((capacity * fillPercent) / 100.0);
			int absentCount = Math.max(count / 2, 100);
			int insertCount = Math.min(count, 1000);

			objectMap = new ObjectMap<>(capacity);
			hashMap = new HashMap<>(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			// Build a shuffled index array
			Integer[] allIndices = new Integer[capacity];
			for (int i = 0; i < capacity; i++) {
				allIndices[i] = i;
			}
			shuffle(allIndices, rng);

			// Populate present keys and values
			presentKeys = new String[count];
			presentValues = new String[count];
			for (int i = 0; i < count; i++) {
				int idx = allIndices[i];
				String key = "key_" + idx;
				String value = "value_" + idx;
				presentKeys[i] = key;
				presentValues[i] = value;
				objectMap.put(key, value);
				hashMap.put(key, value);
			}

			// Absent keys
			absentKeys = new String[absentCount];
			for (int i = 0; i < absentCount; i++) {
				absentKeys[i] = "key_" + allIndices[count + i];
			}

			// Insert keys
			insertKeys = new String[insertCount];
			for (int i = 0; i < insertCount; i++) {
				insertKeys[i] = "key_" + allIndices[count + absentCount + i];
			}
		}

		private void shuffle(Integer[] a, ThreadLocalRandom rng) {
			for (int i = a.length - 1; i > 0; i--) {
				int j = rng.nextInt(i + 1);
				Integer tmp = a[i];
				a[i] = a[j];
				a[j] = tmp;
			}
		}
	}

	// ------------------------------------------------------------------
	// Per-thread scratch pad — each fork/thread gets a fresh copy so that
	// put / remove benchmarks don't interfere with the shared data.
	// ------------------------------------------------------------------

	@State(Scope.Thread)
	public static class ThreadState {

		public ObjectMap<String, String> objectMap;
		public Map<String, String> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			// Deep-copy maps so mutating benchmarks don't affect shared data
			objectMap = new ObjectMap<>(state.capacity);
			for (int i = 0; i < state.presentKeys.length; i++) {
				objectMap.put(state.presentKeys[i], state.presentValues[i]);
			}
			hashMap = new HashMap<>(state.hashMap);
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
	// Iterate over entry set
	// ------------------------------------------------------------------

	@Benchmark
	public long objectMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, String> entry : data.objectMap.entrySet()) {
			sum += entry.getValue().length();
		}
		return sum;
	}

	@Benchmark
	public long hashMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, String> entry : data.hashMap.entrySet()) {
			sum += entry.getValue().length();
		}
		return sum;
	}

	// ------------------------------------------------------------------
	// Iterate over key set
	// ------------------------------------------------------------------

	@Benchmark
	public long objectMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (String key : data.objectMap.keySet()) {
			sum += key.length();
		}
		return sum;
	}

	@Benchmark
	public long hashMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (String key : data.hashMap.keySet()) {
			sum += key.length();
		}
		return sum;
	}
}
