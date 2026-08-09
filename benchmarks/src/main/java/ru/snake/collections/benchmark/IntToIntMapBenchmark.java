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
import ru.snake.collections.map.IntToIntMap;

/**
 * Benchmarks comparing {@link IntToIntMap} with {@link HashMap<Integer,
 * Integer>}.
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntToIntMapBenchmark
 * }</pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public class IntToIntMapBenchmark {

	// ------------------------------------------------------------------
	// Shared state — one instance per invocation thread
	// ------------------------------------------------------------------

	/**
	 * Holds all benchmark parameters and pre-built data maps so that the warm /
	 * measurement loops work on identical, hot data.
	 */
	@State(Scope.Benchmark)
	public static class BenchmarkState {

		/** Number of distinct entries the map contains. */
		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		/** Fill-factor expressed as a percentage (0-100). */
		@Param({ "25" })
		public int fillPercent;

		public IntToIntMap intToIntMap;
		public Map<Integer, Integer> hashMap;

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
			hashMap = new HashMap<>(capacity);

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			// Build a shuffled index array — values are spread across the
			// full positive int range to exercise the 27-bit key hashing.
			int[] allIndices = new int[capacity];
			for (int i = 0; i < capacity; i++) {
				allIndices[i] = i * 32; // spacing avoids slot collisions
			}
			shuffle(allIndices, rng);

			// Populate present keys and values (value = key for simplicity)
			presentKeys = new int[count];
			for (int i = 0; i < count; i++) {
				int key = allIndices[i];
				intToIntMap.put(key, key);
				hashMap.put(key, key);
				presentKeys[i] = key;
			}

			// Absent keys
			absentKeys = new int[absentCount];
			for (int i = 0; i < absentCount; i++) {
				absentKeys[i] = allIndices[count + i];
			}

			// Insert keys
			insertKeys = new int[insertCount];
			for (int i = 0; i < insertCount; i++) {
				insertKeys[i] = allIndices[count + absentCount + i];
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

		public IntToIntMap intToIntMap;
		public Map<Integer, Integer> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			// Deep-copy maps so mutating benchmarks don't affect shared data
			intToIntMap = new IntToIntMap(state.capacity);
			for (int k : state.presentKeys) {
				intToIntMap.put(k, k);
			}
			hashMap = new HashMap<>(state.hashMap);
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
	public int intToIntMap_put(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.insertKeys) {
			sum += ts.intToIntMap.put(key, key);
		}
		return sum;
	}

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
	public int intToIntMap_remove(ThreadState ts, BenchmarkState data) {
		int sum = 0;
		for (int key : data.presentKeys) {
			sum += ts.intToIntMap.remove(key);
		}
		return sum;
	}

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

	@Benchmark
	public long hashMap_iterate(BenchmarkState data) {
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
	public long intToIntMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (Integer key : data.intToIntMap.keySet()) {
			sum += key;
		}
		return sum;
	}

	@Benchmark
	public long hashMap_keySetIterate(BenchmarkState data) {
		long sum = 0;
		for (Integer key : data.hashMap.keySet()) {
			sum += key;
		}
		return sum;
	}
}
