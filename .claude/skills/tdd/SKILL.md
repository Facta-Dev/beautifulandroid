---
name: "tdd"
description: "Drives an <app> change test-first: red, green, refactor, with integration tests at the agreed seams and the repository's own test tiers."
division: testing
---

# <app> TDD skill

> Reusable workflow for the red to green loop, and the reference that keeps the
> loop producing tests worth keeping. An agent invokes this skill; the agent
> supplies the behaviour, the skill supplies the loop.

## When to Activate

Activate this skill when an <app> feature or bug fix is to be built test-first,
when the request mentions red, green, refactor, or when integration tests are
wanted at a public interface.

Do NOT activate this skill when:

- The request is a pure documentation or comment edit.
- The request is to review existing tests (use `review`).
- The behaviour cannot be pinned down yet (grill the plan first, then drive the change).

## Core Principles

- **Red before green.** The failing test comes first, then only enough code to pass it.
- **One vertical slice at a time.** One seam, one test, one minimal implementation, repeat. Each test is a tracer bullet that responds to what the last cycle taught.
- **Tests verify behaviour through public interfaces**, never implementation details. The code can change entirely; the tests should not.
- **A good test reads like a specification.** "A user can check out with a valid cart" states the capability, not the mechanism.
- **Seams are agreed before the first test is written.** You cannot test everything, so agreeing the seams up front is how effort lands on the critical paths.
- **Expected values come from an independent source of truth:** a known-good literal, a worked example, the specification.
- **Refactoring belongs to the review stage**, not to the red to green implementation cycle.

## Rules

- Write down the seams under test and confirm them with the requester before writing any test. No test is written at an unconfirmed seam.
- Give each proposed seam a one-line note on what it catches and what it misses.
- Never anticipate future tests or add speculative features in the green step.
- Refactoring happens with the suite green, but it is never a step inside the red to green cycle.
- Do not mock your own classes or internal collaborators. Mock only at system boundaries: external APIs, the clock, randomness, the filesystem.
- Do not assert through a side channel (querying the database instead of using the interface). The tell: the test breaks on a refactor that changed no behaviour.
- Do not recompute the expected value the way the code computes it. That makes the test pass by construction, and it can never disagree with the code.
- Do not write all the tests first and then all the implementation. Horizontal slicing tests imagined behaviour and goes insensitive to real changes.
- Use the project vocabulary for test names: read the glossary if it exists, and respect the ADRs for the area under test.
- Integration tests exercise the real boundary: the wired adapter over fakes, or the instrumented suite, with fixtures explicit about which layer they stand in for.
- Never claim a test passes without running it, and never assert on a behaviour the test does not actually exercise.
- Leave the suite green at the end of every slice. A red test in the tree is a broken contract, not a to-do.
- The change is not done until `./gradlew test`, `./gradlew :evals:test` and `bash scripts/check-architecture.sh` are green, and the real output is reported.

## Research References

| Claim | Source | Year | Link |
|-------|--------|------|------|
| Red before green, one slice at a time, refactoring is not part of the loop | mattpocock/skills, skills/engineering/tdd/SKILL.md | 2026 | skills/engineering/tdd/SKILL.md |
| Good tests verify behaviour through public interfaces; tautological tests pass by construction | mattpocock/skills, skills/engineering/tdd/tests.md | 2026 | skills/engineering/tdd/tests.md |
| Mock only at system boundaries, and design for mockability at those boundaries | mattpocock/skills, skills/engineering/tdd/mocking.md | 2026 | skills/engineering/tdd/mocking.md |
| The test tiers of this template and the task that runs each one | `docs/12-testing-and-ci.md` | - | `docs/12-testing-and-ci.md` |
| Compose UI tests assert on semantics, not on the node tree | Android Developers, Compose testing | 2026 | https://developer.android.com/develop/ui/compose/testing |

## Output Format

Deliver, in order:

1. The seams under test, with what each catches and what it misses, and the confirmation that they were agreed.
2. The cycle log, one entry per slice: the failing test (and how it fails), the minimal implementation, the passing run.
3. The final state: the full test run result and the seam coverage note (what is not covered by design).

Plain text, in this shape:

```
tdd: seam = <the public interface> (catches: <x>, misses: <y>)
cycle 1: <test name> fails (<the failure>) -> added <the minimal code> -> passes
cycle 2: <test name> fails (<the failure>) -> added <the minimal code> -> passes
integration: <what runs against the real boundary>
suite: ./gradlew test -> <result>
```
