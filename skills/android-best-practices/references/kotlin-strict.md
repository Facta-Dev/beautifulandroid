# Kotlin strict for Android

The Kotlin 2.x subset and the type patterns that make a native app hard to
break.

## 1. The subset

- Kotlin 2.x (K2), explicit API mode for library modules, `-Werror` in CI.
- Coroutines + Flow; kotlinx.serialization (no reflection-heavy JSON).
- `val` over `var`; immutable data classes; `data object` for singletons.
- Nullability is part of the type; no `!!` outside a proven invariant, and
  never a Java platform type flowing into domain.
- No `lateinit` for optional state; use a nullable or a sealed state.
- `@JvmInline value class` for ids/units.

## 2. Sealed state

```kotlin
sealed interface InventoryState {
    data object Loading : InventoryState
    data object Empty : InventoryState
    data class Ready(val rows: List<InventoryRow>) : InventoryState
    data class Forbidden(val requiredPermission: String?) : InventoryState
    data object Offline : InventoryState
    data class Error(val correlationId: String?) : InventoryState
}
```

- `when` over a sealed type is exhaustive; a new case is a compile error.
- Never model a screen with independent booleans.

## 3. Nullability and value classes

```kotlin
@JvmInline
value class InventoryId(val value: String)
```

- A function taking `InventoryId` cannot receive a raw `String` or another
  id.
- Parse at the boundary (deep link, API response); inside, the type is
  trusted.
- `require`/`check` for programmer invariants; a domain error is a value.

## 4. Results

- Domain/use cases return a sealed `Result`/`Outcome`, not exceptions for
  expected failures.
- `runCatching` only at a boundary where the exception is genuinely unknown;
  it hides cancellation — rethrow `CancellationException`.
- Every error carries a stable `code` and a `correlationId`.

## 5. Coroutines

- Injected dispatchers; no `Dispatchers.IO` in a ViewModel.
- `viewModelScope` for screen work; a repository uses `withContext(io)`.
- `StateFlow` for state; `SharedFlow` for effects; `collectAsStateWithLifecycle`.
- Structured concurrency; no `GlobalScope`; cancellation propagates.
- A blocking call in a coroutine on `Dispatchers.Main` is still a main-thread
  block.

## 6. Compose type rules

- `UiState` immutable; `List` replaced, not mutated.
- An unstable parameter re-renders the subtree; use immutable data.
- `remember`/`derivedStateOf` for UI-local values, not for business state.
- A lambda passed to a child should not capture unstable values.

## 7. Detekt and ktlint

- detekt: complexity, coroutine rules, potential bugs, naming, style; a
  `@Suppress` names the rule and the reason.
- ktlint via `.editorconfig`; the formatter is not a debate.
- A detekt baseline file is reviewed like code; growing it silently is a
  finding.

## 8. Review questions

1. Is the state one sealed interface?
2. Is every `when` exhaustive?
3. Is anything blocking the main thread?
4. Is the dispatcher injected?
5. Is the id a value class, or can a raw string stand in?
6. Is every external value parsed once (link, push, API)?
7. Is the error a value, and does cancellation propagate?
