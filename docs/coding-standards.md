# Coding Conventions

## General

- **Java 21** is the minimum target. New language features may be used where they improve clarity, but the codebase does not currently use pattern matching for `instanceof` or `switch` expressions.
- **No external dependencies** beyond JUnit Jupiter for tests, JMH for benchmarks, and JOL for memory analysis. All managed in the parent POM's `<dependencyManagement>`.
- **Null handling varies by class:**
  - Classes with object keys reject `null` keys via `Objects.requireNonNull()` in primitive methods and guard clauses in `Map`/`Set` interface methods.
  - `IntToObjectMap` rejects `null` values but has no concept of null keys (keys are primitive `int`).
  - `IntToIntMap` and `IntToObjectMap` reject `null` boxed arguments passed to `Map` interface methods via `throw new NullPointerException()`.
  - Use `Objects.requireNonNull(value, "message must not be null")` for object parameters in primitive methods.
  - Use bare `throw new NullPointerException()` for boxed-method null guards.

## Naming

- Classes: `PascalCase` (e.g., `IntBitSet`, `ObjectMap`)
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE` with `private static final`
- Package: `ru.snake.primitive` with subpackages by collection type (`set/`, `map/`)
- Test classes: `{ClassName}Test` (package-private, no `public`)
- Inner classes: `PascalCase` (e.g., `KeyIterator`, `IntIntEntry`)

## Formatting

- Hard tab indentation (`\t`), tab width 4. Configured in `.editorconfig`.
- Section separators: `// ------------------------------------------------------------------`
- Javadoc on all public members; brief comments on private helpers.
- Line length: aim for ~120 columns, but longer lines are acceptable when truncation would reduce readability.
- No trailing whitespace in source files.

## Style

- Prefer primitive methods (`put(int, int)`) over boxed ones for internal logic.
- Guard clauses at the top of methods (null checks, argument validation).
- Keep methods short — extract helpers for non-trivial logic.
- All collection classes are `final`. Inner iterator and entry classes are also `private final`.
- Set classes that need default `Collection` behavior `extends AbstractSet`; map classes `implements Map` directly.
- `@Override` is used on every interface or abstract method implementation.
- Key comparison inside hash probe loops uses direct `.equals()` on known-non-null keys (slots are checked for occupancy before comparison). `Objects.equals()` is reserved for value comparisons in entry equality and `containsValue` checks.
- Imports: standard library (`java.*`) first, then project imports (`ru.snake.primitive.*`), grouped and sorted alphabetically within each group.
- `java.lang` imports are never written.
- Curly braces are **mandatory** on `if`, `for`, `while`, `do`, `switch`, and `try` blocks — even for single-statement bodies.

## Testing

- Framework: JUnit Jupiter 5.10.2.
- Test class names: `{ClassName}Test` (package-private).
- Test method names: descriptive `void` methods using `camelCase` (e.g., `putAndGetSingleEntry`).
- Group related tests with comment separators: `// ------------------------------------------------------------------`
- One assertion per test where possible; use `assertThrows` for expected exceptions.
- Static imports from `org.junit.jupiter.api.Assertions`.
