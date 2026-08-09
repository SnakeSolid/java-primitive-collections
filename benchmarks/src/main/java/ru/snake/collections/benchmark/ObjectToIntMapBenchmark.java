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
import org.openjdk.jmh.infra.Blackhole;
import ru.snake.collections.map.ObjectToIntMap;

/**
 * Benchmarks comparing {@link ObjectToIntMap} with {@link HashMap}.
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectToIntMapBenchmark
 * }</pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public class ObjectToIntMapBenchmark {

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

		public ObjectToIntMap<String> objToIntMap;
		public HashMap<String, Integer> hashMap;

		/** Keys that are present in both maps. */
		public String[] presentKeys;
		/** Keys that are absent from both maps. */
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

			objToIntMap = new ObjectToIntMap<>(capacity);
			hashMap = new HashMap<>(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			// Build a shuffled index array — values are spread across the
			// full positive int range to generate diverse string keys.
			int[] allIndices = new int[capacity];
			for (int i = 0; i < capacity; i++) {
				allIndices[i] = i * 32; // spacing avoids slot collisions
			}
			shuffle(allIndices, rng);

			// Populate present keys and values
			presentKeys = new String[count];
			presentValues = new int[count];
			for (int i = 0; i < count; i++) {
				int value = allIndices[i];
				String key = "key_" + value;
				objToIntMap.putInt(key, value);
				hashMap.put(key, value);
				presentKeys[i] = key;
				presentValues[i] = value;
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

		private void shuffle(int[] a, ThreadLocalRandom rng) {
			for (int i = a.length - 1; i > 0; i--) {
				int j = rng.nextInt(i + 1);
				int tmp = a[i];
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

		public ObjectToIntMap<String> objToIntMap;
		public HashMap<String, Integer> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			// Deep-copy maps so mutating benchmarks don't affect shared data
			objToIntMap = new ObjectToIntMap<>(state.capacity);
			for (int i = 0; i < state.presentKeys.length; i++) {
				objToIntMap.putInt(state.presentKeys[i], state.presentValues[i]);
			}
			hashMap = new HashMap<>(state.hashMap);
		}
	}

	// ------------------------------------------------------------------
	// Get — key is present
	// ------------------------------------------------------------------

	@Benchmark
	public long objToIntMap_getPresent(BenchmarkState data) {
		long sum = 0;
		for (String key : data.presentKeys) {
			sum += data.objToIntMap.getInt(key);
		}
		return sum;
	}

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
	public long objToIntMap_getAbsent(BenchmarkState data) {
		long sum = 0;
		for (String key : data.absentKeys) {
			sum += data.objToIntMap.getInt(key);
		}
		return sum;
	}

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
	public void objToIntMap_put(ThreadState ts, BenchmarkState data) {
		for (int i = 0; i < data.insertKeys.length; i++) {
			ts.objToIntMap.putInt(data.insertKeys[i], data.presentValues[i]);
		}
		Blackhole.consumeCPU(1);
	}

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
	public void objToIntMap_remove(ThreadState ts, BenchmarkState data) {
		for (String key : data.presentKeys) {
			ts.objToIntMap.delete(key);
		}
		Blackhole.consumeCPU(1);
	}

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
	public long objToIntMap_iterateEntries(BenchmarkState data) {
		long sum = 0;
		for (Map.Entry<String, Integer> e : data.objToIntMap.entrySet()) {
			sum += e.getValue();
		}
		return sum;
	}

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
	public void objToIntMap_keySetIterate(BenchmarkState data) {
		for (String key : data.objToIntMap.keySet()) {
			Blackhole.consumeCPU(1);
		}
	}

	@SuppressWarnings("unused")
	@Benchmark
	public void hashMap_keySetIterate(BenchmarkState data) {
		for (String key : data.hashMap.keySet()) {
			Blackhole.consumeCPU(1);
		}
	}
}
