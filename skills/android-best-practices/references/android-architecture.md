# Android architecture contract

The layer contract from the facta Android handbook, reduced to what a native
app must keep. The Gradle module graph is the enforcement.

## 1. Layers and modules

| Layer | Module | Contains | Must not depend on |
|---|---|---|---|
| **domain** | `:core:<slice>:domain` (pure Kotlin/JVM) | Entities, value objects, rules, ports | Android, Compose, Room, Retrofit, Hilt |
| **data** | `:core:<slice>:data` | SDK, Room, DataStore adapters | Compose, feature UI |
| **ui** | `:feature:<slice>:ui` | ViewModel, `UiState`, events, effects, composables | other features' internals, data adapters |
| **app** | `:apps:<variant>` | Composition root, nav host, DI wiring | — |
| **platform** | `:core:platform` | Android capability adapters behind ports | feature UI |

- A module's `dependencies {}` is the complete list of what it may import; a
  forbidden edge is a compile error.
- Domain modules apply the Kotlin/JVM plugin, not the Android plugin; an
  `android.*` import there does not compile.
- One variant = one application module; permissions and native code are
  scoped per variant.

## 2. Ports

The consumer declares the port; the data/platform module implements it.

| Port | Declared by | Implemented by |
|---|---|---|
| `InventoryRepository` | feature domain | SDK + Room adapter |
| `SessionStore` | domain | Keystore + DataStore adapter |
| `TokenProvider` | domain | Keystore/Tink adapter |
| `Navigator` | feature ui | Navigation 3 host |
| `Notifier` | feature ui | FCM/local notification adapter |
| `Clock` | domain | system clock (tests use a fake) |
| `LocationSource` | feature domain | FusedLocation adapter |
| `SceneEngine` | feature domain | tiered 3D adapters + 2D fallback |
| `PermissionChecker` | feature ui | Android permission adapter |

Ports return domain types; no `android.*` type crosses into domain.

## 3. UDF

- `UiState` is one sealed interface; `UiEvent` is one sealed interface;
  effects are a separate one-shot channel.
- The ViewModel exposes `StateFlow<UiState>` and `onEvent(UiEvent)`.
- Composables render the state and forward events; no business logic.
- The ViewModel never holds a `Context`, a View or a composable.

## 4. Variants

- `:apps:game`, `:apps:market`, `:apps:avatar` are thin app modules depending
  on shared feature/core modules.
- The market app does not depend on the game engine or its permissions.
- Product flavors that merely hide code are not the architecture.

## 5. Concurrency

- Injected dispatchers (`@IoDispatcher`, `@DefaultDispatcher`), not
  `Dispatchers.IO` inline.
- `viewModelScope` for screen work; WorkManager for deferrable guaranteed
  work.
- Structured concurrency; no `GlobalScope`; cancellation propagates.
- No blocking call on the main thread; StrictMode in debug.

## 6. Data and offline

- Room (SQLite/WAL) + DataStore; secrets in Keystore + Tink.
- The outbox is a Room table drained by WorkManager with an idempotency key.
- A conflict is resolved by the server; the local change is rejected visibly.
- Paging 3 for feeds; a stable key and a bounded page.

## 7. Navigation

- Navigation 3 typed `NavKey`; the route table mirrors the URL playbook.
- Deep links are parsed/validated before becoming a key; unknown → not-found.
- A signed-out link is queued and resumed after login.
- Push taps use the same parser.

## 8. Security

- Secrets in Keystore + Tink; never in Room/DataStore/logs/APK.
- Auth: Credential Manager or AppAuth + Custom Tabs with PKCE.
- The server is authoritative for game state, prices, payments,
  entitlements.
- Root/emulator/Play Integrity are server-side signals, not client blocks.
- Certificate pinning only with a rotation plan.

## 9. Testing

- Domain/ViewModel: JUnit + `kotlinx-coroutines-test`, fake ports.
- Compose UI: `createComposeRule`, semantics-first, one test per state.
- Room: in-memory DB + migration fixtures.
- WorkManager: `WorkManagerTestInitHelper`, idempotency/retry.
- Macrobenchmark: startup/scroll on the Low pool.
- Device lab: Low/mid/flagship, StrictMode clean.

## 10. The enforcement map

| Rule | Where |
|---|---|
| domain has no `android.*` | JVM-only module + a detekt/forbidden-import check |
| feature UI does not import another feature's internals | Gradle dependency graph |
| the app module contains no business logic | review + module graph |
| no main-thread I/O | StrictMode + detekt coroutine rules + review |
| no secret outside the Keystore | review + detekt/security scan |
| no unused variant engine | Gradle graph + a size check |

When a change needs an exception, re-cut the layout; do not add a dependency
or a `@Suppress`. A config exception is a contract change.
