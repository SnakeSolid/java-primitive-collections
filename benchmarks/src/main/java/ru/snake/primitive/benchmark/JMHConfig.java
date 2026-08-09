package ru.snake.primitive.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Abstract base providing common JMH configuration for all benchmarks.
 * <p>
 * All benchmark classes should extend this or use its annotations as a
 * reference to ensure consistent measurement settings across the suite.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 4, time = 2)
@Fork(value = 1, jvmArgsAppend = { "-Xms512m", "-Xmx512m" })
public abstract class JMHConfig {
}
