# A worked review: the inventory screen

The subject is a small Compose inventory screen. It renders, it works on a
fast device, and it has five findings that a review must rank in a useful
order. The fixed version follows the contract in `android-best-practices`.

## The module under review

```kotlin
// InventoryViewModel.kt
class InventoryViewModel(private val dao: InventoryDao) : ViewModel() {
    var loading by mutableStateOf(true)
    var error by mutableStateOf(false)
    var rows by mutableStateOf(emptyList<InventoryRow>())

    fun refresh() {
        loading = true
        runBlocking { rows = dao.all() }  // Room on the main thread
        loading = false
    }
}

// InventoryScreen.kt
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    if (viewModel.loading) {
        CircularProgressIndicator()
        return
    }
    LazyColumn {
        items(viewModel.rows) { row ->
            Row(Modifier.clickable { viewModel.archive(row.id) }) {
                Text(row.name)
                IconButton(onClick = { viewModel.archive(row.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}
```

## The report

### Verdict

The screen renders on a fast device and freezes the main thread on a real
database. Not safe to ship; the main-thread violation is first because it is
the one every screen copies.

### Must fix

1. `InventoryViewModel.kt:7` — `runBlocking` runs a Room query on the main
   thread. Rule: *the main thread never blocks; dispatchers are injected*.
   Fix: a `suspend` use case on `viewModelScope` with an injected
   `@IoDispatcher`.
2. `InventoryViewModel.kt:2` — the ViewModel depends on a DAO, not a use
   case; the data adapter leaks into the UI module. Rule: *the ui module
   depends on domain ports, not data adapters*. Fix: an `InventoryUseCases`
   port; the data module implements it.
3. `InventoryViewModel.kt:3` — `loading`, `error` and `rows` can contradict
   each other; offline and forbidden never render. Rule: *one closed
   `UiState`; every state is rendered*. Fix: a sealed `InventoryState` and a
   `when` that misses a case does not compile.
4. `InventoryScreen.kt:14` — `items(viewModel.rows)` has no `key` or
   `contentType`; a reorder reuses the wrong row. Rule: *lazy lists have keys
   and a content type*. Fix: `items(rows, key = { it.id })`.
5. `InventoryScreen.kt:18` — the icon-only delete button has
   `contentDescription = null`, and the row itself is clickable with no
   action label. Rule: *every control has an accessible name*. Fix:
   `contentDescription = stringResource(R.string.archive)`; the row is not a
   second action.

### Should fix

6. `InventoryScreen.kt:16` — the whole row is clickable and contains a button;
   the tap target is ambiguous and the row has no role. Fix: a single action
   per control, or a row with a menu.
7. `InventoryViewModel.kt:8` — the archive mutation is fire-and-forget with no
   idempotency key and no rollback. Fix: the Room outbox + WorkManager drain.
8. `InventoryViewModel.kt:2` — in the full version the token is read from
   `SharedPreferences`. Fix: Keystore + Tink behind a `TokenProvider` port.

### Waived gates

None. The diff does not touch the lint config, the tests, or the performance
budget.

## The fixed module

```kotlin
// InventoryViewModel.kt
sealed interface InventoryState {
    data object Loading : InventoryState
    data object Empty : InventoryState
    data class Ready(val rows: List<InventoryRow>) : InventoryState
    data class Forbidden(val requiredPermission: String?) : InventoryState
    data object Offline : InventoryState
    data class Error(val correlationId: String?) : InventoryState
}

class InventoryViewModel(
    private val useCases: InventoryUseCases,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ViewModel() {
    private val mutableState = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state: StateFlow<InventoryState> = mutableState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            mutableState.value = withContext(io) { useCases.load().toState() }
        }
    }
}
```

```kotlin
// InventoryScreen.kt
@Composable
fun InventoryScreen(state: InventoryState, onEvent: (InventoryEvent) -> Unit) {
    when (state) {
        InventoryState.Loading -> CircularProgressIndicator()
        InventoryState.Empty -> Text(stringResource(R.string.inventory_empty))
        InventoryState.Offline -> Text(stringResource(R.string.offline))
        is InventoryState.Forbidden -> Text(stringResource(R.string.forbidden))
        is InventoryState.Error -> Text(stringResource(R.string.retry))
        is InventoryState.Ready -> LazyColumn {
            items(state.rows, key = { it.id }) { row ->
                Row(Modifier.semantics { role = Role.Button }) {
                    Text(row.name)
                    IconButton(onClick = { onEvent(InventoryEvent.Archive(row.id)) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.archive))
                    }
                }
            }
        }
    }
}
```

## What changed, and why it matters

| Finding | The fixed version |
|---|---|
| `runBlocking` Room on the main thread | `viewModelScope` + injected IO dispatcher |
| ViewModel depends on the DAO | a domain use-case port |
| Three booleans, missing states | one sealed `UiState`, every state rendered |
| `items` without keys | `key = { it.id }` |
| Icon button with no name | `contentDescription` + a single action per control |
| Fire-and-forget archive | Room outbox + WorkManager |

The review is short on purpose. Five Must-fix findings, each with a location,
a rule, and a fix, is a better review than twenty comments that repeat the
same contract in different words.
