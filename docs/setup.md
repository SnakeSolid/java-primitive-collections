# Development Setup

## Requirements

| Tool | Version |
|------|---------|
| Java (JDK) | 21 or later |
| Maven | 3.6+ (or use the bundled `mvnw` wrapper) |

## Building

```bash
# Compile all modules
./mvnw compile

# Run tests (core module only)
./mvnw test

# Build JARs
./mvnw package

# Run benchmarks
./mvnw package && java -jar benchmarks/target/benchmarks-0.0.1-SNAPSHOT-benchmarks.jar
```

## Project Layout

```
simple-collections/
├── pom.xml                       # Parent POM (packaging: pom)
├── mvnw, mvnw.cmd               # Maven wrapper
├── docs/                         # Documentation
├── core/                         # Core module — production code + unit tests
│   ├── pom.xml
│   └── src/
│       ├── main/java/ru/snake/collections/
│       │   ├── set/
│       │   │   ├── IntBitSet.java
│       │   │   ├── IntSet.java
│       │   │   └── ObjectSet.java
│       │   └── map/
│       │       ├── IntToIntMap.java
│       │       ├── ObjectToIntMap.java
│       │       └── ObjectMap.java
│       └── test/java/ru/snake/collections/
│           ├── set/
│           │   ├── IntBitSetTest.java
│           │   ├── IntSetShiftTest.java
│           │   ├── IntSetTest.java
│           │   └── ObjectSetTest.java
│           └── map/
│               ├── IntToIntMapShiftTest.java
│               ├── IntToIntMapTest.java
│               ├── ObjectMapShiftTest.java
│               ├── ObjectMapTest.java
│               ├── ObjectToIntMapShiftTest.java
│               └── ObjectToIntMapTest.java
└── benchmarks/                   # Benchmarks module — JMH micro-benchmarks
    ├── pom.xml
    └── src/main/java/ru/snake/collections/benchmark/
        ├── IntBitSetBenchmark.java
        ├── IntSetBenchmark.java
        ├── IntToIntMapBenchmark.java
        ├── ObjectMapBenchmark.java
        ├── ObjectSetBenchmark.java
        └── ObjectToIntMapBenchmark.java
```

## Multi-Module Structure

| Module | Artifact | Purpose |
|--------|----------|---------|
| `core` | `ru.snake.collections:core` | Production code and unit tests |
| `benchmarks` | `ru.snake.collections:benchmarks` | JMH benchmarks, depends on `core` |

## IDE

Any IDE that supports Java 21 and Maven works. Import the parent `pom.xml` as a Maven project — the IDE will detect the multi-module structure automatically.

- **IntelliJ IDEA**: Click "Reload All Maven Projects" after restructuring to pick up the new module layout.
