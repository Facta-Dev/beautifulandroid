---
name: "grilling"
description: "Grills an <app> plan, decision or idea before implementation: assumptions, failure modes and scope, asked round by round until nothing is silently assumed."
division: core
---

# <app> Grilling skill

> Reusable workflow for stress-testing thinking before code is written. An
> agent invokes this skill; the requester supplies the decisions, the skill
> supplies the interrogation.

## When to Activate

Activate this skill when a plan, decision, or idea for <app> needs to be
stressed before anyone implements it: the scope is fuzzy, the assumptions are
unwritten, the failure modes have not been named, or the requester asks to be
challenged, grilled, or poked at.

Do NOT activate this skill when:

- The plan is settled and the request is to execute it (use `plan`, then build).
- The request is a factual lookup with no decision to make.
- The request is to review an existing diff (use `review`).

## Core Principles

- **Map the subject as a design tree.** Every decision branches into the decisions that hang off it.
- **Work the tree in rounds.** The frontier is every decision whose prerequisites are already settled, that is, the questions answerable now without guessing at answers not yet heard.
- **Ask the whole frontier in one round.** Each question carries a recommended answer, and is worded so that "yes" accepts that recommendation.
- **Each round of answers reshapes the tree.** Settled decisions push the frontier outward and unblock dependent questions. Recompute and ask again.
- **Facts are the agent's job, never the requester's.** Look them up yourself.
- **Decisions are the requester's.** Put each to them and wait.
- **The session ends when the frontier is empty.** Every branch visited, nothing left silently assumed.

## Rules

- Never act on the plan until the requester confirms shared understanding.
- A question whose answer depends on another open question belongs to a later round, not this one.
- Look up facts yourself (the tree, the build, the tests, the existing code) before asking. Dispatch a background exploration with a fresh context window when the lookup is slow; do not block on it, only the questions downstream of it wait for the report.
- Every question states a recommended answer. A question with no recommendation pushes the work back onto the requester.
- Challenge assumptions, failure modes and scope explicitly, not politely. Ask what breaks, who is affected, and what is out of scope.
- Number every question in a round so the answers can be quoted back.
- Do not invent answers, and do not proceed on silence. Wait for the round.
- Keep rounds small enough to answer in one sitting. A wall of questions is a wall of silence.
- No fabricated facts: a claim about the codebase or the platform cites the file or the source that was read.

## Research References

| Claim | Source | Year | Link |
|-------|--------|------|------|
| The design tree, the frontier and the round format | mattpocock/skills, skills/productivity/grilling/SKILL.md | 2026 | skills/productivity/grilling/SKILL.md |
| Facts belong to the agent, decisions to the requester | mattpocock/skills, skills/productivity/grilling/SKILL.md | 2026 | skills/productivity/grilling/SKILL.md |
| The layer law that bounds the scope of a feature in this template | `PLAYBOOK_MOBILE_ANDROID_KOTLIN.md` §4 | - | `../PLAYBOOK_MOBILE_ANDROID_KOTLIN.md` |
| The offline, error and permission behaviour a plan has to settle | `docs/08-offline-outbox-and-sync.md` | - | `docs/08-offline-outbox-and-sync.md` |

## Output Format

Deliver, in order:

1. The design tree, as a short outline of the branches.
2. The rounds. Each round is a numbered set of questions, each with the recommended answer:

```
Q1 - Failure mode: what breaks first when the network is unavailable?
   Options: (a) block the screen, (b) show cached data, (c) queue the write.

   Recommended: (b) show cached data, because the outbox already records the
   write for later delivery.

Q2 - Scope: does this include the settings screen?
   Recommended: no, the write path and the outbox only.
```

3. The settled decisions and the remaining open branches.
4. The final confirmation request before implementation starts.

The session is done when the frontier is empty and the requester confirms
shared understanding.
