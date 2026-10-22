# QC REPORT — Cardboard Kingdom 3D (Godot)

Date: 2026-10-21. Method: scripted in-engine playtest (`CK_TEST`) +
rendered screenshots (`CK_SHOTS`, Xvfb + software GL) + code review.
Run the playtest yourself: `CK_TEST=1 godot --headless --path godot/`.

## Automated playtest: 20/20 PASS

Scripted sequence: spawn → dumpster → soup → bench → beg → talk to
June → camp sleep → save/load roundtrip → game over at 0 health.

```
PASS initial hunger 70
PASS initial day 1
PASS near dumpster
PASS dumpster uses 1
PASS hunger rose
PASS near soup (not talk)
PASS soup used
PASS hunger higher after soup
PASS near bench
PASS energy full after rest
PASS near beg spot
PASS money grew from begging
PASS near June talk spot
PASS June dialogue shown
PASS near camp
PASS day 2 after sleep
PASS stats full after sleep
PASS daily uses reset
PASS save/load roundtrip
PASS game over shows at 0 health
QC RESULT: 20 passed, 0 failed
```

## Bugs found and fixed during this pass

1. **Save/load was broken (real bug):** `_load_save()` called
   `FileAccess.get_as_utf8_string()`, which does not exist in Godot 4
   — loading a save crashed the load path. Fixed to read via
   `get_buffer().get_string_from_utf8()`. Caught by the roundtrip
   check, verified 20/20 after the fix.
2. **Soup was unreachable (real bug):** the soup-kitchen area and
   June's talk area were stacked on the same point, so the talk area
   always won and soup could never be used. Fixed with smaller
   (2.0 m) areas, talk spots moved 2 m off the service points, and
   nearest-area-wins resolution for overlaps (camp vs fire barrel
   still overlap and resolve correctly).
3. **HUD overlap:** stats text ran into the clock label
   ("Health 100Day 1"). Fixed: stats font 20, clock moved right.
4. **Sprites foreshortened at oblique angles:** tree/character quads
   did not face the camera. Fixed with cylindrical billboarding
   (sprites stay upright, rotate Y toward the camera) for trees,
   NPCs, and the player.
5. **Screenshot tour off-by-one:** async capture saved the NEXT
   view's frame. Fixed with synchronous capture; all 5 shots
   re-verified by eye.
6. **Camera snap:** tour teleports now snap the camera instead of
   waiting out the follow lerp.

## Screenshots (real renders, not mockups)

`godot/screenshots/`: spawn, camp, kitchen, reyes, town — 720x1280,
captured from the game with the Kenney sprite set installed.

## Known issues (not fixed in this pass)

- **Camera can enter buildings:** the follow camera has no collision
  avoidance; standing next to a tall building can put the camera
  inside it. Workaround: it recovers as soon as you walk away.
  Proper fix (camera raycast) is a v0.4 polish item.
- **NPCs are static:** they stand at fixed spots with one dialogue
  line each. Schedules, hearts, and quests arrive with the v0.2/v0.4
  systems work.
- **No Android export yet:** export templates are not installed on
  this machine. The Kotlin M0 APK remains the installable build
  until the Godot export is set up.
- **Xvfb screenshot warnings (benign):** V-Sync unsupported +
  dummy audio driver under software rendering; shots render fine.
