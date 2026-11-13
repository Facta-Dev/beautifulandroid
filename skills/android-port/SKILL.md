---
name: android-port
description: >-
  Port an existing UI to an idiomatic native Android app in Kotlin + Jetpack
  Compose with the same behavior: a React Native app, a web app, a Java/XML
  Views app, or a legacy screen. Use this skill whenever the user asks to
  migrate, convert, rewrite, or port an app or screen to native Android or
  Compose, modernize a Views app, or move a web/RN flow to Android. Trigger on
  port, migrate, convert to Kotlin, "turn this React Native app native",
  "move this XML screen to Compose", or a pasted Java/Kotlin/RN file. Port the
  original tests first, keep behavior identical, and write a PORTING.md.
---

# Android port

A port preserves behavior while changing the implementation. The output is not
a Views app wrapped in Compose or a web screen in a WebView; it is a native app
an Android developer would have written, with the same observable behavior and
tests proving it.

## 1. The order of work

1. **Read the source and its tests.** The tests are the specification. If
   there are none, write characterization tests against the current behavior
   first.
2. **Port the tests first.** Same cases, same assertions, expressed in JUnit +
   `kotlinx-coroutines-test` or Compose UI tests. A port without a failing test
   is a rewrite with hope.
3. **Map every screen, state, event, navigation path, network call and storage
   key.**
4. **Define the Gradle modules.** domain (pure Kotlin), data, feature ui, app;
   ports for platform capabilities.
5. **Port one screen at a time**, with the sealed `UiState` and the five
   states (loading, empty, error, forbidden, offline, ready).
6. **Run the gates.** `./gradlew test detekt lint`, a release build with the
   baseline profile, a Low-tier run.
7. **Write `PORTING.md`.** Every deliberate difference, every platform feature
   not reproduced, every permission/asset change, and why.

## 2. React Native → native Android

| RN | Android |
|---|---|
| `View`/`Text` | Compose `Box`/`Column`/`Row`/`Text` |
| `Pressable` | `Button`/`Modifier.clickable` with semantics |
| `FlatList`/`FlashList` | `LazyColumn` with keys + `contentType` |
| `expo-image` | Coil 3 with explicit sizes |
| `useState`/`useReducer` | `UiState` in the ViewModel |
| `useEffect` fetch | `viewModelScope` + a repository (Room/Flow) |
| MMKV/AsyncStorage | DataStore / Room |
| Keychain/Keystore | Android Keystore + Tink |
| Expo Router | Navigation 3 typed `NavKey` |
| expo-notifications | FCM + `POST_NOTIFICATIONS` |
| `fetch` in a screen | the SDK adapter behind a port |
| Hermes/JS thread | coroutines with injected dispatchers |

- **Keep the ViewModel contract, not the hook shape.** A hook becomes a
  ViewModel exposing `StateFlow<UiState>` and accepting `UiEvent`.
- **Keep the same route table**; deep links and push use one parser.
- **Move secure storage first**; a token in DataStore is a port that is not
  done.

## 3. Java/XML Views → Kotlin + Compose

- **Map the screen state and events**, not the view tree; the Compose screen
  renders the same states.
- **Replace `findViewById`/XML with composables**; hoist state to the
  ViewModel.
- **Replace `LiveData` with `StateFlow`**; collect with
  `collectAsStateWithLifecycle`.
- **Replace `AsyncTask`/callbacks with coroutines** and injected dispatchers.
- **Replace `startActivityForResult` with the Activity Result API**; deep
  links go through Navigation 3.
- **Keep accessibility parity:** content descriptions, touch targets, focus
  order.
- **Do not embed a WebView** to keep a web screen; port the screen.

## 4. Web → Android

- **Same SDK, different client.** Do not duplicate request logic; use the
  generated Kotlin SDK.
- **Same states** as text/Compose, not a webview.
- **Auth:** Credential Manager or AppAuth + Custom Tabs with PKCE; tokens in
  the Keystore.
- **Offline:** Room + the workmanager outbox; the browser's localStorage is
  not a mobile store.
- **Permissions:** runtime permissions with a primer; a denied permission is a
  state.

## 5. Behavior parity checklist

- [ ] Every screen, command, shortcut and deep link exists.
- [ ] Every state is rendered: loading, ready, empty, forbidden, not-found,
      offline, error, policy-blocked.
- [ ] Every network call has the same method, path, payload and headers.
- [ ] Every list uses a lazy list with keys and stable paging.
- [ ] The main thread never blocks; dispatchers are injected.
- [ ] Tokens are in the Keystore; no secret in Room/DataStore/logs.
- [ ] The outbox is persisted and drained by WorkManager.
- [ ] Accessibility semantics and dynamic type survive.
- [ ] The release build meets the startup/scroll budgets on the Low pool.
- [ ] The original tests pass, ported; new tests cover the Android-only paths.
- [ ] `PORTING.md` lists every deliberate difference.

## 6. What not to do

- **Do not keep the source framework's shape.** A React Native state tree in a
  ViewModel, or a web page in a WebView, is a port that will be rewritten.
- **Do not block the main thread** because the old app did; that is the bug
  the port exists to fix.
- **Do not skip the tests.** A port validated only by tapping through it ships
  the source's bugs plus new ones.
- **Do not ship a variant with another variant's engine/permissions.**
- **Do not silently change behavior.** Reproduce the source quirk, pin it with
  a test, and list the fix as a follow-up — or fix it deliberately and say so.

## 7. Cross-skill contract

- `android-best-practices` is the target style; every ported screen follows
  it.
- `android-review` reviews the port before release and ranks a broken
  architecture contract above every other finding.
- The `lints/` config is the machine-checkable half; a port that needs a lint
  exception is a port that has not finished.
