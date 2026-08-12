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
| `IntBitSet` | `HashSet<Integer>` | **4.3x** | iterate, containsAll, iterateRemoveAll |
| `IntSet` | `HashSet<Integer>` | **1.4x** | add, iterate, containsAll |
| `ObjectSet` | `HashSet<String>` | **1.0x** | add (small/medium), iterate, containsAll, remove (small) |
| `IntToIntMap` | `HashMap<Integer, Integer>` | **2.3x** | put (5.5-8.2x), getAbsent (1M), remove (100K+) |
| `ObjectToIntMap` | `HashMap<String, Integer>` | **0.9x** | put (1.1-2.5x), remove (10K) |
| `ObjectMap` | `HashMap<String, String>` | **0.7x** | put (10K, ~1.1x), remove (10K) |

## Set Benchmarks

### IntBitSet vs HashSet\<Integer\>

| Operation | Capacity | IntBitSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 159.6 | 62.4 | 2.56x |
| add | 100 K | 155.4 | 54.7 | 2.84x |
| add | 1 M | 141.3 | 55.7 | 2.54x |
| containsAbsent | 10 K | 228.2 | 272.5 | 0.84x |
| containsAbsent | 100 K | 20.1 | 20.5 | 0.98x |
| containsAbsent | 1 M | 1.7 | 1.6 | 1.02x |
| containsAll | 10 K | 174.0 | 13.5 | 12.91x |
| containsAll | 100 K | 17.0 | 1.6 | 10.41x |
| containsAll | 1 M | 1.7 | 0.1 | 22.00x |
| containsPresent | 10 K | 90.1 | 92.6 | 0.97x |
| containsPresent | 100 K | 8.5 | 4.6 | 1.85x |
| containsPresent | 1 M | 0.7 | 0.1 | 5.62x |
| iterate | 10 K | 225.1 | 18.8 | 11.98x |
| iterate | 100 K | 20.8 | 2.0 | 10.37x |
| iterate | 1 M | 2.1 | 0.1 | 21.26x |
| iterateRemoveAll | 10 K | 72.8 | 8.1 | 9.01x |
| iterateRemoveAll | 100 K | 9.6 | 0.8 | 12.32x |
| iterateRemoveAll | 1 M | 0.9 | 0.1 | 11.38x |
| remove | 10 K | 67.3 | 43.5 | 1.55x |
| remove | 100 K | 8.9 | 6.8 | 1.30x |
| remove | 1 M | 0.7 | 0.5 | 1.58x |

**Analysis:** `IntBitSet` dominates for iteration (10-21x faster) because it walks a compact bit array with no object overhead. Add operations are ~2.5-2.8x faster. `containsAll` shows dramatic improvement (10-22x). `containsAbsent` is near parity across all capacities (0.84-1.02x). `containsPresent` is competitive — 0.97x at 10K to 5.6x at 1M. `iterateRemoveAll` is 9-12x faster.

### IntSet vs HashSet\<Integer\>

| Operation | Capacity | IntSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 158.0 | 62.4 | 2.53x |
| add | 100 K | 145.8 | 54.7 | 2.67x |
| add | 1 M | 78.8 | 55.7 | 1.41x |
| containsAbsent | 10 K | 186.3 | 272.5 | 0.68x |
| containsAbsent | 100 K | 13.7 | 20.5 | 0.67x |
| containsAbsent | 1 M | 0.8 | 1.6 | 0.46x |
| containsAll | 10 K | 57.2 | 13.5 | 4.24x |
| containsAll | 100 K | 3.6 | 1.6 | 2.22x |
| containsAll | 1 M | 0.3 | 0.1 | 4.14x |
| containsPresent | 10 K | 67.5 | 92.6 | 0.73x |
| containsPresent | 100 K | 3.8 | 4.6 | 0.83x |
| containsPresent | 1 M | 0.2 | 0.1 | 1.35x |
| iterate | 10 K | 84.3 | 18.8 | 4.48x |
| iterate | 100 K | 4.1 | 2.0 | 2.05x |
| iterate | 1 M | 0.4 | 0.1 | 4.32x |
| remove | 10 K | 78.6 | 43.5 | 1.81x |
| remove | 100 K | 6.2 | 6.8 | 0.90x |
| remove | 1 M | 0.4 | 0.5 | 0.96x |

**Analysis:** `IntSet` wins on add (1.4-2.7x) and iteration (2.1-4.5x). `containsAll` is 2.2-4.2x faster. `containsAbsent` and `containsPresent` are the weaker areas — 46-73% of HashSet speed at larger capacities, likely because HashSet's lookup path is heavily optimized. Remove is 1.8x faster at 10K but near parity at larger sizes.

### ObjectSet vs HashSet\<String\>

| Operation | Capacity | ObjectSet | HashSet | Ratio |
|---|---|---|---|---|
| add | 10 K | 127.7 | 57.9 | 2.21x |
| add | 100 K | 87.7 | 56.7 | 1.55x |
| add | 1 M | 40.9 | 55.4 | 0.74x |
| containsAbsent | 10 K | 119.4 | 168.7 | 0.71x |
| containsAbsent | 100 K | 6.2 | 14.1 | 0.44x |
| containsAbsent | 1 M | 0.1 | 0.4 | 0.29x |
| containsAll | 10 K | 37.1 | 14.1 | 2.64x |
| containsAll | 100 K | 2.8 | 1.4 | 1.95x |
| containsAll | 1 M | 0.1 | 0.1 | 1.39x |
| containsPresent | 10 K | 78.3 | 130.2 | 0.60x |
| containsPresent | 100 K | 3.5 | 7.0 | 0.50x |
| containsPresent | 1 M | 0.1 | 0.1 | 0.48x |
| iterate | 10 K | 64.7 | 17.1 | 3.78x |
| iterate | 100 K | 4.1 | 1.8 | 2.32x |
| iterate | 1 M | 0.2 | 0.1 | 2.20x |
| remove | 10 K | 144.5 | 90.9 | 1.59x |
| remove | 100 K | 3.5 | 5.9 | 0.60x |
| remove | 1 M | 0.1 | 0.2 | 0.83x |

**Analysis:** `ObjectSet` wins on add at small/medium sizes (1.6-2.2x) and on iteration (2.2-3.8x) due to simpler array layout. `containsAll` is 1.4-2.6x faster. However, `containsAbsent`, `containsPresent`, and `remove` (at larger sizes) are slower (29-71% of HashSet speed), likely because `Objects.equals()` adds overhead compared to HashSet's internal key comparison.

## Map Benchmarks

### IntToIntMap vs HashMap\<Integer, Integer\>

| Operation | Capacity | IntToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 455.5 | 249.9 | 1.82x |
| getAbsent | 100 K | 48.0 | 15.0 | 3.20x |
| getAbsent | 1 M | 1.6 | 0.1 | 12.86x |
| getPresent | 10 K | 184.3 | 61.2 | 3.01x |
| getPresent | 100 K | 7.9 | 3.9 | 2.03x |
| getPresent | 1 M | 0.3 | 0.1 | 4.41x |
| iterate | 10 K | 186.0 | 69.8 | 2.67x |
| iterate | 100 K | 7.8 | 3.9 | 2.01x |
| iterate | 1 M | 0.2 | 0.1 | 2.61x |
| iterateEntries | 10 K | 30.2 | 69.8 | 0.43x |
| iterateEntries | 100 K | 2.6 | 3.9 | 0.67x |
| iterateEntries | 1 M | 0.3 | 0.1 | 3.23x |
| keySetIterate | 10 K | 32.5 | 70.8 | 0.46x |
| keySetIterate | 100 K | 2.7 | 3.7 | 0.72x |
| keySetIterate | 1 M | 0.3 | 0.1 | 2.96x |
| put | 10 K | 193.1 | 35.0 | 5.52x |
| put | 100 K | 387.1 | 47.5 | 8.15x |
| put | 1 M | 130.0 | 23.4 | 5.56x |
| remove | 10 K | 89.6 | 152.7 | 0.59x |
| remove | 100 K | 11.5 | 8.4 | 1.37x |
| remove | 1 M | 1.1 | 0.9 | 1.22x |

**Analysis:** `IntToIntMap` is the strongest map performer — put is 5.5-8.2x faster (eliminating Integer boxing for both key and value). `getAbsent` is 1.8-12.9x faster, dominating at all sizes. `getPresent` is 2.0-4.4x faster. Native iteration is 2.0-2.7x faster. Entry iteration is slower at small sizes (0.4-0.7x) due to view-object overhead, but 3.2x faster at 1M. keySetIterate is 0.5-0.7x at small/medium sizes but flips to 3.0x at 1M. Remove is slower at 10K (0.59x) but 1.2-1.4x at larger sizes.

### ObjectToIntMap vs HashMap\<String, Integer\>

| Operation | Capacity | ObjectToIntMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 172.2 | 349.1 | 0.49x |
| getAbsent | 100 K | 8.1 | 17.4 | 0.46x |
| getAbsent | 1 M | 0.2 | 0.5 | 0.53x |
| getPresent | 10 K | 69.4 | 118.4 | 0.59x |
| getPresent | 100 K | 3.5 | 7.7 | 0.46x |
| getPresent | 1 M | 0.1 | 0.1 | 0.59x |
| iterateEntries | 10 K | 38.9 | 71.6 | 0.54x |
| iterateEntries | 100 K | 3.1 | 3.8 | 0.82x |
| iterateEntries | 1 M | 0.3 | 0.2 | 1.59x |
| keySetIterate | 10 K | 24.9 | 31.7 | 0.78x |
| keySetIterate | 100 K | 2.3 | 2.8 | 0.84x |
| keySetIterate | 1 M | 0.2 | 0.1 | 1.41x |
| put | 10 K | 143.5 | 59.6 | 2.41x |
| put | 100 K | 137.9 | 55.0 | 2.51x |
| put | 1 M | 54.5 | 50.8 | 1.07x |
| remove | 10 K | 187.3 | 130.2 | 1.44x |
| remove | 100 K | 4.6 | 8.7 | 0.53x |
| remove | 1 M | 0.1 | 0.3 | 0.37x |

**Analysis:** `ObjectToIntMap` wins on writes — put is 1.1-2.5x faster because it avoids boxing values. Remove is 1.4x faster at 10K but drops to 0.4-0.5x at larger sizes. Reads are ~46-59% of HashMap speed due to `Objects.equals()` overhead and the IntBitSet occupancy bitset check on each probe step. Entry iteration is 0.5-0.8x at small/medium sizes but flips to 1.6x at 1M. keySetIterate is near parity at small/medium sizes (0.8-0.8x) and 1.4x at 1M.

### ObjectMap vs HashMap\<String, String\>

| Operation | Capacity | ObjectMap | HashMap | Ratio |
|---|---|---|---|---|
| getAbsent | 10 K | 149.0 | 255.8 | 0.58x |
| getAbsent | 100 K | 5.6 | 15.1 | 0.37x |
| getAbsent | 1 M | 0.2 | 0.5 | 0.32x |
| getPresent | 10 K | 69.6 | 124.1 | 0.56x |
| getPresent | 100 K | 3.3 | 7.0 | 0.46x |
| getPresent | 1 M | 0.1 | 0.1 | 0.49x |
| iterateEntries | 10 K | 40.5 | 70.1 | 0.58x |
| iterateEntries | 100 K | 2.8 | 3.6 | 0.79x |
| iterateEntries | 1 M | 0.2 | 0.2 | 0.98x |
| keySetIterate | 10 K | 41.8 | 67.1 | 0.62x |
| keySetIterate | 100 K | 2.8 | 3.5 | 0.79x |
| keySetIterate | 1 M | 0.2 | 0.2 | 1.10x |
| put | 10 K | 37.0 | 32.6 | 1.14x |
| put | 100 K | 25.7 | 28.8 | 0.89x |
| put | 1 M | 17.5 | 28.1 | 0.62x |
| remove | 10 K | 164.5 | 146.6 | 1.12x |
| remove | 100 K | 7.8 | 8.8 | 0.89x |
| remove | 1 M | 0.1 | 0.3 | 0.43x |

**Analysis:** `ObjectMap` is the weakest performer vs HashMap. With full object keys and values, it loses the boxing advantage entirely. It's ~32-58% of HashMap throughput on reads. It's closest to HashMap on put at 10K (1.1x) and remove at 10K (1.1x), but drops to 0.4-0.9x at larger sizes. The main advantage is simplicity (no Node objects) — HashMap's `Node[]` + bin trees are heavily optimized by HotSpot.

## Conclusions

### When to use primitive-collections

| Scenario | Recommended Class | Why |
|----------|-------------------|-----|
| Bitset for non-negative integers | `IntBitSet` | 4.3x average speedup; up to 22x for containsAll, 21x for iteration |
| Set of int values | `IntSet` | 1.4x average; 1.4-2.7x faster for writes, 2.1-4.5x for iteration |
| Map of int-to-int | `IntToIntMap` | 2.3x average; 5.5-8.2x faster for writes at all sizes |
| Set of objects (iterative/bulk workloads) | `ObjectSet` | 1.0x average; 1.6-2.2x faster writes at small/medium size, 2.2-3.8x faster iteration |
| Map with object keys, int values | `ObjectToIntMap` | 0.9x average; 1.1-2.5x faster writes; near parity on iteration at large sizes |
| Generic map (small data) | `ObjectMap` | Within 14% of HashMap on put at 10K; simpler allocation model |

### Design Trade-offs

- **Primitive classes win on throughput** — zero boxing eliminates GC pressure and indirect memory access.
- **`IntBitSet` iteration is the standout** — walking a compact bit array is ~21x faster than iterating `HashSet` Node objects. `containsAll` is up to 22x faster.
- **`IntToIntMap` put is exceptional** — 5.5-8.2x faster than HashMap, the largest speedup among all map operations, because both key and value avoid boxing.
- **Object classes trade speed for simplicity** — `ObjectSet` and `ObjectMap` use fewer object allocations (no `Node<K,V>` entries), but `Objects.equals()` and the `IntBitSet` occupancy tracker add per-probe overhead.
- **ObjectSet lookups are slower** — `containsAbsent` and `containsPresent` are 29-71% of HashSet speed, making it less suitable for read-heavy workloads.
- **Entry/keySet iteration views add overhead** at small sizes across all map types, but native iteration and large-size iteration are often competitive or faster.
