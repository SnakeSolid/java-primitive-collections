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

| Class | Description |
|-------|-------------|
| `IntBitSetBenchmark` | Compares `IntBitSet` vs `HashSet<Integer>` across capacities of 10 K, 100 K, 1 M (25% fill). |
| `IntSetBenchmark` | Compares `IntSet` vs `HashSet<Integer>` across capacities of 10 K, 100 K, 1 M (25% fill). Note: `IntSet` iterator does not support `remove()` — only the `HashSet` variant is benchmarked for that operation. |

**Operations benchmarked:**

| Operation | IntBitSet | HashSet |
|-----------|-----------|---------|
| `contains` (present) | `intBitSet_containsPresent` | `hashSet_containsPresent` |
| `contains` (absent) | `intBitSet_containsAbsent` | `hashSet_containsAbsent` |
| `add` (unique) | `intBitSet_add` | `hashSet_add` |
| `remove` (present) | `intBitSet_remove` | `hashSet_remove` |
| iterate | `intBitSet_iterate` | `hashSet_iterate` |
| iterate + remove | `intBitSet_iterateRemoveAll` | `hashSet_iterateRemoveAll` |
| `containsAll` | `intBitSet_containsAll` | `hashSet_containsAll` |

**Run benchmarks:**

```bash
./mvnw package && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark
```

For a quick run (1 fork, 1 warmup, 1 measurement):

```bash
java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark -f 1 -wi 1 -i 1
```
