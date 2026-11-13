# Changelog

All notable changes to this project are listed here. Versions follow
[Semantic Versioning](https://semver.org/), and the version lives in
`.claude-plugin/plugin.json`.

## [Unreleased]

### Added

- **Three skills for native Android.** `android-best-practices` steers new
  code toward a sacred main thread, Gradle modules that enforce the
  domain/data/ui/app layers, UDF with `UiState`/`UiEvent`/effects, Navigation
  3 and validated deep links, Room/DataStore and the WorkManager outbox, FCM,
  Keystore secrets, baseline/startup profiles and the low-end performance
  budget. `android-review` reviews a screen, ViewModel, module graph,
  Room/WorkManager path, deep link, FCM handler or diff and ranks architecture
  and main-thread contracts first. `android-port` ports a React Native, web,
  Java/XML Views or legacy screen to Kotlin + Compose.
- **A drop-in detekt and ktlint config.** `lints/detekt.yml` pins the
  coroutine, complexity, bug, naming, style and exception rules with zero
  issues; `lints/.editorconfig` is the ktlint formatting contract.
- **A Kotlin example and proof gates.** `examples/minimal/` is a Kotlin/JVM
  UDF ViewModel with JUnit tests; `scripts/check-build.sh` runs Gradle test +
  detekt when Gradle is installed. `scripts/check-lints.sh` runs detekt on the
  clean control and the violation harness, with a visible SKIP when the tool
  is absent.
- **A validator and a test suite.** `scripts/check-skill.py` checks
  frontmatter, links, eval cases and the plugin manifests; the offline
  `scripts/check-examples.py` balances every Kotlin block; `tests/` covers the
  validator, wording, lints config and gates.
- **A worked review.** `examples/review-inventory.md` shows the report shape
  and the architecture-first ranking on a small Compose inventory screen.

[Unreleased]: https://github.com/Facta-Dev/beautifulandroid/compare/v0.1.0...HEAD
