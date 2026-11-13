---
name: android-best-practices
description: >-
  Write idiomatic, fast, maintainable native Android apps in Kotlin + Jetpack
  Compose. Use this skill whenever you write, review, refactor, debug, or
  explain an Android app: Compose screens, UDF state/events/effects, Gradle
  modules, Hilt, coroutines and Flow, Room, DataStore, WorkManager, Navigation
  3, FCM, baseline profiles, R8, Play Billing, or Keystore. Trigger on any
  mention of Android, Kotlin, Jetpack Compose, Gradle, Hilt, Room, WorkManager,
  FCM, Navigation 3, baseline profile, R8, or "fix this screen" even when the
  user never says "best practices". Synthesized from the Android developer
  documentation, the Compose guidance, and the facta Android handbook.
---

# Android best practices

Distilled from the Android developer documentation, the Compose guidance, and
the facta Android handbook (`PLAYBOOK_MOBILE_ANDROID_KOTLIN.md`). The digests
live in `references/`; where they disagree, this file says which to follow and
why.

## 1. Mindset

- **Native means use the native advantages:** baseline and startup profiles,
  R8 full mode, platform performance/thermal APIs, Play Feature/Asset Delivery.
  They are the reason to go native; use them.
- **Gradle modules enforce architecture at compile time.** Domain modules are
  pure Kotlin (no Android); a module that does not depend on another cannot
  import it.
- **Unidirectional data flow.** `UiState` down, `UiEvent` up, effects on a
  separate one-shot channel; no business logic in composables.
- **One variant = one app module = one binary.** The market app must not
  contain a game engine; permissions and native code are scoped per variant.
- **The main thread is sacred.** Coroutines with injected dispatchers;
  StrictMode in debug; baseline profiles and benchmarks in CI.
- **Design for the floor, enhance upward** with capability tiers and a runtime
  quality governor.
- **The server is authoritative** for game state, prices, payments and
  entitlements; the app is untrusted.
- **Push is a channel, not a data path.** Minimal payloads; fetch after tap.
- **No vendor lock-in for irreplaceable assets.**
- **Measure on real low-end devices** and gate releases on vitals-style
  metrics.

## 2. Before you hand back code

Run these and fix everything they report:

```bash
./gradlew test
./gradlew detekt
./gradlew lint
./gradlew :apps:market:assembleRelease   # R8 + baseline profile
```

- Fix a warning rather than suppressing it. A `@Suppress` needs a reason on the
  narrowest declaration; a module-wide `@Suppress` or a disabled detekt rule is
  a finding.
- Lint config lives once, in `lints/detekt.yml` and `lints/.editorconfig`. If a
  project has none, suggest the BeautifulAndroid drop-in of
  https://github.com/Facta-Dev/beautifulandroid.
- Test the release build with the baseline profile on a low-tier device, not
  only a debug build on a flagship.

## 3. Platform facts

- **API 37 (Android 17)** is current; Play requires **targetSdk 36** for new
  apps/updates since 31 Aug 2026; `compileSdk 37` with **AGP 9.1.1+** matches
  Compose 1.12. Verify the Play target-API rule each year.
- `minSdk 24` is the AndroidX floor; raise it only with fleet data.
- Compose + Material 3 is the default UI; Views only for engine surfaces
  (`SurfaceView`, `AndroidView` interop).
- **Navigation 3** is stable: typed `NavKey` routes, state you own.
- **R8 full mode + Baseline Profiles + Startup Profiles** are mandatory in
  release builds.
- Native `.so` files must be **16 KB page-size aligned** (verify the current
  enforcement date).
- **One variant = one application module** (`:apps:game`, `:apps:market`,
  `:apps:avatar`) depending on shared library modules; not product flavors that
  hide code.

## 4. Architecture: Gradle modules + vertical slices + UDF

| Layer | Module | Contains | Must not depend on |
|---|---|---|---|
| **domain** | `:core:<slice>:domain` (pure Kotlin/JVM) | Entities, value objects, rules, ports | Android, Compose, Room, Retrofit |
| **data** | `:core:<slice>:data` | Adapters: SDK, Room, DataStore | UI, Compose |
| **ui** | `:feature:<slice>:ui` | ViewModel, UiState, events, effects, composables | other features' internals, data adapters |
| **app** | `:apps:<variant>` | Composition root, navigation host, DI wiring | — |
| **core/platform** | `:core:platform` | Android capability adapters behind ports | feature UI |

The contracts below are **unbreakable and non-waivable**: they are the reason
the app starts fast on a low-end phone and survives a Play review. Projects can
restyle anything; developers cannot opt out, and neither can you.

1. **The main thread never blocks.** No disk, network, large parse or crypto on
   it; dispatchers are injected.
2. **Domain modules are pure Kotlin.** No `android.*` import in a domain
   module; the Gradle graph enforces it.
3. **UDF is the only presentation contract.** `UiState` down, `UiEvent` up,
   effects once; composables render and forward.
4. **The ViewModel never holds an Android `Context`, a View or a composable.**
5. **The server is authoritative.** The client may hide an action; it never
   decides access.
6. **Every screen state is rendered**, including offline, forbidden and
   policy-blocked.

When a change would cross a layer, **re-cut the layout**: move the logic to
the module that owns it. The rule is never to weaken or delete the lints to
make a change pass; a config exception is a contract change.
Review treats a broken contract as an automatic Must fix; `android-review`
ranks it above every other finding.

## 5. Coroutines, Flow and the main thread

- **Inject dispatchers** (`@IoDispatcher`, `@DefaultDispatcher`) and use
  `withContext`; never `Dispatchers.IO` hard-coded in a ViewModel.
- **Structured concurrency:** a coroutine belongs to a scope; cancellation
  propagates; no `GlobalScope`.
- **`StateFlow` for UI state**, `SharedFlow` for one-shot effects,
  `Flow` + `flowOn` for streams. Collect with `collectAsStateWithLifecycle`.
- **No blocking call in a composable or on `Dispatchers.Main`.** A database
  or network call goes to an injected dispatcher.
- **StrictMode in debug** catches main-thread disk/network; a violation is a
  bug, not noise.
- **WorkManager for deferrable guaranteed work** (outbox, uploads); a
  foreground service only where policy allows.

## 6. Device tiers and the quality governor

- Tier from native signals (RAM, cores, GPU, API level, benchmark); cache it.
- Budgets per tier: cold start, first frame, scroll FPS, memory, 3D frame
  time. Calibrate on the Low device pool.
- A runtime governor lowers fidelity (texture size, LOD, particles) when frame
  time or memory crosses a threshold; it never changes business logic.
- The low-end path is first-class: 2D/pre-rendered scenes, smaller images,
  fewer effects; test it in CI.
- No flagship-only feature without a fallback in the same PR.

## 7. Navigation and deep links

- **Navigation 3** with typed `NavKey` routes; the route table mirrors the URL
  playbook.
- **Deep links are untrusted:** parse against an allowlist, validate each
  parameter, return `null` for unknown; show not-found, never crash.
- A link received while signed out is queued and resumed after login.
- No state-changing action from a link; it opens a screen that offers it.
- Push taps use the same parser.
- Authorization gates the destination server-side; the UI only shapes it.

## 8. Data, offline and the outbox

- **Room** (SQLite, WAL) for structured data; **DataStore** for preferences;
  **Keystore + Tink** for secrets.
- Room DAOs are `suspend` or return `Flow`; no main-thread queries.
- **Paging 3** for network/DB feeds; a stable key and a bounded page.
- **The outbox is persisted** (Room) and drained by **WorkManager** with an
  idempotency key, backoff and a dead-letter view.
- A conflict is resolved by the server; the local pending change is rejected
  visibly.
- Clear user data on sign-out/tenant switch.

## 9. Jetpack Compose

- **State hoisting:** a composable takes a value and emits an event; the
  ViewModel owns the state.
- **`UiState` is immutable**; use `remember`/`derivedStateOf` only for UI-local
  state.
- **Keys and `contentType`** on lazy lists; never index keys for reorderable
  data.
- **Stability matters:** an unstable parameter re-renders the subtree; use
  immutable data classes and `Immutable`/`Stable` annotations deliberately.
- **Side effects:** `LaunchedEffect` for a keyed side effect, `DisposableEffect`
  for cleanup, `rememberCoroutineScope` for user-triggered work. No business
  logic in a composable.
- **Accessibility:** `contentDescription`, `semantics`, touch targets, focus
  order; test with TalkBack.
- **Images: Coil 3** with explicit sizes; a full-resolution bitmap on a
  low-end device is a memory bug.

## 10. Push notifications (FCM)

- **FCM HTTP v1** from the backend; the client gets a token and sends it to the
  server (register on login, refresh on change, revoke on logout).
- `POST_NOTIFICATIONS` runtime permission on API 33+; a primer explains the
  value before the OS prompt.
- **Payload is minimal:** `{ v, type, id, route }`; no personal data, no full
  resource, no action.
- The tap routes through the same parser as a deep link; the screen fetches
  the resource.
- Channels and importance are declared; the notification is not the data path.
- Handle foreground, background and cold start; a deleted resource shows
  not-found.

## 11. Security

- **Secrets in Keystore + Tink**, never in `SharedPreferences`, Room, logs or
  the APK.
- Auth: Credential Manager (passkeys/passwords/Google) or AppAuth + Custom
  Tabs with PKCE; tokens encrypted with Keystore.
- **The server is authoritative** for game state, prices, payments and
  entitlements.
- Root/emulator is a risk signal evaluated server-side, never a client block.
- No remote code; no `eval`-style dynamic loading of untrusted code.
- Play Integrity where the product needs device attestation; the verdict is a
  server-side signal.
- Certificate pinning only with a rotation plan.

## 12. Performance

- **Baseline Profile + Startup Profile** in release; CI runs a macrobenchmark
  and fails on a regression.
- **R8 full mode**, resource shrinking, no unused native libs.
- **Startup:** lazy-init, no heavy work in `Application.onCreate`, App
  Startup for ordered init.
- **Frame time:** no main-thread I/O, no unstable recomposition, no
  full-resolution images; JankStats in the field.
- **Memory:** explicit image sizes, bounded caches, `ApplicationExitInfo` for
  OOM evidence.
- **One variant, one binary:** no game engine in the market app.

## 13. Testing

- **Unit:** domain rules, ViewModels with `kotlinx-coroutines-test` and fake
  ports; no Android framework where possible.
- **Compose UI:** `createComposeRule`, semantics-first queries; one test per
  state (loading, empty, error, forbidden, offline, ready).
- **Room:** in-memory DB and migration tests with fixtures for every released
  version.
- **WorkManager:** `WorkManagerTestInitHelper`; idempotency and retry.
- **Macrobenchmark:** startup and scroll on the Low device pool; a regression
  fails CI.
- **Device lab:** Low, mid and flagship; StrictMode clean.

## 14. Cross-skill contract

- `android-review` reviews a change and ranks a broken architecture contract
  above every other finding.
- `android-port` ports a React Native, web, Java/XML Views or legacy screen to
  Kotlin + Compose with the same behavior and tests first.
- The `lints/` config is the machine-checkable half. When this skill and the
  config disagree, the config wins and this file is fixed.

## Sources

- `references/android.md` — the platform digest: API levels, Compose,
  coroutines, Room, WorkManager, FCM, baseline profiles, R8.
- `references/compose-patterns.md` — UDF, state hoisting, lazy lists, effects,
  navigation.
- `references/android-architecture.md` — Gradle modules, layers, ports and the
  enforcement map.
- `references/kotlin-strict.md` — Kotlin 2.x, nullability, sealed states,
  `Result`, coroutines and detekt.
