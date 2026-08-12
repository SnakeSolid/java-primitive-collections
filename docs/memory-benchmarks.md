# Memory Footprint Benchmarks

JOL-based retained memory analysis comparing each `primitive-collections` class against its `java.util` equivalent.

## Configuration

| Parameter | Value |
|-----------|-------|
| JOL version | From `memory` module dependencies |
| JDK | 21+ |
| Tool | `GraphLayout.parseInstance()` — measures *retained* size (all bytes reachable from the collection, including elements) |
| Element counts | 10, 100, 1 000, 10 000 |
| Keys | Integers 0..N-1 (for int-keyed), `"key-0"`..`"key-(N-1)"` (for object-keyed) |
| Metric | **Per-element** (retained bytes / element count; lower is better) |

**Retained size** includes the collection structure AND all elements stored inside it. A lower per-element value means the collection uses less memory per entry.

## Quick Reference — 10 000 elements

| Class | Java Reference | Per-element (bytes) | Savings |
|-------|---------------|---------------------|---------|
| `IntBitSet` | `HashSet<Integer>` | 0.13 | **99.8%** less |
| `IntSet` | `HashSet<Integer>` | 0.42 | **99.2%** less |
| `ObjectSet<Integer>` | `HashSet<Integer>` | 22.56 | **58.7%** less |
| `ObjectSet<String>` | `HashSet<String>` | 54.56 | **36.9%** less |
| `IntToIntMap` | `HashMap<Integer, Integer>` | 13.32 | **81.1%** less |
| `IntToObjectMap<String>` | `HashMap<Integer, String>` | 61.11 | **40.4%** less |
| `ObjectToIntMap<String>` | `HashMap<String, Integer>` | 61.11 | **40.4%** less |
| `ObjectMap<String, String>` | `HashMap<String, String>` | 109.11 | **18.9%** less |

## Set Memory Benchmarks

### Int Sets (int keys)

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `IntBitSet` | 4.80 | 0.56 | 0.17 | **0.13** |
| `IntSet` | 19.20 | 1.92 | 0.58 | **0.42** |
| `ObjectSet<Integer>` | 26.40 | 26.64 | 24.23 | **22.56** |
| `HashSet<Integer>` | 64.00 | 59.20 | 56.29 | **54.56** |

**Analysis:** `IntBitSet` is the clear winner — at 0.13 bytes/element for 10K entries, it stores 32 values per `int` with no indirection. `IntSet` is also excellent at 0.42 bytes/element thanks to its 27+5 bit-packing scheme (up to 32 elements per hash slot). `ObjectSet<Integer>` uses 58.7% less memory than `HashSet<Integer>` (22.56 vs 54.56 bytes/element), mainly because it avoids JDK's `Node<K,V>` entry objects and uses flat arrays.

### String Sets

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `ObjectSet<String>` | 58.40 | 58.64 | 56.23 | **54.56** |
| `HashSet<String>` | 96.00 | 91.20 | 88.29 | **86.56** |

**Analysis:** `ObjectSet<String>` uses 36.9% less memory than `HashSet<String>` at 10K (54.56 vs 86.56 bytes/element). The savings come from a simpler internal structure — flat `Object[]` + `IntBitSet` occupancy instead of `Node<K,V>` entries with next pointers.

## Map Memory Benchmarks

### int → int

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `IntToIntMap` | 24.00 | 21.84 | 16.74 | **13.32** |
| `HashMap<Integer, Integer>` | 60.80 | 58.88 | 70.21 | **70.36** |

**Analysis:** `IntToIntMap` uses 81.1% less memory (13.32 vs 70.36 bytes/element). The savings come from:
- No `Integer` boxing for keys or values (saves ~20 bytes per entry for boxed objects)
- No `Node<K,V>` wrapper objects
- Parallel `int[]` arrays instead of linked-entry chains

### int → String

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `IntToObjectMap<String>` | 68.00 | 69.20 | 64.46 | **61.11** |
| `HashMap<Integer, String>` | 108.80 | 106.88 | 104.26 | **102.56** |

**Analysis:** `IntToObjectMap` saves 40.4% (61.11 vs 102.56 bytes/element). The primary saving is avoiding boxing the `int` keys. String values are shared references so they don't contribute to differential memory.

### String → int

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `ObjectToIntMap<String>` | 66.40 | 69.04 | 64.44 | **61.11** |
| `HashMap<String, Integer>` | 108.80 | 106.88 | 104.26 | **102.56** |

**Analysis:** `ObjectToIntMap` saves 40.4% (61.11 vs 102.56 bytes/element). Savings come from avoiding boxing `int` values and using simpler internal structure (no `Node<K,V>`).

### String → String

| Collection | 10 | 100 | 1 000 | 10 000 |
|---|---|---|---|---|
| `ObjectMap<String, String>` | 114.40 | 117.04 | 112.44 | **109.11** |
| `HashMap<String, String>` | 140.80 | 138.88 | 136.26 | **134.56** |

**Analysis:** `ObjectMap` saves 18.9% (109.11 vs 134.56 bytes/element). With both keys and values being objects, the savings are modest compared to primitive-keyed maps, but still come from a simpler allocation model (flat arrays instead of `Node<K,V>` entries).

## Scale Behavior

### Observations by scale

| Collection | Trend (10→10K) | Notes |
|---|---|---|
| `IntBitSet` | **Improves** (4.80→0.13) | Amortized overhead — the backing `int[]` grows proportionally to capacity, not element count, but the per-element cost drops as more bits are packed |
| `IntSet` | **Improves** (19.20→0.42) | Fixed overhead of initial arrays is amortized; bit-packing efficiency increases with more elements per slot |
| `ObjectSet<Integer>` | **Improves** (26.40→22.56) | Initial array overhead amortizes; at 100 elements the collection may have already resized once, adding overhead |
| `HashSet<Integer>` | **Improves** (64.00→54.56) | JDK HashMap overhead amortizes similarly |
| `IntToIntMap` | **Improves** (24.00→13.32) | Initial small capacity arrays have high per-element overhead that amortizes |
| `ObjectMap<String, String>` | **Mixed** (114.40→109.11) | Modest improvement — object references are constant size regardless of count |

### Small vs Large collections

For **small collections (10-100 elements)**, all primitive collections have higher per-element overhead due to fixed minimum array sizes. `IntBitSet` and `IntSet` still win by large margins at small sizes because their absolute memory footprint is tiny (48 bytes and 192 bytes respectively for 10 elements).

For **large collections (10 000+ elements)**, the advantages of primitive storage are maximized — `IntBitSet` reaches sub-byte per-element storage, and `IntToIntMap` uses 5x less memory than `HashMap<Integer, Integer>`.

## Running Memory Analysis

```bash
java -jar memory/target/primitive-memory-0.0.1-SNAPSHOT-standalone.jar
```

The tool uses JOL's `GraphLayout.parseInstance()` which may require JVM instrumentation. If you see a warning about Instrumentation, the tool will attempt dynamic attach. For consistent results, add `-javaagent` with the JOL agent JAR or set `-Djdk.attach.allowAttachSelf=true`.
