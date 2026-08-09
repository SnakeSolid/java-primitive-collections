# Benchmark Results

JMH micro-benchmarks comparing each `simple-collections` class against its `java.util` equivalent.

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
| `IntBitSet` | `HashSet<Integer>` | **5.40x** | Most operations (especially iterate) |
| `IntSet` | `HashSet<Integer>` | **1.87x** | add, iterate, containsPresent (large data) |
| `ObjectSet` | `HashSet<String>` | **0.95x** | iterate |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **2.02x** | put, iterate, getAbsent (large data) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **0.85x** | put |
| `ObjectMap` | `HashMap<String, String>` | **0.55x** | put (small data, ~0.9x) |

## Set Benchmarks

### IntBitSet vs HashSet\<Integer\>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 239.3 | 70.0 | 3.42x |
| add | 100 K | 244.5 | 59.9 | 4.08x |
| add | 1 M | 239.6 | 52.1 | 4.60x |
| containsAbsent | 10 K | 228.0 | 215.1 | 1.06x |
| containsAbsent | 100 K | 20.4 | 19.8 | 1.03x |
| containsAbsent | 1 M | 2.0 | 1.6 | 1.29x |
| containsAll | 10 K | 18.0 | 62.1 | 0.29x |
| containsAll | 100 K | 1.9 | 5.0 | 0.38x |
| containsAll | 1 M | 0.1 | 0.4 | 0.21x |
| containsPresent | 10 K | 104.7 | 81.3 | 1.29x |
| containsPresent | 100 K | 9.8 | 4.4 | 2.20x |
| containsPresent | 1 M | 0.9 | 0.1 | 7.08x |
| iterate | 10 K | 233.0 | 19.2 | 12.17x |
| iterate | 100 K | 21.6 | 2.0 | 10.82x |
| iterate | 1 M | 2.1 | 0.1 | 20.94x |
| iterateRemoveAll | 10 K | 114.4 | 8.1 | 14.09x |
| iterateRemoveAll | 100 K | 9.8 | 0.8 | 12.60x |
| iterateRemoveAll | 1 M | 0.9 | 0.1 | 11.84x |
| remove | 10 K | 95.7 | 88.5 | 1.08x |
| remove | 100 K | 8.4 | 6.7 | 1.26x |
| remove | 1 M | 0.9 | 0.5 | 1.68x |

**Analysis:** `IntBitSet` dominates for iteration (10-21x faster) because it walks a compact `int[]` with no object overhead. Bit operations (contains, add, remove) are ~1-4x faster. `containsAll` is slower because HashSet's bulk check is optimized for its internal structure.

### IntSet vs HashSet\<Integer\>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 108.6 | 37.8 | 2.88x |
| add | 100 K | 153.5 | 50.8 | 3.02x |
| add | 1 M | 72.6 | 22.2 | 3.27x |
| containsAbsent | 10 K | 127.3 | 155.8 | 0.82x |
| containsAbsent | 100 K | 15.4 | 13.6 | 1.13x |
| containsAbsent | 1 M | 0.5 | 0.2 | 3.10x |
| containsAll | 10 K | 15.2 | 42.4 | 0.36x |
| containsAll | 100 K | 1.7 | 2.8 | 0.59x |
| containsAll | 1 M | 0.1 | 0.3 | 0.19x |
| containsPresent | 10 K | 67.1 | 57.0 | 1.18x |
| containsPresent | 100 K | 5.1 | 4.1 | 1.25x |
| containsPresent | 1 M | 0.2 | 0.1 | 2.01x |
| iterate | 10 K | 103.1 | 18.6 | 5.53x |
| iterate | 100 K | 4.6 | 2.0 | 2.38x |
| iterate | 1 M | 0.9 | 0.2 | 3.54x |
| remove | 10 K | 87.0 | 96.3 | 0.90x |
| remove | 100 K | 7.2 | 8.5 | 0.84x |
| remove | 1 M | 0.7 | 1.0 | 0.73x |

**Analysis:** `IntSet` wins on write-heavy workloads (add is ~3x faster) and iteration (~2-5x). Lookup is competitive, winning on large datasets. `containsAll` and `remove` lag because backward-shift deletion adds overhead per operation.

### ObjectSet vs HashSet\<String\>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 62.9 | 62.7 | 1.00x |
| add | 100 K | 83.8 | 61.3 | 1.37x |
| add | 1 M | 75.2 | 66.1 | 1.14x |
| containsAbsent | 10 K | 74.5 | 174.1 | 0.43x |
| containsAbsent | 100 K | 4.9 | 14.0 | 0.35x |
| containsAbsent | 1 M | 0.2 | 0.4 | 0.49x |
| containsAll | 10 K | 12.5 | 23.5 | 0.53x |
| containsAll | 100 K | 1.4 | 1.8 | 0.77x |
| containsAll | 1 M | 0.1 | 0.1 | 0.81x |
| containsPresent | 10 K | 51.2 | 85.3 | 0.60x |
| containsPresent | 100 K | 3.3 | 7.0 | 0.47x |
| containsPresent | 1 M | 0.1 | 0.1 | 0.80x |
| iterate | 10 K | 44.4 | 17.9 | 2.47x |
| iterate | 100 K | 3.2 | 1.8 | 1.72x |
| iterate | 1 M | 0.2 | 0.1 | 1.80x |
| remove | 10 K | 93.7 | 117.4 | 0.80x |
| remove | 100 K | 4.2 | 5.9 | 0.71x |
| remove | 1 M | 0.2 | 0.2 | 0.83x |

**Analysis:** `ObjectSet` is closest in performance to `HashSet` of all generic classes. It wins on iteration (1.7-2.5x) due to simpler array layout. Lookup operations are 40-60% of HashSet speed, likely because `Objects.equals()` adds overhead compared to HashMap's internal key comparison.

## Map Benchmarks

### IntToIntMap vs HashMap\<Integer, Integer\>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 120.2 | 173.7 | 0.69x |
| getAbsent | 100 K | 36.6 | 14.8 | 2.47x |
| getAbsent | 1 M | 0.5 | 0.1 | 4.15x |
| getPresent | 10 K | 80.6 | 55.9 | 1.44x |
| getPresent | 100 K | 7.9 | 3.9 | 2.01x |
| getPresent | 1 M | 0.2 | 0.1 | 2.50x |
| iterate | 10 K | 78.2 | 51.4 | 1.52x |
| iterate | 100 K | 8.7 | 4.1 | 2.11x |
| iterate | 1 M | 0.2 | 0.1 | 3.18x |
| iterateEntries | 10 K | 25.2 | 74.5 | 0.34x |
| iterateEntries | 100 K | 2.1 | 3.8 | 0.54x |
| iterateEntries | 1 M | 0.2 | 0.3 | 0.59x |
| keySetIterate | 10 K | 25.5 | 73.6 | 0.35x |
| keySetIterate | 100 K | 2.1 | 3.9 | 0.54x |
| keySetIterate | 1 M | 0.2 | 0.1 | 2.12x |
| put | 10 K | 99.0 | 37.4 | 2.65x |
| put | 100 K | 390.4 | 54.6 | 7.15x |
| put | 1 M | 89.8 | 24.9 | 3.61x |
| remove | 10 K | 123.7 | 93.0 | 1.33x |
| remove | 100 K | 14.5 | 8.6 | 1.69x |
| remove | 1 M | 1.4 | 0.9 | 1.54x |

**Analysis:** `IntToIntMap` is the strongest map performer — put is 3-7x faster (eliminating Integer boxing for both key and value). Read operations improve with data size as the linear-probe cache locality advantage grows. Entry/key iteration is slower at small sizes due to view-object overhead, but catches up at 1M.

### ObjectToIntMap vs HashMap\<String, Integer\>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 167.4 | 251.5 | 0.67x |
| getAbsent | 100 K | 9.7 | 18.1 | 0.54x |
| getAbsent | 1 M | 0.2 | 0.6 | 0.42x |
| getPresent | 10 K | 60.1 | 87.0 | 0.69x |
| getPresent | 100 K | 3.2 | 7.7 | 0.41x |
| getPresent | 1 M | 0.1 | 0.1 | 0.69x |
| iterateEntries | 10 K | 19.5 | 68.7 | 0.28x |
| iterateEntries | 100 K | 1.9 | 3.8 | 0.51x |
| iterateEntries | 1 M | 0.1 | 0.2 | 0.69x |
| keySetIterate | 10 K | 18.4 | 33.0 | 0.56x |
| keySetIterate | 100 K | 1.7 | 2.7 | 0.63x |
| keySetIterate | 1 M | 0.1 | 0.1 | 0.63x |
| put | 10 K | 121.5 | 60.2 | 2.02x |
| put | 100 K | 101.5 | 49.1 | 2.07x |
| put | 1 M | 66.1 | 42.1 | 1.57x |
| remove | 10 K | 128.0 | 145.7 | 0.88x |
| remove | 100 K | 12.6 | 8.9 | 1.40x |
| remove | 1 M | 0.2 | 0.2 | 0.73x |

**Analysis:** `ObjectToIntMap` wins on writes (put ~2x) because it avoids boxing values. However, reads are ~40-70% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is notably slower at small sizes (0.28x).

### ObjectMap vs HashMap\<String, String\>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 79.0 | 179.8 | 0.44x |
| getAbsent | 100 K | 5.1 | 17.3 | 0.29x |
| getAbsent | 1 M | 0.2 | 0.6 | 0.38x |
| getPresent | 10 K | 43.8 | 85.7 | 0.51x |
| getPresent | 100 K | 2.6 | 7.6 | 0.35x |
| getPresent | 1 M | 0.1 | 0.1 | 0.52x |
| iterateEntries | 10 K | 16.9 | 60.4 | 0.28x |
| iterateEntries | 100 K | 1.7 | 3.5 | 0.48x |
| iterateEntries | 1 M | 0.1 | 0.3 | 0.43x |
| keySetIterate | 10 K | 25.2 | 66.7 | 0.38x |
| keySetIterate | 100 K | 2.3 | 3.5 | 0.66x |
| keySetIterate | 1 M | 0.2 | 0.3 | 0.69x |
| put | 10 K | 30.0 | 32.7 | 0.92x |
| put | 100 K | 23.7 | 30.4 | 0.78x |
| put | 1 M | 23.6 | 27.2 | 0.87x |
| remove | 10 K | 99.3 | 155.9 | 0.64x |
| remove | 100 K | 5.3 | 8.6 | 0.61x |
| remove | 1 M | 0.2 | 0.3 | 0.69x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~30-90% of HashMap throughput. The main advantage is simplicity (no Node objects) — HashMap's `Node[]` + bin trees are heavily optimized by HotSpot. `ObjectMap` is competitive on put (0.8-0.9x) where no tree-balancing overhead exists.

## Conclusions

### When to use simple-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| Bitset for non-negative integers | `IntBitSet` | 5.4x average speedup; up to 21x for iteration |
| Set of int values | `IntSet` | 1.9x average; 3x faster for writes |
| Map of int-to-int | `IntToIntMap` | 2.0x average; 7x faster for writes at medium size |
| Set of objects (iterative workloads) | `ObjectSet` | Near parity with HashSet; 2x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | Near parity; 2x faster writes |
| Generic map (small data) | `ObjectMap` | Within 10% of HashMap on writes |

### Design Trade-offs

- **Primitive classes win on throughput** — zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** — walking a compact bit array is ~20x faster than iterating `HashSet` Node objects.
- **Object classes trade speed for simplicity** — `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **`containsAll` is slower** across the board because it uses the generic `Collection<?>` interface, losing type-specific optimizations.
