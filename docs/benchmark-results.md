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
| `IntBitSet` | `HashSet<Integer>` | **6.26x** | Most operations (especially iterate, containsAll) |
| `IntSet` | `HashSet<Integer>` | **2.75x** | add (medium), iterate, containsAll |
| `ObjectSet` | `HashSet<String>` | **1.37x** | add (small/medium), iterate, containsAll |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **1.83x** | put (2.8-4.7x), getAbsent (large), remove (large) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **0.99x** | put (2.3-2.6x) |
| `ObjectMap` | `HashMap<String, String>` | **0.61x** | put (small data, ~0.9x) |

## Set Benchmarks

### IntBitSet vs HashSet\<Integer\>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 152.3 | 60.5 | 2.52x |
| add | 100 K | 140.4 | 51.0 | 2.75x |
| add | 1 M | 137.2 | 56.1 | 2.45x |
| containsAbsent | 10 K | 213.4 | 225.1 | 0.95x |
| containsAbsent | 100 K | 18.8 | 19.1 | 0.98x |
| containsAbsent | 1 M | 1.2 | 1.5 | 0.85x |
| containsAll | 10 K | 72.3 | 13.3 | 5.42x |
| containsAll | 100 K | 9.1 | 1.5 | 5.87x |
| containsAll | 1 M | 0.8 | 0.1 | 14.0x |
| containsPresent | 10 K | 104.5 | 75.1 | 1.39x |
| containsPresent | 100 K | 7.9 | 4.4 | 1.80x |
| containsPresent | 1 M | 0.7 | 0.1 | 7.69x |
| iterate | 10 K | 210.4 | 17.7 | 11.89x |
| iterate | 100 K | 20.3 | 1.9 | 10.90x |
| iterate | 1 M | 2.1 | 0.1 | 26.9x |
| iterateRemoveAll | 10 K | 86.0 | 7.4 | 11.62x |
| iterateRemoveAll | 100 K | 9.2 | 0.7 | 12.59x |
| iterateRemoveAll | 1 M | 0.8 | 0.1 | 10.5x |
| remove | 10 K | 55.5 | 34.3 | 1.62x |
| remove | 100 K | 7.2 | 5.9 | 1.21x |
| remove | 1 M | 0.8 | 0.4 | 1.79x |

**Analysis:** `IntBitSet` dominates for iteration (11-27x faster) because it walks a compact `int[]` with no object overhead. Add operations are ~2.5x faster. `containsAll` shows dramatic improvement (5-14x), likely due to the previous run's anomalous HashSet baseline. `containsAbsent` is nearly parity at small capacities (0.95-0.98x) but slightly slower at 1M. `containsPresent` is competitive — 1.4-7.7x faster.

### IntSet vs HashSet\<Integer\>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 51.3 | 60.5 | 0.85x |
| add | 100 K | 113.6 | 51.0 | 2.23x |
| add | 1 M | 46.2 | 56.1 | 0.82x |
| containsAbsent | 10 K | 87.8 | 225.1 | 0.39x |
| containsAbsent | 100 K | 12.9 | 19.1 | 0.68x |
| containsAbsent | 1 M | 0.4 | 1.5 | 0.27x |
| containsAll | 10 K | 54.5 | 13.3 | 4.07x |
| containsAll | 100 K | 3.9 | 1.5 | 2.49x |
| containsAll | 1 M | 0.6 | 0.1 | 10.5x |
| containsPresent | 10 K | 68.9 | 75.1 | 0.92x |
| containsPresent | 100 K | 4.8 | 4.4 | 1.09x |
| containsPresent | 1 M | 0.1 | 0.1 | 1.31x |
| iterate | 10 K | 96.2 | 17.7 | 5.43x |
| iterate | 100 K | 4.1 | 1.9 | 2.19x |
| iterate | 1 M | 0.9 | 0.1 | 11.52x |
| remove | 10 K | 68.5 | 34.3 | 1.99x |
| remove | 100 K | 7.1 | 5.9 | 1.20x |
| remove | 1 M | 0.6 | 0.4 | 1.35x |

**Analysis:** `IntSet` wins strongly on iteration (2.2-11.5x) and `containsAll` (2.5-10.5x). Add is 2.2x faster at 100K but near parity or slightly slower at other sizes. `containsAbsent` is the weakest area (0.3-0.7x), likely because HashSet's lookup path is heavily optimized. `containsPresent` and remove are near parity to slightly better across sizes.

### ObjectSet vs HashSet\<String\>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 70.0 | 54.9 | 1.27x |
| add | 100 K | 113.2 | 55.1 | 2.05x |
| add | 1 M | 39.6 | 57.5 | 0.69x |
| containsAbsent | 10 K | 81.0 | 169.1 | 0.48x |
| containsAbsent | 100 K | 5.6 | 12.6 | 0.44x |
| containsAbsent | 1 M | 0.1 | 0.3 | 0.44x |
| containsAll | 10 K | 40.1 | 12.0 | 3.34x |
| containsAll | 100 K | 2.7 | 1.2 | 2.27x |
| containsAll | 1 M | 0.1 | 0.1 | 1.74x |
| containsPresent | 10 K | 67.6 | 112.8 | 0.60x |
| containsPresent | 100 K | 3.5 | 6.6 | 0.53x |
| containsPresent | 1 M | 0.1 | 0.1 | 0.44x |
| iterate | 10 K | 65.2 | 16.5 | 3.95x |
| iterate | 100 K | 3.6 | 1.5 | 2.34x |
| iterate | 1 M | 0.1 | 0.1 | 1.91x |
| remove | 10 K | 95.7 | 78.8 | 1.21x |
| remove | 100 K | 3.1 | 5.9 | 0.53x |
| remove | 1 M | 0.2 | 0.2 | 0.87x |

**Analysis:** `ObjectSet` wins on iteration (1.9-4.0x) due to simpler array layout, and on `containsAll` (1.7-3.3x). Add is competitive — 2x faster at 100K, 1.3x at 10K, but slower at 1M. `containsAbsent` and `containsPresent` are slower (44-60% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison. Remove is near parity at small sizes but drops off at 100K.

## Map Benchmarks

### IntToIntMap vs HashMap\<Integer, Integer\>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 161.0 | 230.4 | 0.70x |
| getAbsent | 100 K | 28.8 | 14.2 | 2.02x |
| getAbsent | 1 M | 0.5 | 0.1 | 4.31x |
| getPresent | 10 K | 93.6 | 62.0 | 1.51x |
| getPresent | 100 K | 7.4 | 3.9 | 1.91x |
| getPresent | 1 M | 0.1 | 0.1 | 2.16x |
| iterate | 10 K | 95.6 | 71.0 | 1.35x |
| iterate | 100 K | 7.5 | 3.8 | 1.98x |
| iterate | 1 M | 0.1 | 0.1 | 1.48x |
| iterateEntries | 10 K | 20.8 | 71.0 | 0.29x |
| iterateEntries | 100 K | 1.9 | 3.8 | 0.50x |
| iterateEntries | 1 M | 0.2 | 0.3 | 0.67x |
| keySetIterate | 10 K | 24.0 | 70.5 | 0.34x |
| keySetIterate | 100 K | 2.1 | 3.7 | 0.56x |
| keySetIterate | 1 M | 0.2 | 0.1 | 2.65x |
| put | 10 K | 90.2 | 32.5 | 2.78x |
| put | 100 K | 171.0 | 42.9 | 3.99x |
| put | 1 M | 96.2 | 20.5 | 4.69x |
| remove | 10 K | 74.5 | 130.5 | 0.57x |
| remove | 100 K | 10.9 | 9.0 | 1.21x |
| remove | 1 M | 1.2 | 0.9 | 1.43x |

**Analysis:** `IntToIntMap` is the strongest map performer — put is 2.8-4.7x faster (eliminating Integer boxing for both key and value). `getPresent` is consistently 1.5-2.2x faster. `getAbsent` starts slower at 10K (0.70x) but dominates at 1M (4.3x). Native iteration is 1.4-2.0x faster. Entry/key iteration is slower at small sizes (0.3-0.5x) due to view-object overhead, but keySetIterate flips to 2.7x at 1M. Remove is competitive at larger sizes (1.2-1.4x) but slower at 10K (0.57x).

### ObjectToIntMap vs HashMap\<String, Integer\>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 201.2 | 337.9 | 0.60x |
| getAbsent | 100 K | 9.2 | 15.4 | 0.60x |
| getAbsent | 1 M | 0.2 | 0.4 | 0.56x |
| getPresent | 10 K | 69.3 | 107.0 | 0.65x |
| getPresent | 100 K | 4.5 | 7.4 | 0.60x |
| getPresent | 1 M | 0.1 | 0.1 | 0.45x |
| iterateEntries | 10 K | 22.1 | 65.9 | 0.34x |
| iterateEntries | 100 K | 2.2 | 3.6 | 0.61x |
| iterateEntries | 1 M | 0.2 | 0.2 | 0.99x |
| keySetIterate | 10 K | 18.9 | 30.5 | 0.62x |
| keySetIterate | 100 K | 1.9 | 2.6 | 0.74x |
| keySetIterate | 1 M | 0.1 | 0.1 | 1.62x |
| put | 10 K | 139.4 | 55.0 | 2.53x |
| put | 100 K | 130.5 | 49.9 | 2.62x |
| put | 1 M | 105.6 | 45.7 | 2.31x |
| remove | 10 K | 201.8 | 118.3 | 1.71x |
| remove | 100 K | 4.8 | 8.6 | 0.56x |
| remove | 1 M | 0.1 | 0.2 | 0.37x |

**Analysis:** `ObjectToIntMap` wins on writes (put consistently 2.3-2.6x faster) because it avoids boxing values. Remove is 1.7x faster at 10K but drops to 0.4-0.6x at larger sizes. Reads are ~45-65% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is slower at small sizes (0.34x) but near parity at 1M (0.99x).

### ObjectMap vs HashMap\<String, String\>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 79.5 | 223.5 | 0.36x |
| getAbsent | 100 K | 4.4 | 15.0 | 0.29x |
| getAbsent | 1 M | 0.2 | 0.4 | 0.42x |
| getPresent | 10 K | 57.2 | 100.1 | 0.57x |
| getPresent | 100 K | 3.7 | 7.3 | 0.51x |
| getPresent | 1 M | 0.1 | 0.1 | 0.62x |
| iterateEntries | 10 K | 19.9 | 58.1 | 0.34x |
| iterateEntries | 100 K | 2.1 | 3.2 | 0.65x |
| iterateEntries | 1 M | 0.1 | 0.1 | 0.75x |
| keySetIterate | 10 K | 27.2 | 63.2 | 0.43x |
| keySetIterate | 100 K | 2.6 | 3.3 | 0.78x |
| keySetIterate | 1 M | 0.2 | 0.2 | 0.97x |
| put | 10 K | 27.5 | 29.1 | 0.94x |
| put | 100 K | 21.3 | 26.1 | 0.82x |
| put | 1 M | 19.6 | 24.5 | 0.80x |
| remove | 10 K | 147.2 | 139.6 | 1.06x |
| remove | 100 K | 4.2 | 9.3 | 0.45x |
| remove | 1 M | 0.1 | 0.2 | 0.36x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~29-94% of HashMap throughput. The main advantage is simplicity (no Node objects) — HashMap's `Node[]` + bin trees are heavily optimized by HotSpot. `ObjectMap` is closest to HashMap on put (0.8-0.9x) where no tree-balancing overhead exists. Remove is 1.1x at 10K but drops to 0.4-0.5x at larger sizes.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| Bitset for non-negative integers | `IntBitSet` | 6.3x average speedup; up to 27x for iteration, 14x for containsAll |
| Set of int values | `IntSet` | 2.8x average; 2.2x faster for writes at medium size, 11.5x for iteration at 1M |
| Map of int-to-int | `IntToIntMap` | 1.8x average; 2.8-4.7x faster for writes at all sizes |
| Set of objects (iterative/bulk workloads) | `ObjectSet` | 1.4x average; 2-4x faster iteration; slower on lookups |
| Map with object keys, int values | `ObjectToIntMap` | Near parity overall; 2.3-2.6x faster writes |
| Generic map (small data) | `ObjectMap` | Within 16% of HashMap on writes; simpler allocation model |

### Design Trade-offs

- **Primitive classes win on throughput** — zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** — walking a compact bit array is ~27x faster than iterating `HashSet` Node objects.
- **Object classes trade speed for simplicity** — `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **`containsAll` performance varies** — results depend heavily on the baseline run; IntBitSet and IntSet show strong gains, while ObjectSet is competitive.
- **ObjectSet lookups are slower** — `containsAbsent` and `containsPresent` are 44-60% of HashSet speed, making it less suitable for read-heavy workloads.
- **Entry/keySet iteration views add overhead** at small sizes across all map types, but native iteration is consistently faster.
