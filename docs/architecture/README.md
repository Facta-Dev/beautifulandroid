# beautifulandroid — Architecture Diagrams

Rendered with [Archify](https://github.com/tt-a1i/archify). Each entry is a standalone,
explorable HTML page (inline SVG, dark/light themes, live motion) paired with its source
JSON candidate. The set documents the native Android stack this repo teaches: Kotlin +
Jetpack Compose on a Gradle module graph (`:feature:<slice>:ui` → `:core:<slice>:domain` →
ports, implemented by `:core:<slice>:data` and `:core:platform`), UDF state and events,
Room + DataStore + Keystore/Tink, the Room outbox drained by WorkManager, Navigation 3,
FCM, baseline profiles and R8 — with the three skills, the detekt/ktlint config and the CI
gates as the delivery mechanism.

| Diagram | Type | What it shows | JSON | HTML |
|---|---|---|---|---|
| c4-containers | architecture | Container view: the device user → `:apps:<variant>` → `:feature:<slice>:ui` → the domain, data and platform modules → the Backend API, with the Keystore credential plane, the WorkManager drain and the FCM channel as the systems around them | [json](c4-containers.json) | [html](c4-containers.html) |
| services-slices | architecture | Module topology: the composition root, the feature slice, the pure-Kotlin domain, the data adapters and the platform adapters — with the dependency walls named (`domain` has no `android.*`, a feature never imports another feature's internals, the app module holds no business logic) and each wall labeled with the gate that enforces it | [json](services-slices.architecture.json) | [html](services-slices.architecture.html) |
| auth-algorithm | workflow | The credential and trust algorithm as gated steps: Credential Manager or AppAuth + Custom Tabs with PKCE → token exchange → session gate → Keystore + Tink seal → server-side entitlement, with the sign-out wipe and the Play Integrity verdict beside it | [json](auth-algorithm.workflow.json) | [html](auth-algorithm.workflow.html) |
| outbox-algorithm | workflow | The offline path: the user action commits the entity and the Room outbox row together, WorkManager drains the row with an idempotency key, the server answers, and a conflict lands in the dead-letter view | [json](outbox-algorithm.workflow.json) | [html](outbox-algorithm.workflow.html) |
| auth-sequence | sequence | Full sign-in chain across the app, Credential Manager, Keystore + Tink, the Backend API and Play Integrity — one exchange, one seal, one entitlement — then the sign-out revoke and wipe | [json](auth-sequence.sequence.json) | [html](auth-sequence.sequence.html) |
| outbox-sequence | sequence | The WorkManager worker's message trace: the Room transaction, the unique work, the pending rows, the POST with the idempotency key, the settle, the backoff and the dead-letter row the user finally sees | [json](outbox-sequence.sequence.json) | [html](outbox-sequence.sequence.html) |
| database-dataflow | dataflow | Where state actually goes: the sealed `UiState` reads Room and writes the outbox row, the token comes from Keystore, WorkManager posts pending rows to the API and settles them | [json](database-dataflow.dataflow.json) | [html](database-dataflow.dataflow.html) |
| runtime-lifecycle | lifecycle | App runtime states: `Application.onCreate` → ordered init → the Navigation 3 host → the ViewModel → steady state, with the offline/forbidden gate, the WorkManager pause, the ANR ending, process death and the push cold start | [json](runtime-lifecycle.lifecycle.json) | [html](runtime-lifecycle.lifecycle.html) |

Authored with Archify, sibling set of the facta backend template. The repository content
is grounded in the architecture contract in the README and in the digests the skills carry
(`skills/android-best-practices/references/android.md`, `android-architecture.md`,
`compose-patterns.md`, `kotlin-strict.md`).

**Adapted roles.** Two of the eight roles keep the file name for series consistency but
document the Android mechanism instead of a backend fiction: `auth-*` is the **credential
and trust boundary** — Credential Manager or AppAuth + Custom Tabs with PKCE, the token
sealed by Keystore + Tink, and a server that stays authoritative — not an auth server, which
does not exist in this repository; `outbox-*` is the **Room outbox drained by WorkManager**,
not a transactional-outbox broker. No database server, message broker, mail vendor or auth
server is part of this repository, and the diagrams do not invent one: the only remote
system shown is the Backend API the app talks to.
