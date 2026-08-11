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
| `IntBitSet` | `HashSet<Integer>` | **5.13x** | Most operations (especially iterate, containsAll) |
| `IntSet` | `HashSet<Integer>` | **3.05x** | add, iterate, containsAll |
| `ObjectSet` | `HashSet<String>` | **1.24x** | add (large), iterate, containsAll |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **2.03x** | put (2.0-7.9x), getAbsent (large), remove (large) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **1.23x** | put (2.3-2.6x), remove (small) |
| `ObjectMap` | `HashMap<String, String>` | **0.60x** | put (small data, ~0.9x) |

## Set Benchmarks

### IntBitSet vs HashSet\<Integer\>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 156.2 | 48.7 | 3.21x |
| add | 100 K | 138.4 | 49.3 | 2.81x |
| add | 1 M | 131.1 | 44.9 | 2.92x |
| containsAbsent | 10 K | 221.7 | 235.7 | 0.94x |
| containsAbsent | 100 K | 19.7 | 18.2 | 1.08x |
| containsAbsent | 1 M | 1.5 | 1.5 | 1.01x |
| containsAll | 10 K | 166.2 | 13.3 | 12.47x |
| containsAll | 100 K | 16.3 | 1.6 | 10.17x |
| containsAll | 1 M | 1.6 | 0.1 | 23.27x |
| containsPresent | 10 K | 96.8 | 90.5 | 1.07x |
| containsPresent | 100 K | 7.9 | 4.5 | 1.77x |
| containsPresent | 1 M | 0.8 | 0.1 | 7.48x |
| iterate | 10 K | 212.1 | 17.1 | 12.39x |
| iterate | 100 K | 15.8 | 1.9 | 8.38x |
| iterate | 1 M | 2.1 | 0.1 | 22.60x |
| iterateRemoveAll | 10 K | 84.7 | 7.8 | 10.87x |
| iterateRemoveAll | 100 K | 9.1 | 0.8 | 12.06x |
| iterateRemoveAll | 1 M | 0.8 | 0.1 | 10.12x |
| remove | 10 K | 55.2 | 39.4 | 1.40x |
| remove | 100 K | 7.8 | 6.5 | 1.19x |
| remove | 1 M | 0.7 | 0.5 | 1.53x |

**Analysis:** `IntBitSet` dominates for iteration (8-23x faster) because it walks a compact `int[]` with no object overhead. Add operations are ~2.8-3.2x faster. `containsAll` shows dramatic improvement (10-23x). `containsAbsent` is near parity across all capacities (0.94-1.08x). `containsPresent` is competitive — 1.1-7.5x faster.

### IntSet vs HashSet\<Integer\>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 153.6 | 48.7 | 3.15x |
| add | 100 K | 134.0 | 49.3 | 2.72x |
| add | 1 M | 76.3 | 44.9 | 1.70x |
| containsAbsent | 10 K | 168.1 | 235.7 | 0.71x |
| containsAbsent | 100 K | 13.7 | 18.2 | 0.75x |
| containsAbsent | 1 M | 0.7 | 1.5 | 0.46x |
| containsAll | 10 K | 59.1 | 13.3 | 4.42x |
| containsAll | 100 K | 3.6 | 1.6 | 2.24x |
| containsAll | 1 M | 0.3 | 0.1 | 4.73x |
| containsPresent | 10 K | 64.6 | 90.5 | 0.71x |
| containsPresent | 100 K | 3.7 | 4.5 | 0.82x |
| containsPresent | 1 M | 0.1 | 0.1 | 1.34x |
| iterate | 10 K | 83.3 | 17.1 | 4.86x |
| iterate | 100 K | 4.1 | 1.9 | 2.16x |
| iterate | 1 M | 0.4 | 0.1 | 4.37x |
| remove | 10 K | 77.8 | 39.4 | 1.97x |
| remove | 100 K | 4.4 | 6.5 | 0.69x |
| remove | 1 M | 0.5 | 0.5 | 0.98x |

**Analysis:** `IntSet` wins strongly on add (1.7-3.2x) and iteration (2.2-4.9x). `containsAll` is 2.2-4.7x faster. `containsAbsent` and `containsPresent` are the weaker areas — 46-82% of HashSet speed at larger capacities, likely because HashSet's lookup path is heavily optimized. Remove is near parity at large sizes but 2x faster at 10K.

### ObjectSet vs HashSet\<String\>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 118.4 | 54.5 | 2.17x |
| add | 100 K | 82.2 | 57.2 | 1.44x |
| add | 1 M | 35.0 | 57.0 | 0.61x |
| containsAbsent | 10 K | 110.9 | 177.5 | 0.62x |
| containsAbsent | 100 K | 5.3 | 14.0 | 0.38x |
| containsAbsent | 1 M | 0.1 | 0.4 | 0.30x |
| containsAll | 10 K | 35.2 | 13.1 | 2.69x |
| containsAll | 100 K | 2.8 | 1.4 | 2.00x |
| containsAll | 1 M | 0.1 | 0.1 | 1.28x |
| containsPresent | 10 K | 72.1 | 129.3 | 0.56x |
| containsPresent | 100 K | 3.4 | 7.1 | 0.47x |
| containsPresent | 1 M | 0.1 | 0.1 | 0.42x |
| iterate | 10 K | 60.6 | 17.2 | 3.52x |
| iterate | 100 K | 4.1 | 1.7 | 2.39x |
| iterate | 1 M | 0.2 | 0.1 | 2.33x |
| remove | 10 K | 53.7 | 94.6 | 0.57x |
| remove | 100 K | 3.4 | 6.0 | 0.57x |
| remove | 1 M | 0.1 | 0.2 | 0.83x |

**Analysis:** `ObjectSet` wins on add at small/medium sizes (1.4-2.2x) and on iteration (2.4-3.5x) due to simpler array layout. `containsAll` is 1.3-2.7x faster. However, `containsAbsent`, `containsPresent`, and `remove` are slower (30-62% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison.

## Map Benchmarks

### IntToIntMap vs HashMap\<Integer, Integer\>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 451.4 | 209.1 | 2.16x |
| getAbsent | 100 K | 44.7 | 14.2 | 3.14x |
| getAbsent | 1 M | 1.3 | 0.1 | 12.03x |
| getPresent | 10 K | 162.8 | 50.7 | 3.21x |
| getPresent | 100 K | 7.9 | 3.9 | 2.02x |
| getPresent | 1 M | 0.2 | 0.1 | 3.74x |
| iterate | 10 K | 171.9 | 66.4 | 2.59x |
| iterate | 100 K | 8.1 | 3.7 | 2.20x |
| iterate | 1 M | 0.2 | 0.3 | 0.91x |
| iterateEntries | 10 K | 29.8 | 66.4 | 0.45x |
| iterateEntries | 100 K | 2.5 | 3.7 | 0.67x |
| iterateEntries | 1 M | 0.3 | 0.3 | 1.12x |
| keySetIterate | 10 K | 31.9 | 67.7 | 0.47x |
| keySetIterate | 100 K | 2.6 | 3.8 | 0.68x |
| keySetIterate | 1 M | 0.3 | 0.1 | 4.15x |
| put | 10 K | 171.9 | 32.2 | 5.34x |
| put | 100 K | 335.9 | 42.9 | 7.84x |
| put | 1 M | 149.3 | 20.8 | 7.19x |
| remove | 10 K | 76.6 | 133.9 | 0.57x |
| remove | 100 K | 11.0 | 8.1 | 1.35x |
| remove | 1 M | 0.9 | 0.9 | 1.04x |

**Analysis:** `IntToIntMap` is the strongest map performer — put is 5.3-7.8x faster (eliminating Integer boxing for both key and value). `getAbsent` is 2.2-12.0x faster, dominating at all sizes this run. `getPresent` is 2.0-3.7x faster. Native iteration is 0.9-2.6x faster. Entry iteration is slower at small sizes (0.5-0.7x) due to view-object overhead, but near parity or faster at 1M. keySetIterate is 0.5-0.7x at small/medium sizes but flips to 4.2x at 1M. Remove is slower at 10K (0.57x) but 1.0-1.4x at larger sizes.

### ObjectToIntMap vs HashMap\<String, Integer\>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 162.6 | 352.0 | 0.46x |
| getAbsent | 100 K | 8.1 | 16.9 | 0.48x |
| getAbsent | 1 M | 0.3 | 0.5 | 0.54x |
| getPresent | 10 K | 67.0 | 115.9 | 0.58x |
| getPresent | 100 K | 3.6 | 7.5 | 0.48x |
| getPresent | 1 M | 0.1 | 0.1 | 0.63x |
| iterateEntries | 10 K | 39.6 | 68.3 | 0.58x |
| iterateEntries | 100 K | 2.9 | 3.7 | 0.77x |
| iterateEntries | 1 M | 0.3 | 0.2 | 1.69x |
| keySetIterate | 10 K | 22.3 | 31.2 | 0.72x |
| keySetIterate | 100 K | 2.4 | 2.3 | 1.03x |
| keySetIterate | 1 M | 0.2 | 0.1 | 1.80x |
| put | 10 K | 140.9 | 55.6 | 2.54x |
| put | 100 K | 131.3 | 51.3 | 2.56x |
| put | 1 M | 118.8 | 49.1 | 2.42x |
| remove | 10 K | 180.3 | 124.2 | 1.45x |
| remove | 100 K | 8.1 | 8.6 | 0.94x |
| remove | 1 M | 0.1 | 0.2 | 0.51x |

**Analysis:** `ObjectToIntMap` wins on writes (put consistently 2.4-2.6x faster) because it avoids boxing values. Remove is 1.5x faster at 10K but drops to 0.5-0.9x at larger sizes. Reads are ~46-58% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is 0.6-0.8x at small/medium sizes but flips to 1.7x at 1M. keySetIterate is near parity at 100K (1.0x) and 1.8x at 1M.

### ObjectMap vs HashMap\<String, String\>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 113.1 | 238.5 | 0.47x |
| getAbsent | 100 K | 5.7 | 15.3 | 0.37x |
| getAbsent | 1 M | 0.1 | 0.5 | 0.31x |
| getPresent | 10 K | 66.1 | 117.6 | 0.56x |
| getPresent | 100 K | 3.2 | 6.3 | 0.50x |
| getPresent | 1 M | 0.0 | 0.1 | 0.40x |
| iterateEntries | 10 K | 40.9 | 70.6 | 0.58x |
| iterateEntries | 100 K | 2.6 | 3.4 | 0.76x |
| iterateEntries | 1 M | 0.2 | 0.2 | 0.84x |
| keySetIterate | 10 K | 39.4 | 67.6 | 0.58x |
| keySetIterate | 100 K | 2.8 | 3.5 | 0.78x |
| keySetIterate | 1 M | 0.1 | 0.2 | 0.70x |
| put | 10 K | 34.2 | 31.7 | 1.08x |
| put | 100 K | 26.5 | 28.5 | 0.93x |
| put | 1 M | 17.3 | 27.2 | 0.64x |
| remove | 10 K | 142.9 | 139.5 | 1.02x |
| remove | 100 K | 4.1 | 8.7 | 0.47x |
| remove | 1 M | 0.1 | 0.2 | 0.54x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~31-58% of HashMap throughput on reads. It's closest to HashMap on put at 10K (1.1x) and remove at 10K (1.0x), but drops to 0.5-0.8x at larger sizes. The main advantage is simplicity (no Node objects) — HashMap's `Node[]` + bin trees are heavily optimized by HotSpot.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| Bitset for non-negative integers | `IntBitSet` | 5.1x average speedup; up to 23x for containsAll, 22x for iteration |
| Set of int values | `IntSet` | 3.0x average; 1.7-3.2x faster for writes, 2.2-4.9x for iteration |
| Map of int-to-int | `IntToIntMap` | 2.0x average; 5.3-7.8x faster for writes at all sizes |
| Set of objects (iterative/bulk workloads) | `ObjectSet` | 1.2x average; 1.4-2.2x faster writes at small/medium size, 2.4-3.5x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | 1.2x average; 2.4-2.6x faster writes; near parity on iteration at large sizes |
| Generic map (small data) | `ObjectMap` | Within 8% of HashMap on put at 10K; simpler allocation model |

### Design Trade-offs

- **Primitive classes win on throughput** — zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** — walking a compact bit array is ~23x faster than iterating `HashSet` Node objects. `containsAll` is up to 23x faster.
- **`IntToIntMap` put is exceptional** — 5.3-7.8x faster than HashMap, the largest speedup among all map operations, because both key and value avoid boxing.
- **Object classes trade speed for simplicity** — `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **ObjectSet lookups are slower** — `containsAbsent` and `containsPresent` are 30-62% of HashSet speed, making it less suitable for read-heavy workloads.
- **Entry/keySet iteration views add overhead** at small sizes across all map types, but native iteration and large-size iteration are often competitive or faster.
