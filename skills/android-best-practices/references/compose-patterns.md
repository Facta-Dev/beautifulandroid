# Compose patterns

The patterns a Compose app needs, with the failure mode each prevents.

## 1. The UDF contract

```kotlin
sealed interface InventoryState {
    data object Loading : InventoryState
    data object Empty : InventoryState
    data class Ready(val rows: List<InventoryRow>) : InventoryState
    data class Forbidden(val requiredPermission: String?) : InventoryState
    data object Offline : InventoryState
    data class Error(val correlationId: String?) : InventoryState
}

sealed interface InventoryEvent {
    data object Refresh : InventoryEvent
    data class Archive(val id: String) : InventoryEvent
}

class InventoryViewModel(private val useCases: InventoryUseCases) : ViewModel() {
    private val _state = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    fun onEvent(event: InventoryEvent) {
        when (event) {
            InventoryEvent.Refresh -> refresh()
            is InventoryEvent.Archive -> archive(event.id)
        }
    }
}
```

- One sealed state; a `when` that misses a case does not compile.
- `StateFlow` down; events up; one-shot effects on a `SharedFlow`.
- The ViewModel never holds a `Context`, a View or a composable.

## 2. State hoisting

- A composable takes a value and emits an event:
  `InventoryScreen(state = state, onEvent = viewModel::onEvent)`.
- UI-local state (a dropdown open) uses `remember`; business state lives in
  the ViewModel.
- Do not pass a ViewModel deep into the tree; pass state and lambdas.

## 3. Lazy lists

- `items(rows, key = { it.id }, contentType = { it.kind })`.
- Never an index key on reorderable data.
- A stable, immutable row type; `@Immutable` only when true.
- Paging 3 for unbounded feeds; a placeholder and an error footer.

## 4. Effects

- `LaunchedEffect(key)` for a keyed side effect; it cancels on recomposition
  with a new key.
- `DisposableEffect` for cleanup (listeners, observers).
- `rememberCoroutineScope` for user-triggered work launched from a click.
- No business logic in a composable; no `LaunchedEffect` for a load that
  belongs in `init`/`onEvent`.

## 5. Stability and recomposition

- An unstable parameter re-renders the subtree; use immutable data classes.
- `derivedStateOf` for a computed value that changes less often than its
  inputs.
- Do not `remember` a value that should be state; do not hide a state bug with
  memoization.
- A lambda passed to a child is stable if it does not capture unstable
  values; use method references.

## 6. Navigation 3

- Typed `NavKey` routes; the route table is the source of truth.
- The back stack is state you own; deep links append/restore it.
- A deep link is parsed and validated before it becomes a key.
- The same parser handles push taps.

## 7. Accessibility

- `contentDescription` for images/icon buttons; `semantics` for custom
  controls.
- Touch targets at least 48 dp; focus order follows the visual order.
- Test with TalkBack; dynamic type and RTL survive.
- Color is never the only signal.

## 8. Testing

- `createComposeRule`; query by semantics/text, not by test tag unless needed.
- One test per state; one per critical interaction.
- The ViewModel test uses `kotlinx-coroutines-test` and fake use cases.
- A screenshot test only where it earns its maintenance.
