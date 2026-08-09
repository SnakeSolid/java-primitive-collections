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

import ru.snake.primitive.map.ObjectToIntMap;

/**
 * Benchmarks for {@link ObjectToIntMap}.
 * <p>
 * Java's {@code Map<String, Integer>} baseline is provided by
 * {@link JavaObjectToIntMapBenchmark}.
 * </p>
 *
 * <p>
 * Run with:
 * </p>
 * 
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectToIntMapBenchmark
 * }</pre>
 */
public class ObjectToIntMapBenchmark extends JMHConfig {

	@State(Scope.Benchmark)
	public static class BenchmarkState {

		@Param({ "10000", "100000", "1000000" })
		public int capacity;

		@Param({ "25" })
		public int fillPercent;

		public ObjectToIntMap<String> objToIntMap;

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

			ThreadLocalRandom rng = ThreadLocalRandom.current();

			int[] allIndices = BenchmarkDataHelper.spacedIndices(capacity);
			BenchmarkDataHelper.shuffle(allIndices, rng);

			presentKeys = new String[count];
			presentValues = new int[count];
			for (int i = 0; i < count; i++) {
				int value = allIndices[i];
				String key = "key_" + value;
				objToIntMap.putInt(key, value);
				presentKeys[i] = key;
				presentValues[i] = value;
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

		public ObjectToIntMap<String> objToIntMap;
		public HashMap<String, Integer> hashMap;

		@Setup
		public void setup(BenchmarkState state) {
			objToIntMap = new ObjectToIntMap<>(state.capacity);
			for (int i = 0; i < state.presentKeys.length; i++) {
				objToIntMap.putInt(state.presentKeys[i], state.presentValues[i]);
			}
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
}
