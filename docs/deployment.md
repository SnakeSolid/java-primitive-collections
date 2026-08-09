# Deployment & Operations

## Building

```bash
./mvnw package
```

## Artifacts

| Module | JAR | Location |
|--------|-----|----------|
| `core` | `primitive-core-0.0.1-SNAPSHOT.jar` | `core/target/` |
| `benchmarks` | `primitive-benchmarks-0.0.1-SNAPSHOT-benchmarks.jar` | `benchmarks/target/` |

The `core` JAR has no external runtime dependencies and can be dropped onto any classpath with a Java 21+ runtime.

The `benchmarks` shaded JAR is self-contained (includes JMH + core) and is runnable via `java -jar`.

## Running Benchmarks

```bash
java -jar benchmarks/target/primitive-benchmarks-0.0.1-SNAPSHOT-benchmarks.jar
```

Run a specific benchmark:

```bash
java -jar benchmarks/target/primitive-benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark
```

Quick run (1 fork, 1 warmup, 1 measurement):

```bash
java -jar benchmarks/target/primitive-benchmarks-0.0.1-SNAPSHOT-benchmarks.jar IntBitSetBenchmark -f 1 -wi 1 -i 1
```

## Versioning

Managed via `<version>` in parent `pom.xml`. Current version: `0.0.1-SNAPSHOT`.

## CI/CD

No CI pipeline is configured yet. To add one, run `./mvnw verify` as the build/test step.
