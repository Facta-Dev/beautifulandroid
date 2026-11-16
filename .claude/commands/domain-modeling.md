---
name: domain-modeling
description: "Delegate to the domain-modeling skill: sharpen the <app> domain model by challenging fuzzy terms, resolving them into the project glossary, and recording an ADR only for hard-to-reverse decisions."
division: core
---

# /domain-modeling

## Delegate

Delegate to the **domain-modeling** skill. Activate when a term in the request
is ambiguous, overloaded, or already means something else in `<app>`, when the
requester describes behaviour the code contradicts, when a glossary entry
needs writing, or when a decision was made that a future reader would puzzle
over.

Steps:

1. Read the existing glossary and the ADRs for the area before proposing
   anything.
2. Challenge the terms: name the conflict with the existing definition, or
   propose one canonical term with an `_Avoid_` list for the rejected ones.
3. Stress-test the relationships with concrete edge-case scenarios.
4. Cross-reference the code and surface any contradiction instead of picking
   a side silently.
5. Update the glossary inline as terms resolve.
6. Write an ADR only when the decision is hard to reverse, surprising without
   context, and the result of a real trade-off; otherwise say which condition
   failed.
