# Tests

Everything here runs with stock Python 3 — no Android SDK needed.

## Game math tests (58 cases, all green ✅)

`game_math_tests.py` ports `GameState.kt`'s rules **exactly** (same
constants, same order: drains → health drains → clock advance → day
rollover; same sleep/eat/dumpster/kitchen/shelter values and clamps):

| Suite | What it checks |
|---|---|
| 1. Hourly drains | winter-day rates at 8 AM |
| 2. Winter night | night rate 5/hr, 5 AM still night, 6 AM day rate |
| 3. Mild season | lower warm/cold drains |
| 4. Clamping | stats floor at 0, cap at 100 |
| 5. Health stacking | starving+parched+frozen+low = 17/hr |
| 6. Game over | at 0 HP, no further ticks |
| 7. Day rollover | midnight → day+1, dumpster reset |
| 8. Sleep | camp vs rough restores, caps, costs |
| 9. Eat/dumpster/kitchen/shelter | daily limits, costs |
| 10. Movement | 0.3/tile, blocked at 0 energy |
| 11. Winter-night worst case | 12h from full warmth → 54 left, alive; from 30 → 0 + frostbite |
| 12. 3-day loop | meals + camp sleep keeps you alive |

Run: `python3 tests/game_math_tests.py` (exit 0 = all green).

## What these tests do NOT cover

Native compilation (needs Android SDK — GitHub Actions covers this on
push) and on-device playtesting (touch feel, frame rate, real save/load).
