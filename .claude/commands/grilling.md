---
name: grilling
description: "Delegate to the grilling skill: stress-test an <app> plan round by round against a design tree, resolving assumptions, failure modes and scope before any code is written."
division: core
---

# /grilling

## Delegate

Delegate to the **grilling** skill. Activate when a plan, decision, or idea
for `<app>` has to be challenged before anyone implements it: the scope is
fuzzy, the assumptions are unwritten, the failure modes have not been named,
or the requester asks to be grilled.

Steps:

1. Map the plan as a design tree: every decision branches into the decisions
   that hang off it.
2. Compute the frontier, that is, the decisions whose prerequisites are
   already settled and that can be asked now.
3. Ask the whole frontier in one numbered round, each question carrying a
   recommended answer and worded so that "yes" accepts it.
4. Look up the facts yourself before asking; only the decisions go to the
   requester.
5. Recompute the frontier from the answers and run the next round, leaving
   any question whose answer depends on an open question for a later round.
6. Stop when the frontier is empty, then ask for the confirmation of shared
   understanding before anything is implemented.
