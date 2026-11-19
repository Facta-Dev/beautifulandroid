<p align="center">
  <img src="assets/icon.png" alt="Beautiful Android: a green robot head on a dark field" width="160">
</p>

# Beautiful Android

Three standard [Agent Skills](https://agentskills.io) and a lints config that
teach coding agents to write, review, port, and enforce idiomatic
**native Android apps in Kotlin + Jetpack Compose**:

- **`android-best-practices`** loads whenever an agent writes or changes an
  Android app, and steers it toward a sacred main thread, a Gradle-module
  architecture and a low-end performance budget.
- **`android-review`** reviews existing code — a Compose screen, ViewModel,
  module graph, Room/WorkManager path, deep link, FCM handler, or diff — and
  writes a prioritized report where every finding cites the rule it breaks and
  shows the fix.
- **`android-port`** ports a React Native app, a web app, a Java/XML Views app,
  or a legacy screen to Kotlin + Compose with the same behavior, porting the
  original tests first.
- **`lints/`** is a drop-in detekt + ktlint unit that runs in CI.

They work with Claude Code, OpenAI Codex, GitHub Copilot, Cursor, Gemini CLI,
and any other agent that reads the Agent Skills format.

## The playbooks

These skills are the runnable, machine-checked enforcement of the
[facta playbooks](https://github.com/Facta-Dev/ai-coding-playbook). Start with
[`PLAYBOOK_MASTER.md`](https://github.com/Facta-Dev/ai-coding-playbook/blob/main/PLAYBOOK_MASTER.md) — the catalog, precedence and
cross-playbook invariants — then read
[`PLAYBOOK_MOBILE_ANDROID_KOTLIN.md`](https://github.com/Facta-Dev/ai-coding-playbook/blob/main/PLAYBOOK_MOBILE_ANDROID_KOTLIN.md) for this stack, and use this repository as
the worked example. The set has the master plus twenty platform playbooks,
split into `_PART<n>` files where needed; the repository also ships twelve
runnable kits (Go, Rust, Node.js, Python, Java, extension, desktop, React
Native, Android, iOS, frontend, CLI), the model-routing guide and the
instruction-budget tooling. The shared contracts (`PLAYBOOK_URLS.md`,
`PLAYBOOK_TESTS.md`, `PLAYBOOK_SECURITY.md`, `PLAYBOOK_AUDIT.md`,
`PLAYBOOK_CLAUDE_CODE.md`) apply across the series.

## Why

Android rewards discipline and punishes shortcuts, especially on the phones
most people own:

- A Room query or a network call on the main thread freezes the UI.
- A ViewModel that holds a `Context` leaks the Activity.
- A Compose state with three booleans renders impossible combinations.
- A deep link or FCM payload navigates without validation.
- A token in `SharedPreferences` is a breach on a shared device.
- A missing baseline profile makes a cold start feel broken on a low-end
  phone.
- A market app that carries a game engine is a download and review problem.

Beautiful Android is the contract that catches those before review.

## Agent kit (Claude Code)

This repo carries the `mobile-android` kit plane from
[`ai-coding-playbook`](https://github.com/Facta-Dev/ai-coding-playbook):
`.claude/agents`, `.claude/skills`, `.claude/commands`, and the
`model-routing:ollama` block in `CLAUDE.md`.

Verify or repair the wiring from the playbook checkout:

```
bash ai-coding-playbook/scripts/claude-kit/configure-repo.sh . --kit=mobile-android --check
```

`configure-repo.sh` is idempotent, never deletes, and backs up
`.claude/settings.json` under `.claude/backups/` before any merge.

## Install

### Claude Code

```bash
/plugin marketplace add Facta-Dev/beautifulandroid
/plugin install beautiful-android@beautiful-android
```

### OpenAI Codex and ChatGPT

The repository ships a portable `plugin.json` with the `com.openai` interface
block. Point Codex at the repository, or package it with:

```bash
python3 scripts/package-openai-plugin.py --out beautiful-android.zip
```

### Any agent

Copy `skills/android-best-practices`, `skills/android-review`, and
`skills/android-port` into your agent's skills directory. Each skill is a
`SKILL.md` plus a `references/` digest; no runtime, no network.

## A worked example

Without the skill, an agent writes a screen that blocks the main thread and
models the state with booleans:

```kotlin
// Without the skill: blocking I/O in the ViewModel, three booleans.
class InventoryViewModel(private val dao: InventoryDao) : ViewModel() {
    var loading by mutableStateOf(true)
    var error by mutableStateOf(false)
    var rows by mutableStateOf(emptyList<InventoryRow>())

    fun refresh() {
        runBlocking { rows = dao.all() }  // on the main thread
        loading = false
    }
}
```

Problems: `runBlocking` freezes the UI; `loading`, `error` and `rows` can
contradict each other; the ViewModel depends on a DAO instead of a use case.

With the skill, the ViewModel exposes one sealed state and a coroutine use
case:

```kotlin
// With the skill: one state, injected dispatcher, no blocking.
class InventoryViewModel(
    private val useCases: InventoryUseCases,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ViewModel() {
    private val mutableState = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state: StateFlow<InventoryState> = mutableState.asStateFlow()

    fun onEvent(event: InventoryEvent) {
        when (event) {
            InventoryEvent.Refresh -> refresh()
            is InventoryEvent.Archive -> archive(event.id)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            mutableState.value = withContext(io) { useCases.load() }
        }
    }
}
```

The module graph, the UDF contract and the enforcement map are in
[`skills/android-best-practices/references/android-architecture.md`](skills/android-best-practices/references/android-architecture.md).

## The architecture contract

```text
:feature:<slice>:ui  ──►  :core:<slice>:domain  ──►  ports
        │                        ▲
        └── UiState / UiEvent ───┘
:core:<slice>:data (SDK, Room, DataStore)  ──►  the ports
:apps:<variant> (composition root, nav, DI)
```

| Layer | Owns | Must not depend on |
|---|---|---|
| **domain** | Entities, rules, ports (pure Kotlin/JVM) | Android, Compose, Room, Hilt |
| **data** | SDK, Room, DataStore adapters | Compose, feature UI |
| **ui** | ViewModel, `UiState`, events, effects, composables | another feature's internals |
| **app** | Composition root, navigation host, DI | — |

The unbreakable rules:

1. The main thread never blocks.
2. Domain modules are pure Kotlin; Gradle enforces it.
3. UDF is the only presentation contract.
4. The ViewModel never holds a `Context`, a View or a composable.
5. The server is authoritative.
6. Every state is rendered, including offline and forbidden.

## Architecture diagrams

Beautiful Android ships eight [Archify](https://github.com/tt-a1i/archify)
diagrams under [`docs/architecture/`](docs/architecture/README.md). Each one
is a standalone HTML page you can open in a browser, paired with the JSON it
was rendered from.

| Diagram | Type | What it shows |
|---|---|---|
| [c4-containers](docs/architecture/c4-containers.html) | architecture | The app, the Gradle modules under it, the device stores and the systems around them |
| [services-slices](docs/architecture/services-slices.architecture.html) | architecture | The module topology and the dependency walls, each with the gate that enforces it |
| [auth-algorithm](docs/architecture/auth-algorithm.workflow.html) | workflow | Credential Manager or AppAuth + PKCE, the Keystore seal, the server-side gate |
| [outbox-algorithm](docs/architecture/outbox-algorithm.workflow.html) | workflow | The Room outbox row, the WorkManager drain, the server answer and the dead letter |
| [auth-sequence](docs/architecture/auth-sequence.sequence.html) | sequence | The full sign-in chain, then the sign-out revoke and wipe |
| [outbox-sequence](docs/architecture/outbox-sequence.sequence.html) | sequence | The write transaction, the drain, the settle and the state the user sees |
| [database-dataflow](docs/architecture/database-dataflow.dataflow.html) | dataflow | Where state goes: Room, the outbox table, Keystore, WorkManager and the API |
| [runtime-lifecycle](docs/architecture/runtime-lifecycle.lifecycle.html) | lifecycle | Boot, init, navigation, the ViewModel and steady state, with the gates and endings |

## The lints config

[`lints/detekt.yml`](lints/detekt.yml) and
[`lints/.editorconfig`](lints/.editorconfig) are copy-in units:

- **detekt:** coroutine misuse (`GlobalScope`, redundant suspend, hard-coded
  dispatchers), complexity, potential bugs, naming, style, exceptions, with
  warnings as errors and zero issues.
- **ktlint:** 4-space indent, 120 columns, trailing commas, no wildcard
  imports.

[`scripts/check-lints.sh`](scripts/check-lints.sh) runs detekt on the clean
control and the violation harness. A missing tool prints a visible `SKIP`; CI
installs it and runs the same script. [`lints/check/`](lints/README.md) holds
the deliberate findings.

The Kotlin example in [`examples/minimal/`](examples/minimal/build.gradle.kts)
is compiled and tested by [`scripts/check-build.sh`](scripts/check-build.sh)
when Gradle is installed; the offline
[`scripts/check-examples.py`](scripts/check-examples.py) balances every Kotlin
block everywhere.

## What it does not do

- The skills contain instructions and reference text only. They make no
  network calls and collect no data ([PRIVACY.md](PRIVACY.md)).
- When a Gradle/Kotlin toolchain is available, the skills run Gradle test,
  detekt, and lint; without one, they work from the code alone.
- The lint config is a copy-in unit, not a dependency.
- The skills do not replace an Android device lab or a Play review; they tell
  the agent what those must prove.

## Repository layout

```text
skills/
  android-best-practices/  SKILL.md + references/ (platform, Compose,
                           architecture, strict Kotlin)
  android-review/          SKILL.md (report format, ranking, waived gates)
  android-port/            SKILL.md (RN/web/Views → Compose)
lints/
  detekt.yml               the drop-in checks
  .editorconfig            the ktlint rules
  check/                   the violation harness + clean control
scripts/
  check-skill.py           frontmatter, links, evals, manifests
  check-examples.py        Kotlin block balance (offline gate)
  check-build.sh           Gradle test + detekt of the example
  check-lints.sh           detekt clean/violation harness (SKIP if absent)
  package-openai-plugin.py
tests/                     validator, wording, lints-config and gate tests
examples/
  minimal/                 a Kotlin/JVM UDF example + JUnit tests
  review-inventory.md      a full worked review
docs/
  architecture/            eight Archify diagrams (C4, modules, auth,
                           outbox, sequences, dataflow, lifecycle)
```

## Sources

- The Android developer documentation (Compose, coroutines, Room,
  WorkManager, Navigation 3, FCM, baseline profiles, R8).
- The facta Android handbook (`PLAYBOOK_MOBILE_ANDROID_KOTLIN.md`): Gradle
  modules, UDF, device tiers, offline/outbox, push, security, performance.
- The Kotlin documentation, detekt and ktlint docs.

## Contributing

Rule proposals are welcome; open an issue with the
[rule template](.github/ISSUE_TEMPLATE/propose-a-rule.yml) and a source. A
rule without a source and a failing example is a preference, not a rule. See
[CONTRIBUTING.md](CONTRIBUTING.md).

## Siblings

Beautiful Android is part of the Beautiful series for the facta playbooks:
[beautiful-j2ee](https://github.com/Facta-Dev/beautiful-j2ee),
[beautifulcli](https://github.com/Facta-Dev/beautifulcli),
[beautifulextension](https://github.com/Facta-Dev/beautifulextension),
[beautifulgo](https://github.com/Facta-Dev/beautifulgo),
[beautifulios](https://github.com/Facta-Dev/beautifulios),
[beautifulnodejs](https://github.com/Facta-Dev/beautifulnodejs),
[beautifulpython](https://github.com/Facta-Dev/beautifulpython),
[beautifulqt](https://github.com/Facta-Dev/beautifulqt),
[beautifulreact](https://github.com/Facta-Dev/beautifulreact),
[beautifulreactnative](https://github.com/Facta-Dev/beautifulreactnative),
[beautifulrust](https://github.com/Facta-Dev/beautifulrust).

## License

MIT — see [LICENSE](LICENSE).
