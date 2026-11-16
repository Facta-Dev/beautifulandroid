---
name: "diagnosing-bugs"
description: "Diagnoses a hard bug or performance regression in <app> with a red-capable feedback loop, a minimised repro, ranked falsifiable hypotheses, one-variable instrumentation, and a regression test at a correct seam."
division: testing
---

# <app> Diagnosing Bugs skill

> Reusable workflow for finding the cause of a hard bug or a performance
> regression. An agent invokes this skill; the requester supplies the symptom,
> the skill supplies the loop.

## When to Activate

Activate this skill when something in <app> is broken and the cause is not
already visible: a stack trace nobody read, a wrong value, a test that fails
for no clear reason, a screen that hangs, a request that takes too long, or a
regression between two known states whose bisection point is unknown.

Do NOT activate this skill when:

- The cause is known and only the change is missing (make the edit).
- The work is a new feature (use `plan`).
- The request is a diff review (use `review`).

## Core Principles

- **The feedback loop is the skill.** A tight pass/fail signal that goes red on this bug is what finds the cause. Reading code is not.
- **Red before theory.** No hypothesis before one command exists that can fail on this exact symptom, and that command has been run at least once.
- **Minimise, then hypothesise.** A minimal repro shrinks the suspect set and becomes the regression test.
- **Rank the hypotheses.** Three to five, each stating the prediction it makes. A hypothesis with no prediction is a vibe, not a cause.
- **One variable per probe.** Every probe maps to one stated prediction.
- **Fix the root cause at the shared seam.** Fix where all callers route through, not where the report happened to surface.
- **Measure first on performance work.** A baseline before a change, a bisection before a guess.
- **Redact before showing.** Secrets never enter the transcript.

## Rules

- The loop must be red-capable, deterministic, fast (seconds, not minutes) and runnable unattended. Show the invocation and its output before moving on.
- The loop asserts the reported symptom, not "it did not crash". A wrong bug is a wrong fix.
- A non-deterministic bug needs a higher reproduction rate, not a clean repro: repeat the trigger, narrow the timing window, add stress, run in parallel, until the failure is debuggable. A one-percent flake is not a loop.
- Minimise by cutting inputs, callers, configuration and data one at a time, re-running after each cut. Every remaining element must be load-bearing: removing any one of them turns the loop green.
- Show the ranked hypotheses to the requester before testing them. Continue with the ranking if nobody answers.
- Change one variable at a time. Never "log everything and grep".
- Tag every temporary log with a unique prefix, so the cleanup is one grep.
- Write the regression test before the fix, at a seam that exercises the real bug pattern as it occurs at the call site. If no correct seam exists, that absence is the finding: report it instead of shipping a shallow test that gives false confidence.
- After the fix, re-run the original, un-minimised scenario. Never claim a fix works without a passing run of the original loop.
- If no loop can be built, stop and say so: list what was tried and ask for access to a reproducing environment, a redacted capture, or permission to add temporary instrumentation. Do not hypothesise without a loop.
- Clean up before declaring done: remove the tagged instrumentation, delete the throwaway harnesses, and state the confirmed cause in the commit message.
- No fabricated output: every command shown was actually run, every value in it is real.

## Research References

| Claim | Source | Year | Link |
|-------|--------|------|------|
| The red loop: feedback loop, reproduce and minimise, ranked hypotheses, instrumentation, fix, cleanup | mattpocock/skills, skills/engineering/diagnosing-bugs/SKILL.md | 2026 | skills/engineering/diagnosing-bugs/SKILL.md |
| A regression test must exercise the real bug pattern, or it gives false confidence | mattpocock/skills, skills/engineering/diagnosing-bugs/SKILL.md | 2026 | skills/engineering/diagnosing-bugs/SKILL.md |
| The test tiers a regression test lands in, and the gate that runs them | `PLAYBOOK_TESTS.md` | - | `../PLAYBOOK_TESTS.md` |
| The local test tiers and the two unit-test tasks of this template | `docs/12-testing-and-ci.md` | - | `docs/12-testing-and-ci.md` |
| Compose UI tests assert on semantics, not on the node tree | Android Developers, Compose testing | 2026 | https://developer.android.com/develop/ui/compose/testing |

## Output Format

Deliver, in order:

1. The feedback loop: the exact command, its redacted output, and why it is red-capable for this symptom.
2. The minimised repro: the smallest scenario that still goes red, with the cut list.
3. The ranked hypotheses, each with the prediction it makes.
4. The probe results, one variable per probe, and which hypothesis was confirmed or eliminated.
5. The root cause in one sentence, then the fix.
6. The regression test at the chosen seam, or the missing-seam finding.
7. The cleanup status: original repro green, regression test green, instrumentation removed.

Plain text, in this shape:

```
diagnosing-bugs: loop = <one command>
(red: <the asserted symptom>) | hypothesis <n> confirmed | cause: <one sentence>
fix: <what changed> | regression test: <file>:<name> at seam <seam>
cleanup: instrumentation removed, original repro green
```
