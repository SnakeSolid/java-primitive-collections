package ru.snake.collections.benchmark;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import ru.snake.collections.set.IntBitSet;

/**
 * Benchmarks comparing {@link IntBitSet} with {@link HashSet<Integer>}.
 *
 * <p>Run with:</p>
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark
 * }</pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public class IntBitSetBenchmark {

    // ------------------------------------------------------------------
    // Shared state — one instance per invocation thread
    // ------------------------------------------------------------------

    /**
     * Holds all benchmark parameters and pre-built data sets so that
     * the warm / measurement loops work on identical, hot data.
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {

        /** Number of distinct elements the set can address. */
        @Param({ "10000", "100000", "1000000" })
        public int capacity;

        /** Fill-factor expressed as a percentage (0-100). */
        @Param({ "25" })
        public int fillPercent;

        public IntBitSet intBitSet;
        public Set<Integer> hashSet;

        /** Random keys that are present in both sets. */
        public int[] presentKeys;
        /** Random keys that are absent from both sets. */
        public int[] absentKeys;
        /** Random keys used for insertion benchmarks. */
        public int[] insertKeys;

        @Setup
        public void setup() {
            int count = (int) ((capacity * fillPercent) / 100.0);
            int absentCount = Math.max(count / 2, 100);
            int insertCount = Math.min(count, 1000);

            intBitSet = new IntBitSet(capacity);
            hashSet = new HashSet<>(capacity);

            ThreadLocalRandom rng = ThreadLocalRandom.current();

            // Build a shuffled index array
            int[] allIndices = new int[capacity];
            for (int i = 0; i < capacity; i++) {
                allIndices[i] = i;
            }
            shuffle(allIndices, rng);

            // Populate present keys
            presentKeys = new int[count];
            for (int i = 0; i < count; i++) {
                int key = allIndices[i];
                intBitSet.set(key);
                hashSet.add(key);
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
    // add / remove benchmarks don't interfere with the shared data.
    // ------------------------------------------------------------------

    @State(Scope.Thread)
    public static class ThreadState {

        public IntBitSet intBitSet;
        public Set<Integer> hashSet;

        @Setup
        public void setup(BenchmarkState state) {
            // Deep-copy sets so mutating benchmarks don't affect shared data
            intBitSet = new IntBitSet(state.capacity);
            for (int k : state.presentKeys) {
                intBitSet.set(k);
            }
            hashSet = new HashSet<>(state.hashSet);
        }
    }

    // ------------------------------------------------------------------
    // Contains — key is present
    // ------------------------------------------------------------------

    @Benchmark
    public long intBitSet_containsPresent(BenchmarkState data) {
        long hits = 0;
        for (int key : data.presentKeys) {
            if (data.intBitSet.contains(key)) {
                hits++;
            }
        }
        return hits;
    }

    @Benchmark
    public long hashSet_containsPresent(BenchmarkState data) {
        long hits = 0;
        for (int key : data.presentKeys) {
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
    public long intBitSet_containsAbsent(BenchmarkState data) {
        long hits = 0;
        for (int key : data.absentKeys) {
            if (data.intBitSet.contains(key)) {
                hits++;
            }
        }
        return hits;
    }

    @Benchmark
    public long hashSet_containsAbsent(BenchmarkState data) {
        long hits = 0;
        for (int key : data.absentKeys) {
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
    public boolean intBitSet_add(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (int key : data.insertKeys) {
            if (ts.intBitSet.add(key)) {
                changed = true;
            }
        }
        return changed;
    }

    @Benchmark
    public boolean hashSet_add(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (int key : data.insertKeys) {
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
    public boolean intBitSet_remove(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (int key : data.presentKeys) {
            if (ts.intBitSet.remove(key)) {
                changed = true;
            }
        }
        return changed;
    }

    @Benchmark
    public boolean hashSet_remove(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (int key : data.presentKeys) {
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
    public long intBitSet_iterate(BenchmarkState data) {
        long sum = 0;
        for (Integer v : data.intBitSet) {
            sum += v;
        }
        return sum;
    }

    @Benchmark
    public long hashSet_iterate(BenchmarkState data) {
        long sum = 0;
        for (Integer v : data.hashSet) {
            sum += v;
        }
        return sum;
    }

    // ------------------------------------------------------------------
    // Iterator with remove()
    // ------------------------------------------------------------------

    @Benchmark
    public void intBitSet_iterateRemoveAll(
        ThreadState ts,
        BenchmarkState data
    ) {
        ts.intBitSet.clear();
        for (int k : data.presentKeys) {
            ts.intBitSet.set(k);
        }
        Iterator<Integer> it = ts.intBitSet.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        Blackhole.consumeCPU(1);
    }

    @Benchmark
    public void hashSet_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
        ts.hashSet.clear();
        ts.hashSet.addAll(data.hashSet);
        Iterator<Integer> it = ts.hashSet.iterator();
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
    public boolean intBitSet_containsAll(BenchmarkState data) {
        return data.intBitSet.containsAll(data.hashSet);
    }

    @Benchmark
    public boolean hashSet_containsAll(BenchmarkState data) {
        return data.hashSet.containsAll(data.intBitSet);
    }
}
