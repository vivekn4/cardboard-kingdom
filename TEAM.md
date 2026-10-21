# CARDBOARD KINGDOM — TEAM

The crew that builds and releases the game. Modeled on the channel
review-crew system: nothing ships without a greenlight.

## ROSTER (7 roles)

| Role | Owns | Runs as |
|---|---|---|
| **Director (Squirt)** | Vision, final calls, scope, tone (empathy rule) | Main agent |
| **Systems Designer** | Survival math, skills/perks, economy, physics tuning | Subagent per milestone |
| **Narrative Lead** | Story acts, missions, dialogue, narration, diary | Subagent per milestone |
| **Art Director** | Sprite sheets, character design, world dressing, UI | Subagent per milestone |
| **World Builder** | Districts, interactables, schedules, events, festivals | Subagent per milestone |
| **QA / Physics Tester** | Playtests, bug hunts, balance, soak tests, device tests | Subagent per milestone |
| **Release Manager** | Builds, CI, store page, screenshots, changelog, trailer | Main agent + browser tasks |

## GREENLIGHT RULE (adapted from the channels)

- Every phase gate (v0.2 → v1.0) needs a **unanimous GREEN** from the
  crew roles assigned to that phase. One RED blocks release → fix →
  re-vote (max 2 re-votes, then escalate to the user).
- A vote record is logged per phase in `docs/votes/`.
- The user is Executive Producer: escalations, scope cuts, and the
  final v1.0 ship call are theirs.

## SPRINT CADENCE

- Sprint 1 (current): research done → GDD v2 → Godot scaffold →
  character design sheets → interactables catalog.
- Each sprint ends with a playable build + QA note, even if small.
- Stand-up format per sprint: what shipped, what broke, what's next.

## WORKING RULES

- Free tools only. Offline-first. No mocking tone, ever.
- Never describe incomplete work as playable or complete.
- Every claim of "done" needs a receipt: build log, test result, or
  screenshot.
