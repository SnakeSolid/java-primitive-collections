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
import ru.snake.collections.set.ObjectSet;

/**
 * Benchmarks comparing {@link ObjectSet} with {@link HashSet}.
 *
 * <p>Element type is {@link String}. Run with:</p>
 * <pre>{@code
 *   mvn package -pl benchmarks && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar ObjectSetBenchmark
 * }</pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public class ObjectSetBenchmark {

    // ------------------------------------------------------------------
    // Shared state — one instance per invocation thread
    // ------------------------------------------------------------------

    /**
     * Holds all benchmark parameters and pre-built data sets so that
     * the warm / measurement loops work on identical, hot data.
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {

        /** Number of distinct elements the set contains. */
        @Param({ "10000", "100000", "1000000" })
        public int capacity;

        /** Fill-factor expressed as a percentage (0-100). */
        @Param({ "25" })
        public int fillPercent;

        public ObjectSet<String> objectSet;
        public Set<String> hashSet;

        /** Random keys that are present in both sets. */
        public String[] presentKeys;
        /** Random keys that are absent from both sets. */
        public String[] absentKeys;
        /** Random keys used for insertion benchmarks. */
        public String[] insertKeys;

        @Setup
        public void setup() {
            int count = (int) ((capacity * fillPercent) / 100.0);
            int absentCount = Math.max(count / 2, 100);
            int insertCount = Math.min(count, 1000);

            objectSet = new ObjectSet<>(capacity);
            hashSet = new HashSet<>(capacity);

            ThreadLocalRandom rng = ThreadLocalRandom.current();

            // Build a shuffled index array used to generate distinct string keys.
            String[] allKeys = new String[capacity];
            for (int i = 0; i < capacity; i++) {
                allKeys[i] = "key_" + i;
            }
            shuffle(allKeys, rng);

            // Populate present keys
            presentKeys = new String[count];
            for (int i = 0; i < count; i++) {
                String key = allKeys[i];
                objectSet.add(key);
                hashSet.add(key);
                presentKeys[i] = key;
            }

            // Absent keys
            absentKeys = new String[absentCount];
            for (int i = 0; i < absentCount; i++) {
                absentKeys[i] = allKeys[count + i];
            }

            // Insert keys
            insertKeys = new String[insertCount];
            for (int i = 0; i < insertCount; i++) {
                insertKeys[i] = allKeys[count + absentCount + i];
            }
        }

        private void shuffle(String[] a, ThreadLocalRandom rng) {
            for (int i = a.length - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                String tmp = a[i];
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

        public ObjectSet<String> objectSet;
        public Set<String> hashSet;

        @Setup
        public void setup(BenchmarkState state) {
            // Deep-copy sets so mutating benchmarks don't affect shared data
            objectSet = new ObjectSet<>(state.capacity);
            for (String k : state.presentKeys) {
                objectSet.add(k);
            }
            hashSet = new HashSet<>(state.hashSet);
        }
    }

    // ------------------------------------------------------------------
    // Contains — key is present
    // ------------------------------------------------------------------

    @Benchmark
    public long objectSet_containsPresent(BenchmarkState data) {
        long hits = 0;
        for (String key : data.presentKeys) {
            if (data.objectSet.contains(key)) {
                hits++;
            }
        }
        return hits;
    }

    @Benchmark
    public long hashSet_containsPresent(BenchmarkState data) {
        long hits = 0;
        for (String key : data.presentKeys) {
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
    public long objectSet_containsAbsent(BenchmarkState data) {
        long hits = 0;
        for (String key : data.absentKeys) {
            if (data.objectSet.contains(key)) {
                hits++;
            }
        }
        return hits;
    }

    @Benchmark
    public long hashSet_containsAbsent(BenchmarkState data) {
        long hits = 0;
        for (String key : data.absentKeys) {
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
    public boolean objectSet_add(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (String key : data.insertKeys) {
            if (ts.objectSet.add(key)) {
                changed = true;
            }
        }
        return changed;
    }

    @Benchmark
    public boolean hashSet_add(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (String key : data.insertKeys) {
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
    public boolean objectSet_remove(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (String key : data.presentKeys) {
            if (ts.objectSet.remove(key)) {
                changed = true;
            }
        }
        return changed;
    }

    @Benchmark
    public boolean hashSet_remove(ThreadState ts, BenchmarkState data) {
        boolean changed = false;
        for (String key : data.presentKeys) {
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
    public long objectSet_iterate(BenchmarkState data) {
        long sum = 0;
        for (String v : data.objectSet) {
            sum += v.hashCode();
        }
        return sum;
    }

    @Benchmark
    public long hashSet_iterate(BenchmarkState data) {
        long sum = 0;
        for (String v : data.hashSet) {
            sum += v.hashCode();
        }
        return sum;
    }

    // ------------------------------------------------------------------
    // Iterator with remove() — only HashSet, ObjectSet iterator doesn't support
    // remove()
    // ------------------------------------------------------------------

    @Benchmark
    public void hashSet_iterateRemoveAll(ThreadState ts, BenchmarkState data) {
        ts.hashSet.clear();
        ts.hashSet.addAll(data.hashSet);
        Iterator<String> it = ts.hashSet.iterator();
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
    public boolean objectSet_containsAll(BenchmarkState data) {
        return data.objectSet.containsAll(data.hashSet);
    }

    @Benchmark
    public boolean hashSet_containsAll(BenchmarkState data) {
        return data.hashSet.containsAll(data.objectSet);
    }
}
