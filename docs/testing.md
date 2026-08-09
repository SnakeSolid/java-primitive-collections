# Testing Strategy

## Framework

JUnit Jupiter 5.10.2. Run with `./mvnw test` (executed in the `core` module).

## Test Inventory

| Class | Tests |
|-------|-------|
| `IntBitSetTest` | 42 |
| `IntSetTest` | 49 |
| `IntSetShiftTest` | 12 |
| `ObjectSetTest` | 42 |
| `IntToIntMapTest` | 86 |
| `IntToIntMapShiftTest` | 16 |
| `ObjectMapTest` | 92 |
| `ObjectMapShiftTest` | 15 |
| `ObjectToIntMapTest` | 97 |
| `ObjectToIntMapShiftTest` | 15 |
| **Total** | **466** |

## Coverage Goals

Each class should have tests for:

1. **Construction** — default, custom parameters, invalid input (negative capacity, zero capacity).
2. **Core operations** — every public method, including edge cases (zero keys, boundary bits, duplicate insertion).
3. **Interface contract** — behavior when used through `Set<Integer>`, `Map<Integer, Integer>`, or `Map<K, V>`.
4. **Guard clauses** — null arguments, non-`Integer` objects passed to boxed methods, out-of-range values.
5. **Collision / stress scenarios** — keys that force hash collisions, repeated resize cycles.
6. **View mutation** — structural modification through `keySet`, `values`, `entrySet` views (remove, contains, iterator).
7. **Bulk operations** — `addAll`, `removeAll`, `retainAll`, `containsAll` including no-op and empty-collection edge cases.
8. **Functional operations** — `forEach`, `replaceAll`, `computeIfAbsent`, `computeIfPresent`, `compute`, `merge` including null-return semantics and NPE on null arguments.
9. **Entry semantics** — `Map.Entry` equals/hashCode/toString, `setValue` mutates backing map, null value throws NPE.
10. **Equality** — `equals` identity (`this == o`), non-Set comparison, different-size comparison.
11. **Custom types** — for generic collections, verify that custom key/value types with proper `equals`/`hashCode` work correctly.

## IntSet-specific Tests

- Packing 32 elements with the same 27-bit prefix into a single slot.
- Removing individual elements from a packed slot without losing siblings.
- Removing all elements from a packed slot (slot is cleared, backward-shift runs).
- Backward-shift deletion — removing from the middle of a probe chain keeps siblings findable.
- Split key chains — multiple distinct probe chains overlapping in the same table region.
- `retainAll` triggering backward-shift for fully emptied slots.
- Table wrap-around during backward-shift.
- Negative values, `Integer.MIN_VALUE`, `Integer.MAX_VALUE`.

## Conventions

- Test class names: `{ClassName}Test`
- Test method names: descriptive `void` methods using `camelCase` (e.g., `putAndGetSingleEntry`).
- Group related tests with comment separators: `// ------------------------------------------------------------------`
- One assertion per test where possible; use `assertThrows` for expected exceptions.

## Benchmarks

JMH 1.37 is used for micro-benchmarks. Benchmark code lives in the `benchmarks` module.

All benchmarks use throughput mode (ops/ms), 25% fill factor, and capacities of 10 K, 100 K, 1 M.

### Benchmark Inventory

The benchmark suite is split into two groups:

**Project-collection benchmarks** — test the library's own classes (with Java collections still instantiated in the state for `containsAll` cross-type comparisons):

| Benchmark | Class Under Test |
|-----------|---------------|
| `IntBitSetBenchmark` | `IntBitSet` |
| `IntSetBenchmark` | `IntSet` |
| `ObjectSetBenchmark` | `ObjectSet<String>` |
| `IntToIntMapBenchmark` | `IntToIntMap` |
| `ObjectToIntMapBenchmark` | `ObjectToIntMap<String>` |
| `ObjectMapBenchmark` | `ObjectMap<String, String>` |

**Java-collections benchmarks** — standalone baselines for the JDK classes, extracted from the project benchmarks to avoid duplication:

| Benchmark | JDK Class |
|-----------|----------|
| `JavaIntSetBenchmark` | `HashSet<Integer>` / `Set<Integer>` |
| `JavaStringSetBenchmark` | `HashSet<String>` / `Set<String>` |
| `JavaIntMapBenchmark` | `HashMap<Integer, Integer>` / `Map<Integer, Integer>` |
| `JavaStringMapBenchmark` | `HashMap<String, String>` / `Map<String, String>` |
| `JavaObjectToIntMapBenchmark` | `HashMap<String, Integer>` |

All benchmarks share a common configuration inherited from `JMHConfig` (throughput mode, 25% fill factor, capacities of 10 K / 100 K / 1 M). Setup helpers are centralized in `BenchmarkDataHelper`.

### Operations Benchmarked per Class

**Set classes (`IntBitSet`, `IntSet`, `ObjectSet`):**

| Operation | Description |
|-----------|-------------|
| `containsPresent` | Key is present in the set |
| `containsAbsent` | Key is absent from the set |
| `add` | Insert unique elements (uses `ThreadState`) |
| `remove` | Remove present elements (uses `ThreadState`) |
| `iterate` | Iterate over all elements |
| `iterateRemoveAll` | Iterator with `remove()` — only benchmarked for `IntBitSet` and `HashSet`; `IntSet` and `ObjectSet` iterators throw `UnsupportedOperationException` |
| `containsAll` | Bulk containment check |

**Map classes (`IntToIntMap`, `ObjectToIntMap`, `ObjectMap`):**

| Operation | Description |
|-----------|-------------|
| `getPresent` | `get()` for a present key |
| `getAbsent` | `get()` for an absent key |
| `put` | Insert new key-value pairs (uses `ThreadState`) |
| `remove` | Remove present keys (uses `ThreadState`) |
| `iterate` | Iterate and sum values via `get()` (only `IntToIntMap`) |
| `iterateEntries` | Iterate over `entrySet()` |
| `keySetIterate` | Iterate over `keySet()` |

### Benchmark Results Summary

See [docs/benchmark-results.md](./docs/benchmark-results.md) for the full results table and analysis.

**Key findings:**

| Class | Avg Speedup vs Java | Best Operation | Worst Operation |
|-------|---------------------|----------------|-----------------|
| `IntBitSet` | **5.40x** | iterate (20.94x) | containsAll (0.21x) |
| `IntSet` | **1.87x** | iterate (5.53x) | containsAll (0.19x) |
| `ObjectSet` | **0.95x** | iterate (2.47x) | containsAbsent (0.35x) |
| `IntToIntMap` | **2.02x** | put (7.15x) | iterateEntries (0.34x) |
| `ObjectToIntMap` | **0.85x** | put (2.07x) | iterateEntries (0.28x) |
| `ObjectMap` | **0.55x** | put (0.92x) | iterateEntries (0.28x) |

**Primitive classes (`IntBitSet`, `IntSet`, `IntToIntMap`) consistently outperform their Java equivalents** thanks to zero boxing overhead. Generic object classes (`ObjectSet`, `ObjectMap`) trade some lookup speed for simpler architecture, though they match HashMap on write operations.

### Running Benchmarks

```bash
./mvnw package && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark
```

For a quick run (1 fork, 1 warmup, 1 measurement):

```bash
java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark -f 1 -wi 1 -i 1
```
