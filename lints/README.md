# Beautiful Android lints

The two-file unit that turns the android-best-practices skill into red-on-save
output:

- `detekt.yml` — the drop-in check set: coroutine misuse (`GlobalScope`,
  redundant `suspend`, hard-coded dispatchers), complexity, potential bugs,
  naming, style and exception handling, with warnings as errors and a zero
  issue budget.
- `.editorconfig` — the ktlint formatting rules (4-space indent, 120 columns,
  trailing commas, no wildcard imports).

## Running it

```bash
./gradlew detekt
./gradlew ktlintCheck
```

`scripts/check-lints.sh` runs detekt on the repository's clean control and
violation harness when detekt is installed. When it is missing it prints a
visible `SKIP` and exits 0 — the gate runs in CI, where the tool is pinned,
never silently downgraded on a machine that has it.

## The check project

`lints/check/clean.kt` must stay quiet; `lints/check/violations.kt` holds
deliberate findings (a magic number, a long method, `GlobalScope`, a swallowed
exception, a TODO). CI fails if either expectation breaks.

A `@Suppress` in product code names the detekt rule and the reason on the
narrowest declaration. A module-wide suppression or a grown baseline is a
finding, not a fix.
