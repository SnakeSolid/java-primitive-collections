# Coding Conventions

## General

- **Java 21** features are available and should be used where appropriate (e.g., pattern matching for `instanceof`, `switch` expressions).
- **No external dependencies** beyond JUnit for tests, JMH for benchmarks, and JOL for memory analysis. All managed in the parent POM's `<dependencyManagement>`.
- **No null keys or values** — throw `NullPointerException` explicitly.

## Naming

- Classes: `PascalCase` (e.g., `IntBitSet`, `ObjectMap`)
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE` with `private static final`
- Package: `ru.snake.primitive` with subpackages by collection type (`set/`, `map/`)

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
- Classes are `final` unless designed for extension.
- Imports: standard library (`java.*`) first, then project imports (`ru.snake.primitive.*`), grouped and sorted alphabetically within each group.
- Curly braces are **mandatory** on `if`, `for`, `while`, `do`, `switch`, and `try` blocks — even for single-statement bodies.
