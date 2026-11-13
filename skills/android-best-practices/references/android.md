# Android platform digest

The platform facts a native Android app depends on, with the design
consequence. The Android developer documentation is the source.

## 1. Versions and policy

- **API 37 (Android 17)** is current; Play requires `targetSdk 36` for new
  apps/updates since 31 Aug 2026; `compileSdk 37` with AGP 9.1.1+ matches
  Compose 1.12 (verify the current Play rule).
- `minSdk 24` is the AndroidX floor; raise it only with fleet data.
- Kotlin 2.x (K2), coroutines + Flow, kotlinx.serialization.
- R8 full mode + Baseline Profile + Startup Profile in release builds.
- Native `.so` files 16 KB page-size aligned (verify the enforcement date).

## 2. UI

- Jetpack Compose + Material 3 is the default; Views only for engine surfaces
  (`SurfaceView`, `AndroidView`).
- Navigation 3 (typed `NavKey`, state you own) is stable.
- Lazy lists with keys and `contentType`; Paging 3 for feeds.
- Coil 3 for images with explicit sizes.

## 3. Concurrency

- Coroutines with **injected dispatchers**; no `Dispatchers.IO` hard-coded in
  a ViewModel.
- Structured concurrency; no `GlobalScope`; cancellation propagates.
- `StateFlow<UiState>` down, `UiEvent` up, one-shot effects on a `SharedFlow`.
- `collectAsStateWithLifecycle` for Compose.
- StrictMode in debug; main-thread disk/network is a bug.
- WorkManager for deferrable guaranteed work; a foreground service only where
  policy allows.

## 4. Data

- **Room** (SQLite, WAL) for structured data; `suspend`/`Flow` DAOs.
- **DataStore** for preferences; **Keystore + Tink** for secrets.
- Migrations are versioned and tested against fixtures of every release.
- An outbox in Room, drained by WorkManager with an idempotency key.
- Paging 3 for network/DB feeds; a stable key and a bounded page.

## 5. Push

- FCM HTTP v1 from the backend; the client sends the token to the server.
- `POST_NOTIFICATIONS` runtime permission on API 33+; a primer before the OS
  prompt.
- Minimal payload `{ v, type, id, route }`; the tap uses the deep-link parser.
- Channels/importance declared; foreground/background/cold start handled.
- Token lifecycle: register on login, refresh, revoke on logout.

## 6. Navigation and deep links

- Navigation 3 with typed `NavKey`; the route table mirrors the URL playbook.
- Deep links: allowlist, type/size validation, unknown → not-found.
- A signed-out link is queued and resumed after login.
- No state-changing action from a link; push uses the same parser.
- Authorization is server-side.

## 7. Device tiers

- Tier from RAM/cores/GPU/API/benchmark; cache it.
- Budgets per tier: cold start, first frame, scroll FPS, memory, 3D frame
  time; calibrate on the Low pool.
- A runtime quality governor lowers fidelity only, never logic.
- The low-end path is first-class and tested in CI.

## 8. Performance

- Baseline + Startup Profiles, R8 full mode, resource shrinking.
- No heavy `Application.onCreate`; App Startup for ordered init.
- No main-thread I/O; no unstable recomposition; no full-resolution images.
- Macrobenchmark in CI on the Low pool; JankStats in the field.
- `ApplicationExitInfo` for OOM/crash evidence.

## 9. Release

- AAB, Play App Signing, staged rollouts, Play Feature/Asset Delivery.
- One variant = one application module = one binary.
- Permissions/native code scoped per variant.
- Play Integrity is a server-side risk signal, not an authorization decision.

## 10. Testing

- Unit: domain/ViewModel with `kotlinx-coroutines-test`, fake ports.
- Compose UI: semantics-first, one test per state.
- Room: in-memory + migration fixtures.
- WorkManager: `WorkManagerTestInitHelper`, idempotency/retry.
- Macrobenchmark: startup/scroll on the Low pool.
- Device lab: Low/mid/flagship; StrictMode clean.
