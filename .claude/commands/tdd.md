---
name: tdd
description: "Delegate to the tdd skill: drive an <app> change test-first through red, green, refactor, with integration tests at the agreed seams."
division: testing
---

# /tdd

## Delegate

Delegate to the **tdd** skill. Activate when an `<app>` feature or bug fix is
to be built test-first, when the request mentions red, green, refactor, or
when integration tests are wanted at a public interface.

Steps:

1. Write down the seams under test and confirm them with the requester
   before writing a single test.
2. Run one vertical slice at a time: the failing test first, then only enough
   code to make it pass.
3. Keep the suite green at the end of every slice; a red test left in the tree
   is a broken contract.
4. Add the integration test that exercises the real boundary, with fixtures
   explicit about which layer they stand in for.
5. Run `./gradlew test`, `./gradlew :evals:test` and
   `bash scripts/check-architecture.sh`, and report the real output.
