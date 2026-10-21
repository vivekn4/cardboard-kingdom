# Cardboard Kingdom 3D — Godot greybox (v0.1)

3D Frostgate built with Godot 4.7 (mobile renderer) + billboard
sprites. This is the Big Edition's playable foundation: the M0 loop
ported into 3D.

## Run it (desktop)

```bash
~/workspace/tools/godot/godot --path godot
```

Or open `godot/project.godot` in the Godot editor and press Play.

## Controls

- **WASD / arrow keys** — move. **E** or the INTERACT button — interact.
- **Touch** — left joystick to move, INTERACT button to act.
- Gold markers = interactable spots: dumpsters, soup kitchen, shelter,
  benches, camp, begging corner, busking spot, fire barrel, NPCs.

## What's in

- 3D world: river + bridge, camp, soup kitchen, shelter, pawn shop,
  warehouse, trees, pushable shopping cart (real physics).
- Billboard sprites: Rook, Mama June, Pigeon Pete, Officer Reyes, Caw
  (`sprites/make_sprites.py` generated the first greybox set; the real
  art comes from Kenney CC0 packs — see `../docs/ASSET_MAP.md` and
  regenerate with `tools/convert_sprites.py` after re-downloading the
  packs into `assets/_packs/`).
- Survival loop: hunger, thirst, warmth, energy, health, morale, money,
  respect; day/night clock (1 real second = 1 game minute); sleep at
  camp or rough; game over with the 211 line; JSON autosave.

## What's next (per GDD v2)

- v0.2: skills, clothing, cooking, hygiene, cart improvements.
- v0.4: 5 districts, NPC schedules, mission board, events.
- Android export: needs export templates + a debug keystore (later
  milestone; the Kotlin M0 APK remains the installable build for now).
