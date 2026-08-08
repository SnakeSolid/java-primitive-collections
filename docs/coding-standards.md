# Coding Conventions

## General

- **Java 21** features are available and should be used where appropriate (e.g., pattern matching for `instanceof`, `switch` expressions).
- **No external dependencies** beyond JUnit for tests and JMH for benchmarks.
- **No null keys or values** — throw `NullPointerException` explicitly.
- Classes are `final` unless designed for extension.

## Naming

- Classes: `PascalCase` (e.g., `IntBitSet`, `ObjectMap`)
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE` with `private static final`
- Package: `ru.snake.collections` with subpackages by collection type (`set/`, `map/`)

## Formatting

- 4-space indentation (no tabs).
- Section separators: `// ------------------------------------------------------------------`
- Javadoc on all public members; brief comments on private helpers.

## Style

- Prefer primitive methods (`put(int, int)`) over boxed ones for internal logic.
- Guard clauses at the top of methods (null checks, argument validation).
- Keep methods short — extract helpers for non-trivial logic.
