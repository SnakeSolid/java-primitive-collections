# Benchmark Results

JMH micro-benchmarks comparing each `primitive-collections` class against its `java.util` equivalent.

## Configuration

| Parameter | Value |
|-----------|-------|
| JMH version | 1.37 |
| JDK | 21.0.11 (OpenJDK 64-Bit Server VM) |
| Mode | Throughput (ops/ms) |
| Warmup | 3 iterations x 2s |
| Measurement | 5 iterations x 2s |
| Forks | 2 |
| Heap | 512 MB |
| Fill factor | 25% |
| Capacities | 10 K, 100 K, 1 M |

**Higher ops/ms = faster.** A ratio > 1.0x means the custom class is faster than the Java reference.

## Quick Reference

| Class | Java Reference | Avg Speedup | Winner On |
|-------|---------------|-------------|-----------|
| `IntBitSet` | `HashSet<Integer>` | **5.46x** | Most operations (especially iterate) |
| `IntSet` | `HashSet<Integer>` | **1.72x** | add, iterate, containsPresent (large data) |
| `ObjectSet` | `HashSet<String>` | **0.77x** | iterate |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **1.85x** | put, iterate, getAbsent (large data), remove (large data) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **0.82x** | put |
| `ObjectMap` | `HashMap<String, String>` | **0.54x** | put (small data, ~0.9x) |

## Set Benchmarks

### IntBitSet vs HashSet\<Integer\>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 151.2 | 49.0 | 3.08x |
| add | 100 K | 135.0 | 46.4 | 2.91x |
| add | 1 M | 127.5 | 45.7 | 2.79x |
| containsAbsent | 10 K | 208.9 | 231.2 | 0.90x |
| containsAbsent | 100 K | 18.2 | 20.5 | 0.89x |
| containsAbsent | 1 M | 1.6 | 1.4 | 1.22x |
| containsAll | 10 K | 17.0 | 51.6 | 0.33x |
| containsAll | 100 K | 1.7 | 4.3 | 0.40x |
| containsAll | 1 M | 0.07 | 0.35 | 0.20x |
| containsPresent | 10 K | 86.0 | 82.4 | 1.04x |
| containsPresent | 100 K | 8.1 | 4.3 | 1.91x |
| containsPresent | 1 M | 0.7 | 0.1 | 7.31x |
| iterate | 10 K | 211.9 | 17.3 | 12.25x |
| iterate | 100 K | 19.9 | 1.8 | 11.13x |
| iterate | 1 M | 2.0 | 0.1 | 27.52x |
| iterateRemoveAll | 10 K | 92.2 | 7.2 | 12.73x |
| iterateRemoveAll | 100 K | 8.9 | 0.7 | 12.52x |
| iterateRemoveAll | 1 M | 0.7 | 0.07 | 10.38x |
| remove | 10 K | 56.2 | 54.4 | 1.03x |
| remove | 100 K | 5.7 | 6.0 | 0.94x |
| remove | 1 M | 0.7 | 0.4 | 1.70x |

**Analysis:** `IntBitSet` dominates for iteration (11-28x faster) because it walks a compact `int[]` with no object overhead. Add operations are ~3x faster. `containsAbsent` is slightly slower at small capacities but competitive at scale. `containsAll` is slower because HashSet's bulk check is optimized for its internal structure.

### IntSet vs HashSet\<Integer\>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 60.6 | 38.6 | 1.57x |
| add | 100 K | 108.7 | 44.8 | 2.43x |
| add | 1 M | 37.0 | 22.0 | 1.68x |
| containsAbsent | 10 K | 88.9 | 111.6 | 0.80x |
| containsAbsent | 100 K | 12.0 | 12.4 | 0.97x |
| containsAbsent | 1 M | 0.4 | 0.1 | 3.67x |
| containsAll | 10 K | 14.4 | 28.8 | 0.50x |
| containsAll | 100 K | 1.5 | 2.7 | 0.55x |
| containsAll | 1 M | 0.05 | 0.2 | 0.22x |
| containsPresent | 10 K | 60.5 | 64.7 | 0.94x |
| containsPresent | 100 K | 4.8 | 4.0 | 1.21x |
| containsPresent | 1 M | 0.1 | 0.1 | 2.06x |
| iterate | 10 K | 95.5 | 16.7 | 5.70x |
| iterate | 100 K | 4.3 | 1.7 | 2.49x |
| iterate | 1 M | 0.9 | 0.2 | 3.79x |
| remove | 10 K | 59.3 | 43.6 | 1.36x |
| remove | 100 K | 7.0 | 7.1 | 0.99x |
| remove | 1 M | 0.5 | 0.5 | 1.03x |

**Analysis:** `IntSet` wins on iteration (~2.5-5.7x) and is competitive on adds (1.6-2.4x). Lookup is mixed — `containsAbsent` is slightly slower at small sizes but wins 3.7x at 1M. `containsAll` lags significantly. Remove is near parity across all sizes.

### ObjectSet vs HashSet\<String\>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 52.5 | 52.9 | 0.99x |
| add | 100 K | 41.4 | 53.1 | 0.78x |
| add | 1 M | 39.4 | 57.9 | 0.68x |
| containsAbsent | 10 K | 67.8 | 224.1 | 0.30x |
| containsAbsent | 100 K | 3.8 | 12.6 | 0.30x |
| containsAbsent | 1 M | 0.1 | 0.3 | 0.41x |
| containsAll | 10 K | 12.7 | 19.3 | 0.66x |
| containsAll | 100 K | 1.3 | 1.7 | 0.77x |
| containsAll | 1 M | 0.05 | 0.07 | 0.75x |
| containsPresent | 10 K | 46.7 | 111.6 | 0.42x |
| containsPresent | 100 K | 3.0 | 6.8 | 0.44x |
| containsPresent | 1 M | 0.1 | 0.1 | 0.62x |
| iterate | 10 K | 45.5 | 17.4 | 2.62x |
| iterate | 100 K | 3.1 | 1.7 | 1.77x |
| iterate | 1 M | 0.1 | 0.1 | 1.49x |
| remove | 10 K | 82.8 | 75.9 | 1.09x |
| remove | 100 K | 5.1 | 5.7 | 0.89x |
| remove | 1M | 0.1 | 0.1 | 0.88x |

**Analysis:** `ObjectSet` wins on iteration (1.5-2.6x) due to simpler array layout. Lookup operations are notably slower (30-60% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison. Remove is near parity.

## Map Benchmarks

### IntToIntMap vs HashMap\<Integer, Integer\>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 161.9 | 205.4 | 0.79x |
| getAbsent | 100 K | 30.0 | 14.0 | 2.14x |
| getAbsent | 1 M | 0.6 | 0.1 | 5.59x |
| getPresent | 10 K | 113.0 | 55.7 | 2.03x |
| getPresent | 100 K | 7.8 | 4.0 | 1.97x |
| getPresent | 1 M | 0.2 | 0.1 | 2.89x |
| iterate | 10 K | 102.1 | 54.7 | 1.87x |
| iterate | 100 K | 7.7 | 4.0 | 1.93x |
| iterate | 1 M | 0.1 | 0.1 | 2.04x |
| iterateEntries | 10 K | 21.3 | 57.8 | 0.37x |
| iterateEntries | 100 K | 1.9 | 3.8 | 0.51x |
| iterateEntries | 1 M | 0.2 | 0.3 | 0.59x |
| keySetIterate | 10 K | 22.3 | 65.2 | 0.34x |
| keySetIterate | 100 K | 2.0 | 3.8 | 0.53x |
| keySetIterate | 1 M | 0.2 | 0.1 | 2.17x |
| put | 10 K | 78.9 | 37.3 | 2.12x |
| put | 100 K | 125.2 | 45.6 | 2.75x |
| put | 1 M | 85.2 | 21.7 | 3.92x |
| remove | 10 K | 63.3 | 148.3 | 0.43x |
| remove | 100 K | 10.1 | 8.1 | 1.25x |
| remove | 1 M | 2.3 | 0.9 | 2.69x |

**Analysis:** `IntToIntMap` is the strongest map performer — put is 2-4x faster (eliminating Integer boxing for both key and value). Read operations are consistently 2x faster at medium/large sizes, with `getAbsent` reaching 5.6x at 1M. Entry/key iteration is slower at small sizes (0.3-0.5x) due to view-object overhead, but native iteration is 1.9-2x faster. Remove is 2.7x at 1M, 1.3x at 100K, though slower at 10K (0.43x).

### ObjectToIntMap vs HashMap\<String, Integer\>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 149.3 | 346.6 | 0.43x |
| getAbsent | 100 K | 8.1 | 15.8 | 0.51x |
| getAbsent | 1 M | 0.2 | 0.5 | 0.36x |
| getPresent | 10 K | 56.3 | 117.2 | 0.48x |
| getPresent | 100 K | 3.3 | 7.2 | 0.45x |
| getPresent | 1 M | 0.1 | 0.1 | 0.66x |
| iterateEntries | 10 K | 17.0 | 71.0 | 0.24x |
| iterateEntries | 100 K | 1.7 | 3.8 | 0.45x |
| iterateEntries | 1 M | 0.1 | 0.2 | 0.72x |
| keySetIterate | 10 K | 18.0 | 31.4 | 0.57x |
| keySetIterate | 100 K | 1.5 | 2.7 | 0.56x |
| keySetIterate | 1 M | 0.1 | 0.1 | 0.92x |
| put | 10 K | 126.5 | 56.4 | 2.24x |
| put | 100 K | 97.9 | 52.1 | 1.88x |
| put | 1 M | 88.7 | 44.6 | 1.99x |
| remove | 10 K | 108.3 | 130.5 | 0.83x |
| remove | 100 K | 9.6 | 8.4 | 1.14x |
| remove | 1 M | 0.2 | 0.2 | 0.73x |

**Analysis:** `ObjectToIntMap` wins on writes (put ~2x) because it avoids boxing values. However, reads are ~36-51% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is notably slower at small sizes (0.24x). Remove is near parity at 10K but drops off at larger sizes.

### ObjectMap vs HashMap\<String, String\>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 71.6 | 230.2 | 0.31x |
| getAbsent | 100 K | 4.1 | 15.3 | 0.27x |
| getAbsent | 1 M | 0.2 | 0.4 | 0.41x |
| getPresent | 10 K | 41.6 | 105.3 | 0.40x |
| getPresent | 100 K | 2.7 | 7.1 | 0.38x |
| getPresent | 1 M | 0.0 | 0.1 | 0.56x |
| iterateEntries | 10 K | 16.7 | 61.3 | 0.27x |
| iterateEntries | 100 K | 1.8 | 3.4 | 0.53x |
| iterateEntries | 1 M | 0.1 | 0.3 | 0.44x |
| keySetIterate | 10 K | 22.0 | 61.1 | 0.36x |
| keySetIterate | 100 K | 2.3 | 3.4 | 0.67x |
| keySetIterate | 1 M | 0.2 | 0.2 | 0.71x |
| put | 10 K | 25.5 | 29.1 | 0.88x |
| put | 100 K | 22.2 | 26.6 | 0.84x |
| put | 1 M | 17.8 | 26.3 | 0.68x |
| remove | 10 K | 87.1 | 135.2 | 0.64x |
| remove | 100 K | 6.2 | 7.7 | 0.80x |
| remove | 1 M | 0.1 | 0.2 | 0.78x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~27-88% of HashMap throughput. The main advantage is simplicity (no Node objects) — HashMap's `Node[]` + bin trees are heavily optimized by HotSpot. `ObjectMap` is closest to HashMap on put (0.7-0.9x) where no tree-balancing overhead exists. Remove is ~64-80% of HashMap.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| Bitset for non-negative integers | `IntBitSet` | 5.5x average speedup; up to 28x for iteration |
| Set of int values | `IntSet` | 1.7x average; 2.4x faster for writes at medium size |
| Map of int-to-int | `IntToIntMap` | 1.9x average; 4x faster for writes at large size |
| Set of objects (iterative workloads) | `ObjectSet` | Slower than HashSet on lookups; 1.5-2.6x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | Near parity; 2x faster writes |
| Generic map (small data) | `ObjectMap` | Within 12% of HashMap on writes |

### Design Trade-offs

- **Primitive classes win on throughput** — zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** — walking a compact bit array is ~28x faster than iterating `HashSet` Node objects.
- **Object classes trade speed for simplicity** — `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **`containsAll` is slower** across the board because it uses the generic `Collection<?>` interface, losing type-specific optimizations.
- **ObjectSet is weakest on lookups** — `containsAbsent` and `containsPresent` are 30-45% of HashSet speed, making it unsuitable for read-heavy workloads.
