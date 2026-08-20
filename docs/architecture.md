# Architecture & Design

## Overview

The library provides primitive and generic collection implementations: sets and maps. The primitive types avoid the overhead of boxing `int` to `Integer`. The `IntSet` uses a compact bit-packing scheme where each hash slot stores up to 32 elements. Map classes implement their respective `java.util` interfaces for interoperability. Set classes implement `java.util.Set` similarly.

## Project Structure

The project is a multi-module Maven build:

| Module | Artifact | Description |
|--------|----------|-------------|
| `core` | `ru.snake.primitive:primitive-core` | Production code and unit tests |
| `benchmarks` | `ru.snake.primitive:primitive-benchmarks` | JMH micro-benchmarks; depends on `core` |
| `memory` | `ru.snake.primitive:primitive-memory` | JOL-based memory footprint analysis; depends on `core` |

Benchmark code is isolated in its own module to keep JMH dependencies out of the core library and to produce a separate runnable JAR. The memory analysis module is similarly isolated with JOL, and produces a standalone shaded JAR for measuring collection memory usage.

## Package Structure

All classes live under `ru.snake.primitive` and are split by collection type:

| Package | Contents |
|---------|----------|
| `ru.snake.primitive.set` | `IntBitSet`, `IntSet`, `ObjectSet` |
| `ru.snake.primitive.map` | `IntToIntMap`, `IntToObjectMap`, `ObjectToIntMap`, `ObjectMap` |

The `set` and `map` packages keep related types grouped and make it easy to add new collection categories (e.g., `list`, `queue`) without cluttering a single package.

## IntBitSet

A BitSet that stores each boolean value as an individual bit in an `int[]`. Each array element holds 32 values (one per bit).

### Internal Structure

- `int[] words` — the bit storage; `words[i]` holds bits `[i*32 .. i*34]`
- `int size` — cached count of set bits
- `int capacity` — total addressable bits (0 to capacity-1)

### Key Operations

| Operation | Implementation |
|-----------|---------------|
| `get(i)` | `words[i >> 5] & (1 << (i & 0x1F))` |
| `set(i)` | OR the bit; increment `size` if it was 0 |
| `clear(i)` | AND-not the bit; decrement `size` if it was 1 |
| `toArray()` | Iterates set bits in word/bit order; fills `Object[size]` directly |
| `toArray(T[])` | Collects into `Object[size]`, then copies into caller's array or allocates via `Arrays.copyOf` |
| `iterator()` | Custom `IntBitSetIterator` walks word/bit state inline; supports `remove()` by clearing the last-returned bit |

### Constraints

- Elements must be non-negative and less than the configured capacity.
- Capacity is fixed at construction time — no auto-resize.

## IntSet

A compact hash set of `int` values. Each stored integer is split: the top 27 bits serve as the hash map key and the bottom 5 bits select a single bit within the map value. One map slot (`int`) can therefore hold up to 32 elements that share the same 27-bit prefix.

### Internal Structure

- `int[] keys` — the top 27 bits of stored elements; a value of `-1` denotes an empty slot (safe sentinel because valid keys have their lower 5 bits cleared)
- `int[] values` — packed bit words; each `int` holds up to 32 elements
- `int occupiedCount` — number of occupied slots (keys[i] != -1), used for load-factor checks
- `int size` — number of distinct elements

### Hashing

Uses a multi-shift mixing function: `h ^= (h >>> 20) ^ (h >>> 12); h ^= (h >>> 7) ^ (h >>> 4)`, then masks with `capacity - 1` (capacity is always a power of two).

### Collision Resolution

Linear probing — on collision, walk to the next slot (`(index + 1) & mask`) until an empty slot is found.  Backward-shift on removal keeps chains compact without tombstones.

### Resizing

When the number of occupied slots exceeds `capacity * 0.75`, the
table doubles in size and all entries are rehashed into the new table.

### Removal Strategy

Removal clears the bit from the packed word. If no bits remain in the slot,
the slot is cleared and subsequent entries in the same probe chain are shifted
backward (**backward-shift deletion**). Each entry is checked: if its original
hash position would have probed through the vacated slot, it is moved one
position back. This repeats until no more entries qualify for shifting.

This keeps probe chains compact without needing tombstone markers or
periodic cleanup. Lookup and insertion probe only live slots and stop at
the first empty slot.

### Bulk Operations

`retainAll` rebuilds each packed word by keeping only the bits whose elements are present in the argument collection, avoiding the infinite-loop pitfall of not advancing through all bits.

### toArray

`toArray()` allocates `Object[size]` and fills it directly — no intermediate `ArrayList`. `toArray(T[])` follows the `Collection` contract: reuses the caller's array if large enough (null-terminating at `a[size]` when oversized), or allocates a new correctly-typed array via `Arrays.copyOf`.

## ObjectSet

A generic open-addressed hash set backed by `Object[]` with linear probing. Follows the same architecture as the map classes but stores only elements (no associated values).

### Internal Structure

- `E[] keys` — element storage; a `null` entry denotes an empty slot
- `int size` — number of live elements

### Hashing

Same multi-shift mixing function as the map classes, masked with `capacity - 1`.

### Collision Resolution

Linear probing — identical to the map classes.

### Resizing

Same load factor (0.75) and doubling strategy as the maps.

### Removal Strategy

Same backward-shift deletion as the map classes — clears the slot and shifts
subsequent chain entries backward, eliminating the need for tombstones
or full rehash.

### Primitive Convenience Methods

| Method | Returns | Notes |
|--------|---------|-------|
| `add0(E)` | `boolean` | Add element, true if not already present |
| `remove0(Object)` | `boolean` | Remove element, true if present |
| `contains0(Object)` | `boolean` | Element presence check |
| `putAll(ObjectSet)` | `void` | Copy from same-type set |

These use primitive return types to avoid unnecessary boxing where possible.

### Null Handling

- `null` elements are **not supported** — `NullPointerException` is thrown for `add0`.
- `add(null)` (Set interface) throws `NullPointerException`.
- `contains(null)` / `remove(null)` return `false`.

### toArray

Same pattern as `IntSet` — direct `Object[size]` allocation and `Arrays.copyOf` for the typed variant; no intermediate `ArrayList`.

### Interface Compliance

Extends `AbstractSet<E>`. Uses direct `equals()` for element comparison (after a null guard).

## IntToIntMap

An open-addressed hash map using parallel `int[]` arrays for keys and values. Collision resolution uses linear probing.

### Internal Structure

- `int[] keys` - key storage
- `int[] values` - parallel value storage
- `IntBitSet occupied` - tracks which table slots hold live entries
- `int size` - number of live mappings

### Hashing

Uses a multi-shift mixing function: `h ^= (h >>> 20) ^ (h >>> 12); h ^= (h >>> 7) ^ (h >>> 4)`, then masks with `capacity - 1` (capacity is always a power of two).

### Collision Resolution

Linear probing — on collision, walk to the next slot (`(index + 1) & mask`) until an empty slot is found.  Backward-shift on removal keeps chains compact without tombstones.

### Resizing

When `size > capacity * 0.75`, the table doubles in size and all entries are rehashed into the new table.

### Removal Strategy

Removal clears the slot and shifts subsequent probe-chain entries backward
(**backward-shift deletion**). Each entry is checked: if its original hash
position would have probed through the vacated slot, it is moved one
position back. This repeats until no more entries qualify for shifting.

This keeps probe chains compact without needing tombstone markers or
periodic cleanup. Lookup and insertion probe only live slots and stop at
the first empty slot.

## ObjectToIntMap

A hybrid open-addressed hash map using `Object[]` for keys and `int[]` for values. This avoids boxing overhead for the values while allowing arbitrary Object types for keys.

### Internal Structure

- `Object[] keys` — key storage; a `null` entry denotes an empty slot
- `int[] values` — parallel primitive value storage
- `int size` — number of live mappings

### Hashing

Same multi-shift mixing function as the other map classes, masked with `capacity - 1`.

### Collision Resolution

Linear probing — identical to `IntToIntMap`.

### Resizing

Same load factor (0.75) and doubling strategy as `IntToIntMap`.

### Removal Strategy

Same backward-shift deletion as `IntToIntMap` — clears the slot and shifts
subsequent chain entries backward, eliminating the need for tombstones
or full rehash.

### Primitive Convenience Methods

| Method | Returns | Notes |
|--------|---------|-------|
| `putInt(K, int)` | `int` | Previous value, or 0 if absent |
| `getInt(Object)` | `int` | Value, or 0 if absent |
| `getOrDefault(Object, int)` | `int` | Value or default |
| `hasKey(K)` | `boolean` | Key presence check |
| `containsValue(int)` | `boolean` | Value presence check |
| `delete(K)` | `int` | Removed value, or 0 if absent |
| `putAll(ObjectToIntMap)` | `void` | Copy from same-type map |

These methods use `int` return types (not `Integer`) to avoid unnecessary boxing.
Method names differ from `Map` interface to avoid erasure conflicts:
`getInt` instead of `get(int)`, `hasKey` instead of `containsKey(K)`, `delete` instead of `remove(int)`.

### Null Handling

- `null` keys are **not supported** — `NullPointerException` is thrown for `putInt`, `hasKey`, `delete`.
- `null` keys return `null` (Map interface) or 0/false (primitive) for `getInt`, `containsKey`, `getOrDefault`, `remove`.
- `getOrDefault(nullKey, default)` returns the default.
- `containsKey(null)` / `containsValue(null)` return `false`.
- `remove(null)` returns `null`.

### Interface Compliance

Implements `Map<K, Integer>`. `null` keys throw `NullPointerException` in primitive methods;
`null` keys handled gracefully in Map interface methods (return `null`/`false`).
Uses direct `equals()` for key comparison.

## IntToObjectMap

A hybrid open-addressed hash map using `int[]` for keys and `Object[]` for values. This avoids boxing overhead for the keys while allowing arbitrary Object types for values.

### Internal Structure

- `int[] keys` — key storage
- `Object[] values` — parallel value storage; an `EMPTY_SLOT` sentinel object denotes an empty slot (allows occupancy tracking without a separate bitset)
- `int size` — number of live mappings

### Hashing

Same multi-shift mixing function as the other map classes, masked with `capacity - 1`.

### Collision Resolution

Linear probing — identical to `IntToIntMap`.

### Resizing

Same load factor (0.75) and doubling strategy as `IntToIntMap`.

### Removal Strategy

Same backward-shift deletion as `IntToIntMap` — clears the slot and shifts
subsequent chain entries backward, eliminating the need for tombstones
or full rehash.

### Primitive Convenience Methods

| Method | Returns | Notes |
|--------|---------|-------|
| `put(int, V)` | `V` | Previous value, or null if absent |
| `get(int)` | `V` | Value, or null if absent |
| `getOrDefault(int, V)` | `V` | Value or default |
| `containsKey(int)` | `boolean` | Key presence check |
| `containsValue(Object)` | `boolean` | Value presence check |
| `remove(int)` | `V` | Removed value, or null if absent |

These methods use primitive `int` keys to avoid boxing.

### Null Handling

- `null` values are **not supported** — `NullPointerException` is thrown for `put(int, V)`.
- `null` keys are not applicable (keys are primitive `int`).
- `get(nullKey)` / `getOrDefault(nullKey, default)` — not applicable for primitive key methods.
- `containsKey(null)` (Map interface) returns `false`.
- `containsValue(null)` returns `false`.
- `remove(null)` (Map interface) returns `null`.

### Interface Compliance

Implements `Map<Integer, V>`. `null` values throw `NullPointerException`; `null` keys handled gracefully in Map interface methods (return `null`/`false`).

## ObjectMap

A generic open-addressed hash map using parallel `Object[]` arrays for keys and values. Shares the same architecture as the primitive maps but works with arbitrary key and value types.

### Internal Structure

- `K[] keys` — key storage; a `null` entry denotes an empty slot
- `V[] values` — parallel value storage
- `int size` — number of live mappings

### Hashing

Same multi-shift mixing function as the other map classes, masked with `capacity - 1`.

### Collision Resolution

Linear probing — identical to the other map classes.

### Resizing

Same load factor (0.75) and doubling strategy as the other map classes.

### Removal Strategy

Same backward-shift deletion as the other map classes — clears the slot and shifts
subsequent chain entries backward, eliminating the need for tombstones
or full rehash.

### Null Handling

- `null` keys and `null` values are **not supported** — `NullPointerException` is thrown.
- `getOrDefault(nullKey, default)` returns the default without throwing.
- `containsKey(null)` / `containsValue(null)` return `false`.
- `remove(null)` returns `null`.

### Interface Compliance

Implements `Map<K, V>`. `null` keys and values throw `NullPointerException`. Uses direct `equals()` for key/value comparison.

## Shared Design Decisions

- **Capacity**: Always a power of two, starting from a default of 16, up to a maximum of `1 << 30`.
- **Load factor**: 0.75, matching `java.util.HashMap`.
- **Occupancy tracking**: `IntToIntMap` uses `IntBitSet` to track occupied slots. `IntSet` uses a `-1` sentinel in its `keys` array. `IntToObjectMap` uses an `EMPTY_SLOT` sentinel in its `values` array. `ObjectSet`, `ObjectToIntMap`, and `ObjectMap` use `null` as an empty-slot sentinel — no separate occupancy tracker needed.
- **Removal**: All map and set classes use backward-shift deletion — cleared slots trigger a compacting scan that shifts subsequent chain entries backward, eliminating the need for tombstones or full rehash on removal.
- **Thread safety**: None of the classes are thread-safe.
- **IntSet compact encoding**: 27-bit key + 5-bit offset packed into hash table slots, allowing up to 32 elements per slot without extra indirection.
- **Iterators**: All collections use custom inner-class iterators that walk internal arrays directly — no intermediate `ArrayList` allocation. Every iterator supports `remove()` by delegating to the collection's internal removal logic (which handles `shiftBack` and slot cleanup). After `remove()`, the iterator re-syncs its cursor from the last-removed position to account for any shifted entries.
- **toArray**: All `Set` implementations allocate `Object[size]` and fill it directly with a for-loop — no `ArrayList` intermediary. The typed `toArray(T[])` reuses the caller's array when large enough (null-terminating per `Collection` contract) or allocates a new correctly-typed array via `Arrays.copyOf`.
