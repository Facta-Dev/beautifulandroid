---
name: "domain-modeling"
description: "Sharpens the <app> domain model: challenges conflicting and fuzzy terms, resolves them into the project glossary, and records an ADR only for decisions that are hard to reverse."
division: core
---

# <app> Domain Modeling skill

> Reusable workflow for changing the <app> domain model, not just reading it.
> An agent invokes this skill; the requester supplies the vocabulary, the skill
> supplies the discipline.

## When to Activate

Activate this skill when a term in the request is ambiguous, overloaded, or
already means something else in the codebase, when the requester describes
behaviour the code contradicts, when a glossary entry needs writing,
correcting, or splitting, or when a decision was made that a future reader
would puzzle over.

Do NOT activate this skill when:

- The task is only to read the existing vocabulary (one read of the glossary
  is enough).
- The task is to add or change a test (use `tdd`).
- The task is to review a diff (use `review`).

## Core Principles

- **One word per concept.** Pick the best name and list the rejected ones under `_Avoid_`, so the vocabulary does not fork.
- **The glossary holds domain language and nothing else.** No implementation detail, no spec, no scratch pad.
- **Definitions say what a thing IS, not what it does.** One or two sentences.
- **Only context-specific terms belong.** Generic programming vocabulary (timeouts, error types, util patterns) stays out even when the project uses it heavily.
- **The code is evidence.** When the requester describes behaviour, check the code and surface the contradiction instead of silently picking a side.
- **Stress-test relationships with concrete scenarios.** Edge cases force the boundaries between concepts to be stated.
- **ADRs are offered sparingly.** Most decisions are not worth a file.
- **Files are created lazily.** The glossary appears with the first resolved term, `docs/adr/` with the first ADR.

## Rules

- Update the glossary inline as terms resolve. Do not batch the writes to the end of the session.
- Call out a conflict with the existing glossary immediately: name the existing definition and the new meaning, then ask which one is intended.
- For a fuzzy or overloaded term, propose one canonical term and its `_Avoid_` list rather than leaving both words alive.
- A single-context project keeps one glossary file at the root. A multi-context project keeps a `GLOSSARY-MAP.md` at the root pointing at each context's glossary and ADR folder, plus the relationships between contexts.
- Write an ADR only when all three hold: hard to reverse, surprising without context, and the result of a real trade-off. If one is missing, skip the ADR and say which condition failed.
- An ADR is one short paragraph: context, decision, reason. Optional sections (status, considered options, consequences) only when they earn their place. Number sequentially as `docs/adr/NNNN-slug.md`.
- No real secret, token, customer identifier or personal name in a glossary or an ADR. Describe the shape, not the value.
- No fabricated vocabulary: never invent a term the requester did not use and the code does not support.

## Research References

| Claim | Source | Year | Link |
|-------|--------|------|------|
| Glossary terms carry an `_Avoid_` list and define what a thing IS | mattpocock/skills, skills/engineering/domain-modeling/GLOSSARY-FORMAT.md | 2026 | skills/engineering/domain-modeling/GLOSSARY-FORMAT.md |
| An ADR is warranted only when hard to reverse, surprising, and a real trade-off | mattpocock/skills, skills/engineering/domain-modeling/ADR-FORMAT.md | 2026 | skills/engineering/domain-modeling/ADR-FORMAT.md |
| The active discipline: challenge terms, sharpen language, cross-reference the code | mattpocock/skills, skills/engineering/domain-modeling/SKILL.md | 2026 | skills/engineering/domain-modeling/SKILL.md |
| The vocabulary this template already defines, and the decisions already recorded | `docs/14-glossary.md`, `docs/adr/` | - | `docs/14-glossary.md` |
| The module boundaries the terms must respect | `PLAYBOOK_MOBILE_ANDROID_KOTLIN.md` §4 | - | `../PLAYBOOK_MOBILE_ANDROID_KOTLIN.md` |

## Output Format

Deliver, in order:

1. The resolved terms: canonical term, its definition, and the `_Avoid_` list.
2. The glossary update, written inline (the existing glossary file, or a new one when the first term resolves).
3. The ADR, or the reason none was written (which of the three conditions was missing).
4. Open questions: terms that are still ambiguous and need a decision from the requester.

Plain text, in this shape:

```
domain-modeling: "<term>" is now canonical for <thing>; "Avoid: <rejected>".
<second term> stays unresolved: <why>. ADR skipped: <the condition that failed>.
```
