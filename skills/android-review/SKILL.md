---
name: android-review
description: >-
  Review a native Android change — a Compose screen, a ViewModel with UDF, a
  Gradle module boundary, a Room/WorkManager path, a deep link, FCM handling,
  a coroutine/dispatcher change, or a diff — and produce a prioritized report
  where every finding cites the file and line, the rule it breaks, and the
  smallest fix. Use this skill whenever the user asks to review, audit,
  critique, or "find problems in" an Android app, screen, ViewModel, or
  module graph. Trigger on review, audit, code review, PR review, "what is
  wrong with this screen", "check this ViewModel", "review this module", or a
  pasted .kt file. Architecture and main-thread contracts always rank first.
---

# Android review

A review is a prioritized list of findings, not a rewrite. Read the whole
change first. Every finding has four parts: **location** (file and line),
**severity**, **the rule it breaks** (named or quoted), and **the smallest
fix**. If you cannot cite a rule or a user-visible consequence, it is not a
finding; it is a preference for the Notes section, if anywhere.

## 1. Report shape

```text
## Verdict
<one paragraph: what this change does, whether it is safe to ship, and the
single most important thing to fix>

## Must fix
1. <file>:<line> — <finding>. Rule: <rule or contract>. Fix: <smallest fix>.

## Should fix
...

## Notes
...

## Waived gates
...
```

- **Must fix** blocks merge or release: a main-thread block, a broken module
  boundary, a missing state, a token outside the Keystore, an outbox that
  loses a mutation, a deep link that navigates unvalidated.
- **Should fix** is worth doing in this change or the next: an unstable
  Compose parameter, a missing baseline-profile benchmark, a slow startup.
- **Notes** is optional. Do not pad the report to look thorough.

## 2. Categories, in ranking order

### Architecture contracts (always Must fix, ranked first)

A broken architecture contract (ranked above everything) is a Must fix even
when the screen works. Check:

- A **domain module** imports `android.*`, Compose, Room or Retrofit. Domain is
  pure Kotlin; the Gradle graph must enforce it.
- A **composable** calls a repository/SDK, contains a business rule, or holds
  a `Context`.
- A **ViewModel** holds an Android `Context`/View, or the state is not a single
  immutable `UiState`.
- A feature imports another feature's internals, or the app module contains
  business logic.
- One variant reaches another variant's engine/permissions.
- A client-side permission check presented as access control.

### Main thread and coroutines

- A blocking disk/network/parse/crypto call on `Dispatchers.Main`; a
  `runBlocking` in production; a hard-coded `Dispatchers.IO` in a ViewModel.
- A `GlobalScope` coroutine; a scope that outlives its screen.
- A `Flow` collected without lifecycle (`collectAsStateWithLifecycle` missing);
  a state mutation from a background thread.
- `StrictMode` violations treated as noise.
- A long task not moved to WorkManager/a background dispatcher.
- An exception that escapes a coroutine scope and crashes the app.

### UDF and Compose

- `UiState` mutated in place; independent booleans instead of one sealed
  state.
- A composable that owns business state instead of hoisting it.
- A lazy list without keys/`contentType`; an index key on reorderable data.
- An unstable parameter causing a full subtree recomposition; `remember` used
  to hide a state bug.
- Business logic in a composable; `LaunchedEffect` used for an event that
  belongs in the ViewModel.
- Accessibility: missing `contentDescription`/semantics; small touch targets;
  focus order broken.

### Data, offline and outbox

- A Room query on the main thread; a DAO without `suspend`/`Flow`.
- A migration without fixtures; a destructive fallback that loses data.
- An outbox that is not persisted, not idempotent, or not drained by
  WorkManager on reconnect/restart.
- An optimistic update with no rollback; a conflict silently overwritten.
- Paging without a stable key or a bounded page.
- A token or secret in Room/DataStore instead of the Keystore.

### Navigation and deep links

- A deep link parsed without an allowlist/type/size validation; unknown URL
  still navigates.
- A link that performs a state-changing action.
- A signed-out link dropped or replayed blindly.
- A second parser for push; a route not in the typed `NavKey` table.
- A route guard presented as server authorization.

### Push

- A payload with personal data or a full resource; an action in the payload.
- A tap that does not use the deep-link parser; foreground/background/cold
  start handled differently.
- `POST_NOTIFICATIONS` not requested (API 33+) or a denied permission treated
  as a crash.
- Token not refreshed/revoked; server not deduping tokens.

### Security

- A secret in `SharedPreferences`, Room, logs, the APK or a crash report.
- A client-side entitlement/payment decision; the server must be
  authoritative.
- An auth flow in a WebView instead of Custom Tabs + PKCE.
- A deep link/FCM payload trusted without validation.
- Root/emulator as a client-side block; certificate pinning without rotation.
- Play Integrity used as the only authorization decision.

### Performance

- A main-thread block; a heavy `Application.onCreate`; no App Startup.
- A missing baseline/startup profile; R8 full mode off; a macrobenchmark
  missing from CI.
- A full-resolution image or an unbounded cache.
- An unstable recomposition or a per-frame allocation.
- A game engine/permissions in the market variant.

### Tests

- No ViewModel test with `kotlinx-coroutines-test` and fake ports.
- No Compose test per state; no Room migration fixtures; no WorkManager
  idempotency test.
- No macrobenchmark on the Low pool; no device-lab run.
- A test that asserts implementation instead of behavior.

## 3. Waived gates

If the diff deletes or weakens a lint, a test, a type, a budget or an
accessibility check, flag the deletion itself, loudly — even when the new code
looks fine. Look for:

- A new `@Suppress` **without a reason**; a module/file-wide suppression; a
  detekt rule disabled or a baseline file grown without review.
- A Gradle dependency added to cross a module boundary; a `friend` path or an
  `api`/`implementation` swap that exposes internals.
- A skipped test without an owner and expiry; a migration fixture deleted; a
  macrobenchmark budget raised without a device-lab run.
- A `minSdk`/`targetSdk` change without the Play policy check; a baseline
  profile removed.

State the deletion, the reason it claims, and what it would take to keep the
gate. "Temporarily" is not a reason; an expiring waiver with an owner is.

## 4. How to review

1. **Read the change twice.** First for intent, then for the diff.
2. **Run the gates if you can:** `./gradlew test`, `./gradlew detekt`,
   `./gradlew lint`, a release build with the baseline profile, a Low-tier
   run.
3. **Check the main thread and the module graph first.** They are the
   expensive findings; style is cheap.
4. **Cite evidence.** File and line for every Must fix; a rule name or a
   quoted contract for every finding.
5. **Propose the smallest fix.** "Rewrite the screen" is not a fix; "move the
   Room query to `Dispatchers.IO` via the injected dispatcher and expose a
   `Flow`" is.
6. **Stop when the change is safe.** An empty Should-fix section is a valid
   review.

## 5. Worked example

`examples/review-inventory.md` is a full review of a small Compose inventory
screen: the blocking ViewModel, the state and list issues, the findings in
ranking order, and the fixed module. It shows the main-thread violation first,
then the UDF/state bug, then the list/accessibility gap.

## 6. Cross-skill contract

- `android-best-practices` defines the rules this review cites. When a finding
  and the skill disagree, the skill is the source.
- `android-port` produces the change; this review checks it before release.
- The `lints/` config is the machine-checkable subset. A finding the config
  can catch should become a rule, not a repeated comment.
