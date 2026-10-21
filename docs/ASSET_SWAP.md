# Asset swap plan (greybox → real sprites)

M0 draws everything as coloured blocks in `GameView.drawWorld()`. This
document maps each greybox block to its real sprite-pack replacement for
the M1 art pass. One palette pass (re-tint to the dusk palette) runs over
all packs first, so mixed sources read as one game.

## Tile mapping

| Greybox block | Real asset | Pack |
|---|---|---|
| Street / sidewalk (dark grey) | Road + sidewalk tiles | Kenney Tiny City (CC0) |
| Building walls (brown) | Building exteriors, doors, windows | muchopixels City Street Tileset |
| Dumpster (green, "D") | Dumpster, open/closed/full states | BloodyFish Pixel Streets (CC0 demo) |
| Soup kitchen (orange, "K") | Shop/storefront + interior | muchopixels City Street / MutterPixel Cozy Street |
| Shelter (blue, "S") | Civic building (police/fire-style) | Defected Pixel Artist Town Pack |
| Camp (brown, "C") | Cardboard box, sleeping bag, tent | Custom (small, scoped) |
| Bench (grey, "P") | Bench, all directions | BloodyFish Pixel Streets |
| Lamp posts, signs, bus stop | Street furniture | Defected / muchopixels packs |

## Player mapping

The greybox block figure in `GameView` (coat + head + red beanie +
backpack) marks exactly where these layers go:

1. **Base:** Ruzty Survival Player Character (free base) — walk 4-dir,
   idle. 48×48 canvases, centred-bottom.
2. **Custom layer pass (~2–3h pixel work):** beanie, beard, backpack,
   cardboard sign, blanket roll, shopping cart (follows player).
3. **Custom animations:** sit, sleep, beg (kneel + sign), rummage,
   shiver (cold), wave/pitch.

## Pack credits (also in README)

- Kenney — Tiny City — CC0 (no attribution required; credited anyway)
- muchopixels — City Street Tileset Pack — see itch.io page for terms
- BloodyFish — Pixel Streets — CC0 (demo free; full pack $1)
- Defected Pixel Artist — Top-Down Town Pack — free on itch.io
- Ruzty — Survival Player Character — free base
- MutterPixel — Cozy Pixel City Street — itch.io
