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

For memory footprint comparisons, see [Memory Benchmarks](./memory-benchmarks.md).

## Quick Reference

| Class | Java Reference | Avg Speedup | Winner On |
|-------|---------------|-------------|-----------|
| `IntList` | `ArrayList<Integer>` | **2.1x** | get (2.2x), contains (0.7-6.7x), iterate (1.4-7.7x), setInt (6.8-7.5x), iterateRemoveAll (1.1x) |
| `IntBitSet` | `HashSet<Integer>` | **4.1x** | iterate, containsAll, iterateRemoveAll |
| `IntSet` | `HashSet<Integer>` | **1.4x** | add, containsAll, iterate |
| `ObjectSet` | `HashSet<String>` | **1.0x** | add (small/medium), iterate, containsAll, remove (small) |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **3.0x** | put, getAbsent, getPresent, iterate, remove |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **1.3x** | put (0.9-2.5x), remove (10K) |
| `ObjectMap` | `HashMap<String, String>` | **0.7x** | put (10K, ~1.1x), remove (10K) |

## List Benchmarks

### IntList vs ArrayList<Integer>

| Operation | Capacity | IntList | ArrayList | Ratio |
|---|---|---|---|---|
| contains | 10 K | 667.8 | 119.9 | 5.57x |
| contains | 100 K | 66.7 | 16.6 | 4.03x |
| contains | 1 M | 4.0 | 0.99 | 3.99x |
| get | 10 K | 1779.4 | 796.0 | 2.24x |
| get | 100 K | 1717.2 | 775.5 | 2.21x |
| get | 1 M | 1668.9 | 773.8 | 2.16x |
| getBoxed | 10 K | 1788.7 | 796.0 | 2.25x |
| getBoxed | 100 K | 1704.0 | 775.5 | 2.20x |
| getBoxed | 1 M | 1683.7 | 773.8 | 2.18x |
| iterate | 10 K | 776.6 | 206.6 | 3.76x |
| iterate | 100 K | 79.1 | 19.3 | 4.10x |
| iterate | 1 M | 7.8 | 1.0 | 7.73x |
| iterateBoxed | 10 K | 343.7 | 206.6 | 1.66x |
| iterateBoxed | 100 K | 35.0 | 19.3 | 1.81x |
| iterateBoxed | 1 M | 3.4 | 1.0 | 3.35x |
| iterateRemoveAll | 10 K | 203169.5 | 184814.6 | 1.10x |
| iterateRemoveAll | 100 K | 152660.3 | 140041.3 | 1.09x |
| iterateRemoveAll | 1 M | 152730.6 | 137660.5 | 1.11x |
| setInt | 10 K | 1098.2 | 146.7 | 7.49x |
| setInt | 100 K | 1001.6 | 143.7 | 6.97x |
| setInt | 1 M | 984.4 | 143.6 | 6.85x |

**Operations:**

| Operation | Description |
|-----------|-------------|
| `contains` | Linear scan for element not in list |
| `get` | Random index read via `getInt()` (primitive, no boxing) |
| `getBoxed` | Random index read via `get()` (boxed, via `List` interface) |
| `iterate` | Sequential scan via `getInt(i)` (primitive) |
| `iterateBoxed` | Enhanced for-each iteration (boxed) |
| `iterateRemoveAll` | Iterator with `remove()` -- clears the list |
| `setInt` | Random index write via `setInt()` (primitive, uses `ThreadState`) |

**Analysis:** `IntList` dominates across nearly all operations. Primitive `get` is ~2.2x faster than `ArrayList<Integer>`, and surprisingly `getBoxed` is also ~2.2x faster -- the contiguous array layout likely gives better cache performance than `ArrayList`'s `Object[]`. `setInt` is the standout at 6.9-7.5x faster. `contains` (linear scan) is 4.0-5.6x faster. Primitive `iterate` is 3.8-7.7x faster, scaling with capacity. Boxed `iterateBoxed` is 1.7-3.4x faster. Even `iterateRemoveAll` is 1.1x faster, showing the benefit of simpler internal structure.

## Set Benchmarks

### IntBitSet vs HashSet<Integer>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 152.3 | 53.3 | 2.86x |
| add | 100 K | 145.3 | 54.5 | 2.67x |
| add | 1 M | 137.2 | 46.6 | 2.94x |
| containsAbsent | 10 K | 214.3 | 237.5 | 0.90x |
| containsAbsent | 100 K | 19.5 | 19.5 | 1.00x |
| containsAbsent | 1 M | 1.5 | 1.4 | 1.09x |
| containsAll | 10 K | 180.4 | 13.1 | 13.80x |
| containsAll | 100 K | 16.5 | 1.5 | 10.75x |
| containsAll | 1 M | 1.6 | 0.06 | 27.25x |
| containsPresent | 10 K | 97.8 | 75.6 | 1.29x |
| containsPresent | 100 K | 8.6 | 4.4 | 1.97x |
| containsPresent | 1 M | 0.76 | 0.09 | 8.38x |
| iterate | 10 K | 213.9 | 17.9 | 11.96x |
| iterate | 100 K | 20.6 | 1.9 | 11.02x |
| iterate | 1 M | 2.05 | 0.08 | 26.73x |
| iterateRemoveAll | 10 K | 87.1 | 7.3 | 11.88x |
| iterateRemoveAll | 100 K | 9.3 | 0.67 | 13.74x |
| iterateRemoveAll | 1 M | 0.92 | 0.07 | 12.91x |
| remove | 10 K | 58.6 | 56.0 | 1.05x |
| remove | 100 K | 7.9 | 5.6 | 1.43x |
| remove | 1 M | 0.73 | 0.40 | 1.81x |

**Analysis:** `IntBitSet` dominates for iteration (11-27x faster) because it walks a compact bit array with no object overhead. `containsAll` shows dramatic improvement (10.8-27.3x). Add operations are ~2.7-2.9x faster. `containsAbsent` is near parity across all capacities (0.90-1.09x). `containsPresent` is competitive at 10K (1.29x) and becomes much faster at larger sizes (1.97x at 100K, 8.4x at 1M). `iterateRemoveAll` is 11.9-13.7x faster. Remove is 1.0-1.8x faster, improving with capacity.

### IntSet vs HashSet<Integer>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 149.4 | 53.3 | 2.80x |
| add | 100 K | 139.3 | 54.5 | 2.56x |
| add | 1 M | 94.7 | 46.6 | 2.03x |
| containsAbsent | 10 K | 172.8 | 237.5 | 0.73x |
| containsAbsent | 100 K | 13.6 | 19.5 | 0.70x |
| containsAbsent | 1 M | 0.76 | 1.4 | 0.55x |
| containsAll | 10 K | 57.7 | 13.1 | 4.42x |
| containsAll | 100 K | 3.5 | 1.5 | 2.33x |
| containsAll | 1 M | 0.3 | 0.06 | 4.52x |
| containsPresent | 10 K | 59.4 | 75.6 | 0.79x |
| containsPresent | 100 K | 3.7 | 4.4 | 0.83x |
| containsPresent | 1 M | 0.14 | 0.09 | 1.46x |
| iterate | 10 K | 81.3 | 17.9 | 4.55x |
| iterate | 100 K | 4.0 | 1.9 | 2.15x |
| iterate | 1 M | 0.4 | 0.08 | 5.20x |
| remove | 10 K | 70.7 | 56.0 | 1.26x |
| remove | 100 K | 5.5 | 5.6 | 0.98x |
| remove | 1 M | 0.38 | 0.40 | 0.94x |

**Analysis:** `IntSet` wins on add (2.0-2.8x) and iteration (2.1-5.2x). `containsAll` is 2.3-4.5x faster. `containsAbsent` and `containsPresent` are the weaker areas -- 55-83% of HashSet speed at larger capacities, likely because HashSet's lookup path is heavily optimized. Remove is 1.3x faster at 10K but near parity at larger sizes (0.94-0.98x).

### ObjectSet vs HashSet<String>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 119.9 | 51.1 | 2.35x |
| add | 100 K | 108.6 | 50.3 | 2.16x |
| add | 1 M | 37.5 | 52.4 | 0.72x |
| containsAbsent | 10 K | 110.0 | 219.8 | 0.50x |
| containsAbsent | 100 K | 5.1 | 12.5 | 0.41x |
| containsAbsent | 1 M | 0.1 | 0.29 | 0.33x |
| containsAll | 10 K | 35.0 | 12.1 | 2.90x |
| containsAll | 100 K | 2.9 | 1.2 | 2.49x |
| containsAll | 1 M | 0.08 | 0.05 | 1.66x |
| containsPresent | 10 K | 72.6 | 112.4 | 0.65x |
| containsPresent | 100 K | 3.5 | 6.7 | 0.52x |
| containsPresent | 1 M | 0.07 | 0.12 | 0.56x |
| iterate | 10 K | 71.4 | 16.0 | 4.46x |
| iterate | 100 K | 4.1 | 1.3 | 3.13x |
| iterate | 1 M | 0.17 | 0.06 | 2.60x |
| remove | 10 K | 52.9 | 61.3 | 0.86x |
| remove | 100 K | 3.6 | 5.9 | 0.61x |
| remove | 1 M | 0.12 | 0.21 | 0.56x |

**Analysis:** `ObjectSet` wins on add at small/medium sizes (2.2-2.4x) and on iteration (2.6-4.5x) due to simpler array layout. `containsAll` is 1.7-2.9x faster. However, `containsAbsent`, `containsPresent`, and `remove` (at larger sizes) are slower (33-86% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison.

## Map Benchmarks

### IntToIntMap vs HashMap<Integer, Integer>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 442.1 | 199.0 | 2.22x |
| getAbsent | 100 K | 43.3 | 13.8 | 3.14x |
| getAbsent | 1 M | 1.28 | 0.09 | 13.90x |
| getPresent | 10 K | 161.1 | 51.8 | 3.11x |
| getPresent | 100 K | 7.8 | 3.7 | 2.09x |
| getPresent | 1 M | 0.20 | 0.05 | 4.10x |
| iterate | 10 K | 161.1 | 69.8 | 2.31x |
| iterate | 100 K | 7.3 | 3.7 | 1.96x |
| iterate | 1 M | 0.18 | 0.07 | 2.54x |
| iterateEntries | 10 K | 29.5 | 69.8 | 0.42x |
| iterateEntries | 100 K | 2.3 | 3.7 | 0.62x |
| iterateEntries | 1 M | 0.28 | 0.07 | 3.92x |
| keySetIterate | 10 K | 30.9 | 70.8 | 0.44x |
| keySetIterate | 100 K | 2.5 | 3.8 | 0.66x |
| keySetIterate | 1 M | 0.29 | 0.1 | 3.63x |
| put | 10 K | 157.6 | 34.1 | 4.62x |
| put | 100 K | 153.3 | 41.4 | 3.70x |
| put | 1 M | 186.3 | 21.6 | 8.67x |
| remove | 10 K | 75.0 | 137.0 | 0.55x |
| remove | 100 K | 10.0 | 8.1 | 1.24x |
| remove | 1 M | 0.82 | 0.86 | 0.96x |

**Analysis:** `IntToIntMap` is the strongest map performer -- put is 3.7-8.7x faster (eliminating Integer boxing for both key and value). `getAbsent` is 2.2-13.9x faster, dominating at all sizes. `getPresent` is 2.1-4.1x faster. Native iteration is 2.0-2.5x faster. Entry iteration is slower at small sizes (0.4-0.6x) due to view-object overhead, but 3.9x faster at 1M. keySetIterate is 0.4-0.7x at small/medium sizes but flips to 3.6x at 1M. Remove is slower at 10K (0.55x) but 1.2x at 100K and near parity (0.96x) at 1M.

### ObjectToIntMap vs HashMap<String, Integer>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 167.2 | 315.8 | 0.53x |
| getAbsent | 100 K | 8.3 | 17.1 | 0.49x |
| getAbsent | 1 M | 0.24 | 0.43 | 0.56x |
| getPresent | 10 K | 67.3 | 109.4 | 0.61x |
| getPresent | 100 K | 3.5 | 7.4 | 0.47x |
| getPresent | 1 M | 0.08 | 0.12 | 0.63x |
| iterateEntries | 10 K | 39.8 | 63.7 | 0.63x |
| iterateEntries | 100 K | 3.2 | 3.7 | 0.87x |
| iterateEntries | 1 M | 0.31 | 0.17 | 1.84x |
| keySetIterate | 10 K | 24.7 | 30.3 | 0.81x |
| keySetIterate | 100 K | 2.4 | 2.6 | 0.91x |
| keySetIterate | 1 M | 0.16 | 0.12 | 1.40x |
| put | 10 K | 135.8 | 53.3 | 2.55x |
| put | 100 K | 124.8 | 48.5 | 2.57x |
| put | 1 M | 91.1 | 42.7 | 2.13x |
| remove | 10 K | 171.1 | 111.6 | 1.53x |
| remove | 100 K | 8.0 | 8.6 | 0.94x |
| remove | 1 M | 0.10 | 0.20 | 0.49x |

**Analysis:** `ObjectToIntMap` wins on writes -- put is 2.1-2.6x faster because it avoids boxing values. Remove is 1.5x faster at 10K but drops to 0.5-0.9x at larger sizes. Reads are ~47-63% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is 0.6-0.9x at small/medium sizes but flips to 1.8x at 1M. keySetIterate is near parity at small/medium sizes (0.8-0.9x) and 1.4x at 1M.

### ObjectMap vs HashMap<String, String>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 118.0 | 235.8 | 0.50x |
| getAbsent | 100 K | 5.7 | 14.7 | 0.39x |
| getAbsent | 1 M | 0.14 | 0.41 | 0.35x |
| getPresent | 10 K | 66.3 | 108.1 | 0.61x |
| getPresent | 100 K | 3.2 | 7.2 | 0.45x |
| getPresent | 1 M | 0.05 | 0.09 | 0.50x |
| iterateEntries | 10 K | 37.4 | 62.8 | 0.60x |
| iterateEntries | 100 K | 2.5 | 3.0 | 0.83x |
| iterateEntries | 1 M | 0.19 | 0.14 | 1.33x |
| keySetIterate | 10 K | 39.2 | 59.8 | 0.66x |
| keySetIterate | 100 K | 2.6 | 3.3 | 0.78x |
| keySetIterate | 1 M | 0.18 | 0.15 | 1.18x |
| put | 10 K | 32.2 | 30.6 | 1.05x |
| put | 100 K | 25.6 | 26.8 | 0.96x |
| put | 1 M | 16.3 | 25.5 | 0.64x |
| remove | 10 K | 146.0 | 126.9 | 1.15x |
| remove | 100 K | 8.0 | 8.5 | 0.93x |
| remove | 1 M | 0.12 | 0.18 | 0.68x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~35-61% of HashMap throughput on reads. It's closest to HashMap on put at 10K (1.05x) and 100K (0.96x), and on remove at 10K (1.15x), but drops to 0.6-0.9x at larger sizes. The main advantage is simplicity (no Node objects) -- HashMap's `Node[]` + bin trees are heavily optimized by HotSpot.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| List of int values | `IntList` | 2.1x average speedup; 2.2x faster get, 6.9-7.5x faster setInt, 3.8-7.7x faster iterate |
| Bitset for non-negative integers | `IntBitSet` | 4.1x average speedup; up to 27x for containsAll, 27x for iteration |
| Set of int values | `IntSet` | 1.4x average; 2.0-2.8x faster for writes, 2.1-5.2x for iteration |
| Map of int-to-int | `IntToIntMap` | 3.0x average; 3.7-8.7x faster for writes at all sizes |
| Set of objects (iterative/bulk workloads) | `ObjectSet` | 1.0x average; 2.2-2.4x faster writes at small/medium size, 2.6-4.5x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | 1.3x average; 2.1-2.6x faster writes; near parity on iteration at large sizes |
| Generic map (small data) | `ObjectMap` | Within 5% of HashMap on put at 10K; simpler allocation model |

### Design Trade-offs

- **Primitive classes win on throughput** -- zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** -- walking a compact bit array is ~27x faster than iterating `HashSet` Node objects. `containsAll` is up to 27x faster.
- **`IntToIntMap` put is exceptional** -- 3.7-8.7x faster than HashMap, the largest speedup among all map operations, because both key and value avoid boxing.
- **`IntList` is consistently fast** -- 2.2x faster get (even boxed), 6.9-7.5x faster setInt, and 3.8-7.7x faster primitive iteration. The contiguous `int[]` layout gives superior cache performance over `Object[]`.
- **Object classes trade speed for simplicity** -- `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **ObjectSet lookups are slower** -- `containsAbsent` and `containsPresent` are 33-86% of HashSet speed, making it less suitable for read-heavy workloads.
- **Entry/keySet iteration views add overhead** at small sizes across all map types, but native iteration and large-size iteration are often competitive or faster.
