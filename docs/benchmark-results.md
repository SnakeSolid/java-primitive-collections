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
| `IntList` | `ArrayList<Integer>` | **2.2x** | get (2.1-4.1x), contains (3.8-5.4x), iterate (3.5-7.7x), setInt (6.5-7.3x), iterateRemoveAll (1.1x) |
| `IntBitSet` | `HashSet<Integer>` | **4.4x** | iterate (13-30x), containsAll (12-32x), iterateRemoveAll (13-17x) |
| `IntSet` | `HashSet<Integer>` | **1.4x** | add (2.5-3.2x), containsAll (2.4-6.3x), iterate (2.2-5.1x) |
| `ObjectSet` | `HashSet<String>` | **1.0x** | add (small, 2.1x), iterate (2.1-3.4x), containsAll (1.2-2.7x) |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **3.3x** | put (5.0-8.0x), getAbsent (2.2-34.1x), getPresent (2.1-3.2x), iterate (2.1-3.1x) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **1.2x** | put (2.1-2.6x), remove (small, 1.4x) |
| `ObjectMap` | `HashMap<String, String>` | **0.7x** | put (10K, ~1.0x), remove (10K, ~1.1x) |

## List Benchmarks

### IntList vs ArrayList<Integer>

| Operation | Capacity | IntList | ArrayList | Ratio |
|---|---|---|---|---|
| contains | 10 K | 661.1 | 123.7 | 5.35x |
| contains | 100 K | 66.1 | 16.8 | 3.94x |
| contains | 1 M | 3.8 | 1.0 | 3.82x |
| get | 10 K | 1744.1 | 813.3 | 2.14x |
| get | 100 K | 1674.5 | 590.9 | 2.83x |
| get | 1 M | 1681.6 | 406.5 | 4.14x |
| getBoxed | 10 K | 1751.3 | 813.3 | 2.15x |
| getBoxed | 100 K | 1694.8 | 590.9 | 2.87x |
| getBoxed | 1 M | 1674.8 | 406.5 | 4.12x |
| iterate | 10 K | 753.6 | 216.7 | 3.48x |
| iterate | 100 K | 78.5 | 19.9 | 3.95x |
| iterate | 1 M | 7.8 | 1.0 | 7.66x |
| iterateBoxed | 10 K | 345.8 | 216.7 | 1.60x |
| iterateBoxed | 100 K | 34.4 | 19.9 | 1.73x |
| iterateBoxed | 1 M | 3.4 | 1.0 | 3.37x |
| iterateRemoveAll | 10 K | 202952.0 | 185798.7 | 1.09x |
| iterateRemoveAll | 100 K | 152223.3 | 139410.0 | 1.09x |
| iterateRemoveAll | 1 M | 152921.3 | 138204.8 | 1.11x |
| setInt | 10 K | 1104.9 | 151.7 | 7.28x |
| setInt | 100 K | 977.9 | 151.7 | 6.45x |
| setInt | 1 M | 978.5 | 145.0 | 6.75x |

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

**Analysis:** `IntList` dominates across nearly all operations. Primitive `get` is ~2.1-4.1x faster than `ArrayList<Integer>`, scaling with capacity, and surprisingly `getBoxed` is also ~2.1-4.1x faster -- the contiguous array layout likely gives better cache performance than `ArrayList`'s `Object[]`. `setInt` is the standout at 6.5-7.3x faster. `contains` (linear scan) is 3.8-5.4x faster. Primitive `iterate` is 3.5-7.7x faster, scaling with capacity. Boxed `iterateBoxed` is 1.6-3.4x faster. Even `iterateRemoveAll` is 1.1x faster, showing the benefit of simpler internal structure.

## Set Benchmarks

### IntBitSet vs HashSet<Integer>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 167.9 | 50.7 | 3.31x |
| add | 100 K | 176.5 | 52.5 | 3.36x |
| add | 1 M | 170.0 | 47.2 | 3.60x |
| containsAbsent | 10 K | 220.9 | 236.8 | 0.93x |
| containsAbsent | 100 K | 18.3 | 20.0 | 0.91x |
| containsAbsent | 1 M | 1.5 | 1.5 | 0.97x |
| containsAll | 10 K | 182.5 | 13.1 | 13.90x |
| containsAll | 100 K | 18.9 | 1.5 | 12.34x |
| containsAll | 1 M | 1.9 | 0.06 | 32.38x |
| containsPresent | 10 K | 97.9 | 73.5 | 1.33x |
| containsPresent | 100 K | 8.8 | 4.4 | 2.01x |
| containsPresent | 1 M | 0.7 | 0.09 | 7.55x |
| iterate | 10 K | 245.7 | 17.4 | 14.14x |
| iterate | 100 K | 24.6 | 1.9 | 13.27x |
| iterate | 1 M | 2.5 | 0.08 | 30.35x |
| iterateRemoveAll | 10 K | 90.7 | 7.0 | 12.89x |
| iterateRemoveAll | 100 K | 10.8 | 0.6 | 16.88x |
| iterateRemoveAll | 1 M | 1.0 | 0.07 | 14.03x |
| remove | 10 K | 60.8 | 30.5 | 2.00x |
| remove | 100 K | 8.4 | 5.3 | 1.58x |
| remove | 1 M | 0.8 | 0.4 | 1.99x |

**Analysis:** `IntBitSet` dominates for iteration (13-30x faster) because it walks a compact bit array with no object overhead. `containsAll` shows dramatic improvement (12.3-32.4x). Add operations are ~3.3-3.6x faster. `containsAbsent` is near parity across all capacities (0.91-0.97x). `containsPresent` is competitive at 10K (1.33x) and becomes much faster at larger sizes (2.0x at 100K, 7.6x at 1M). `iterateRemoveAll` is 12.9-16.9x faster. Remove is 1.6-2.0x faster, improving with capacity.

### IntSet vs HashSet<Integer>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 153.5 | 50.7 | 3.03x |
| add | 100 K | 132.6 | 52.5 | 2.53x |
| add | 1 M | 148.9 | 47.2 | 3.16x |
| containsAbsent | 10 K | 179.7 | 236.8 | 0.76x |
| containsAbsent | 100 K | 13.9 | 20.0 | 0.70x |
| containsAbsent | 1 M | 1.0 | 1.5 | 0.64x |
| containsAll | 10 K | 59.8 | 13.1 | 4.55x |
| containsAll | 100 K | 3.6 | 1.5 | 2.41x |
| containsAll | 1 M | 0.4 | 0.06 | 6.33x |
| containsPresent | 10 K | 62.6 | 73.5 | 0.85x |
| containsPresent | 100 K | 4.4 | 4.4 | 0.99x |
| containsPresent | 1 M | 0.1 | 0.09 | 1.16x |
| iterate | 10 K | 86.3 | 17.4 | 4.95x |
| iterate | 100 K | 4.2 | 1.9 | 2.20x |
| iterate | 1 M | 0.4 | 0.08 | 5.13x |
| remove | 10 K | 71.4 | 30.5 | 2.34x |
| remove | 100 K | 5.9 | 5.3 | 1.10x |
| remove | 1 M | 0.5 | 0.4 | 1.31x |

**Analysis:** `IntSet` wins on add (2.5-3.2x) and iteration (2.2-5.1x). `containsAll` is 2.4-6.3x faster. `containsAbsent` and `containsPresent` are the weaker areas -- 64-85% of HashSet speed, likely because HashSet's lookup path is heavily optimized. Remove is 2.3x faster at 10K and 1.1-1.3x at larger sizes.

### ObjectSet vs HashSet<String>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 107.3 | 51.5 | 2.08x |
| add | 100 K | 63.7 | 49.2 | 1.29x |
| add | 1 M | 26.6 | 46.5 | 0.57x |
| containsAbsent | 10 K | 113.7 | 163.8 | 0.69x |
| containsAbsent | 100 K | 5.0 | 12.5 | 0.40x |
| containsAbsent | 1 M | 0.1 | 0.3 | 0.26x |
| containsAll | 10 K | 31.6 | 11.7 | 2.70x |
| containsAll | 100 K | 2.3 | 1.0 | 2.25x |
| containsAll | 1 M | 0.06 | 0.05 | 1.17x |
| containsPresent | 10 K | 61.1 | 102.7 | 0.60x |
| containsPresent | 100 K | 3.0 | 6.4 | 0.47x |
| containsPresent | 1 M | 0.05 | 0.1 | 0.46x |
| iterate | 10 K | 54.4 | 16.2 | 3.36x |
| iterate | 100 K | 3.4 | 1.4 | 2.46x |
| iterate | 1 M | 0.1 | 0.07 | 2.05x |
| remove | 10 K | 50.6 | 59.1 | 0.86x |
| remove | 100 K | 6.3 | 5.4 | 1.17x |
| remove | 1 M | 0.1 | 0.2 | 0.77x |

**Analysis:** `ObjectSet` wins on add at small sizes (2.1x) and on iteration (2.1-3.4x) due to simpler array layout. `containsAll` is 1.2-2.7x faster. However, `containsAbsent`, `containsPresent`, and `remove` (at larger sizes) are slower (26-86% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison.

## Map Benchmarks

### IntToIntMap vs HashMap<Integer, Integer>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 445.4 | 207.3 | 2.15x |
| getAbsent | 100 K | 45.1 | 13.4 | 3.36x |
| getAbsent | 1 M | 3.8 | 0.1 | 34.14x |
| getPresent | 10 K | 175.1 | 54.4 | 3.22x |
| getPresent | 100 K | 8.5 | 4.0 | 2.12x |
| getPresent | 1 M | 0.2 | 0.06 | 3.92x |
| iterate | 10 K | 170.6 | 70.9 | 2.41x |
| iterate | 100 K | 7.7 | 3.7 | 2.10x |
| iterate | 1 M | 0.2 | 0.07 | 3.07x |
| iterateEntries | 10 K | 32.0 | 70.9 | 0.45x |
| iterateEntries | 100 K | 2.6 | 3.7 | 0.69x |
| iterateEntries | 1 M | 0.3 | 0.07 | 3.88x |
| keySetIterate | 10 K | 31.8 | 54.4 | 0.58x |
| keySetIterate | 100 K | 2.6 | 3.7 | 0.70x |
| keySetIterate | 1 M | 0.3 | 0.3 | 1.14x |
| put | 10 K | 170.3 | 34.2 | 4.98x |
| put | 100 K | 344.1 | 43.0 | 8.00x |
| put | 1 M | 158.4 | 20.8 | 7.62x |
| remove | 10 K | 81.8 | 150.5 | 0.54x |
| remove | 100 K | 10.6 | 7.8 | 1.35x |
| remove | 1 M | 1.1 | 0.9 | 1.26x |

**Analysis:** `IntToIntMap` is the strongest map performer -- put is 5.0-8.0x faster (eliminating Integer boxing for both key and value). `getAbsent` is 2.2-34.1x faster, dominating at all sizes especially at 1M. `getPresent` is 2.1-3.2x faster. Native iteration is 2.1-3.1x faster. Entry iteration is slower at small sizes (0.5-0.7x) due to view-object overhead, but 3.9x faster at 1M. keySetIterate is 0.6-0.7x at small/medium sizes and near parity (1.1x) at 1M. Remove is slower at 10K (0.54x) but 1.4x at 100K and 1.3x at 1M.

### ObjectToIntMap vs HashMap<String, Integer>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 141.9 | 288.1 | 0.49x |
| getAbsent | 100 K | 7.8 | 14.9 | 0.53x |
| getAbsent | 1 M | 0.2 | 0.4 | 0.49x |
| getPresent | 10 K | 56.2 | 96.7 | 0.58x |
| getPresent | 100 K | 3.3 | 7.0 | 0.48x |
| getPresent | 1 M | 0.07 | 0.1 | 0.62x |
| iterateEntries | 10 K | 37.0 | 62.1 | 0.60x |
| iterateEntries | 100 K | 2.9 | 3.4 | 0.86x |
| iterateEntries | 1 M | 0.3 | 0.2 | 1.49x |
| keySetIterate | 10 K | 22.1 | 26.9 | 0.82x |
| keySetIterate | 100 K | 2.1 | 2.0 | 1.06x |
| keySetIterate | 1 M | 0.1 | 0.09 | 1.52x |
| put | 10 K | 107.2 | 50.4 | 2.13x |
| put | 100 K | 98.5 | 39.1 | 2.52x |
| put | 1 M | 95.5 | 37.4 | 2.55x |
| remove | 10 K | 144.8 | 103.3 | 1.40x |
| remove | 100 K | 9.4 | 8.4 | 1.12x |
| remove | 1 M | 0.1 | 0.2 | 0.34x |

**Analysis:** `ObjectToIntMap` wins on writes -- put is 2.1-2.6x faster because it avoids boxing values. Remove is 1.4x faster at 10K and 1.1x at 100K but drops to 0.3x at 1M. Reads are ~48-58% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is 0.6-0.9x at small/medium sizes but flips to 1.5x at 1M. keySetIterate is near parity at small/medium sizes (0.8-1.1x) and 1.5x at 1M.

### ObjectMap vs HashMap<String, String>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 103.5 | 212.1 | 0.49x |
| getAbsent | 100 K | 5.2 | 13.7 | 0.38x |
| getAbsent | 1 M | 0.1 | 0.4 | 0.30x |
| getPresent | 10 K | 58.2 | 101.0 | 0.58x |
| getPresent | 100 K | 3.0 | 6.8 | 0.45x |
| getPresent | 1 M | 0.04 | 0.09 | 0.48x |
| iterateEntries | 10 K | 39.8 | 55.9 | 0.71x |
| iterateEntries | 100 K | 2.3 | 3.1 | 0.73x |
| iterateEntries | 1 M | 0.1 | 0.2 | 0.75x |
| keySetIterate | 10 K | 36.6 | 56.0 | 0.65x |
| keySetIterate | 100 K | 2.5 | 3.2 | 0.78x |
| keySetIterate | 1 M | 0.1 | 0.2 | 0.72x |
| put | 10 K | 28.6 | 29.3 | 0.98x |
| put | 100 K | 23.4 | 24.9 | 0.94x |
| put | 1 M | 16.1 | 25.2 | 0.64x |
| remove | 10 K | 123.7 | 116.5 | 1.06x |
| remove | 100 K | 8.2 | 8.1 | 1.02x |
| remove | 1 M | 0.2 | 0.2 | 0.76x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~30-58% of HashMap throughput on reads. It's closest to HashMap on put at 10K (0.98x) and 100K (0.94x), and on remove at 10K (1.1x), but drops to 0.6-0.8x at larger sizes. Iteration is 0.6-0.8x across all sizes. The main advantage is simplicity (no Node objects) -- HashMap's `Node[]` + bin trees are heavily optimized by HotSpot.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| List of int values | `IntList` | 2.2x average speedup; 2.1-4.1x faster get, 6.5-7.3x faster setInt, 3.5-7.7x faster iterate |
| Bitset for non-negative integers | `IntBitSet` | 4.4x average speedup; up to 32x for containsAll, 30x for iteration |
| Set of int values | `IntSet` | 1.4x average; 2.5-3.2x faster for writes, 2.2-5.1x for iteration |
| Map of int-to-int | `IntToIntMap` | 3.3x average; 5.0-8.0x faster for writes at all sizes |
| Set of objects (iterative/bulk workloads) | `ObjectSet` | 1.0x average; 2.1x faster writes at small size, 2.1-3.4x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | 1.2x average; 2.1-2.6x faster writes; near parity on iteration at large sizes |
| Generic map (small data) | `ObjectMap` | Within 6% of HashMap on put at 10K; simpler allocation model |

### Design Trade-offs

- **Primitive classes win on throughput** -- zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** -- walking a compact bit array is ~30x faster than iterating `HashSet` Node objects. `containsAll` is up to 32x faster.
- **`IntToIntMap` put is exceptional** -- 5.0-8.0x faster than HashMap, the largest speedup among all map operations, because both key and value avoid boxing.
- **`IntList` is consistently fast** -- 2.1-4.1x faster get (even boxed), 6.5-7.3x faster setInt, and 3.5-7.7x faster primitive iteration. The contiguous `int[]` layout gives superior cache performance over `Object[]`.
- **Object classes trade speed for simplicity** -- `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **ObjectSet lookups are slower** -- `containsAbsent` and `containsPresent` are 26-86% of HashSet speed, making it less suitable for read-heavy workloads.
- **Entry/keySet iteration views add overhead** at small sizes across all map types, but native iteration and large-size iteration are often competitive or faster.
