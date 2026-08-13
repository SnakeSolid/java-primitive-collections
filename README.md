# Primitive Collections

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Lightweight collections for Java — no dependencies, no boxing overhead.

## Classes

| Class | Description |
|-------|-------------|
| [`IntBitSet`](core/src/main/java/ru/snake/primitive/set/IntBitSet.java) | BitSet backed by `int[]`; implements `Set<Integer>` for non-negative integers |
| [`IntSet`](core/src/main/java/ru/snake/primitive/set/IntSet.java) | Compact hash set of `int` values; each slot packs 32 elements via bit encoding; implements `Set<Integer>` |
| [`ObjectSet`](core/src/main/java/ru/snake/primitive/set/ObjectSet.java) | Generic hash set backed by `Object[]` with linear probing; implements `Set<E>` |
| [`IntToIntMap`](core/src/main/java/ru/snake/primitive/map/IntToIntMap.java) | HashMap from `int` to `int` with linear probing; implements `Map<Integer, Integer>` |
| [`ObjectToIntMap`](core/src/main/java/ru/snake/primitive/map/ObjectToIntMap.java) | HashMap from `Object` to `int` with linear probing; implements `Map<K, Integer>` |
| [`ObjectMap`](core/src/main/java/ru/snake/primitive/map/ObjectMap.java) | Generic HashMap from `K` to `V` with linear probing; implements `Map<K, V>` |

## Quick Start

```java
// Primitive bit set
IntBitSet set = new IntBitSet(1024);
set.set(42);
System.out.println(set.get(42)); // true

// Compact int set — 32 values packed per hash slot
IntSet intSet = new IntSet();
intSet.add(100);
intSet.add(200);
System.out.println(intSet.contains(100)); // true

// Generic object set
ObjectSet<String> objSet = new ObjectSet<>();
objSet.add("hello");
System.out.println(objSet.contains("hello")); // true

// Primitive int-to-int map
IntToIntMap map = new IntToIntMap();
map.put(1, 100);
System.out.println(map.get(1));  // 100

// Object keys with primitive int values
ObjectToIntMap<String> map3 = new ObjectToIntMap<>();
map3.putInt("counter", 42);
System.out.println(map3.getInt("counter")); // 42

// Generic object-to-object map
ObjectMap<String, Person> map2 = new ObjectMap<>();
map2.put("Alice", new Person("Alice"));
System.out.println(map2.get("Alice")); // Person{name=Alice}
```

## Building

```bash
# Build all modules
./mvnw package

# Run tests (core module)
./mvnw test

# Run benchmarks
java -jar benchmarks/target/primitive-benchmarks-0.0.1-SNAPSHOT-benchmarks.jar

# Run memory footprint analysis
java -jar memory/target/primitive-memory-0.0.1-SNAPSHOT-standalone.jar
```

## License

This project is licensed under the [MIT License](LICENSE).
