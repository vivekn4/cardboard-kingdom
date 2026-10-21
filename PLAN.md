# CARDBOARD KINGDOM — Homeless Simulator (Android)

**Working title:** Cardboard Kingdom
**Genre:** Top-down 2D survival sim (pixel art), single-player, offline
**Platform:** Android first (native Kotlin, same CI → APK pipeline as Doomscroll Guard)
**Price:** Free, no ads, no IAP (standing rule)
**Tone:** Played with empathy, not mockery. Funny where it can be (a talking
pigeon, a rival who steals your good bench), but the struggle is real: cold,
hunger, and dignity matter. The end screen lists real shelter resources.
**Setting:** A fictional cold-climate city ("Frostgate") — winter is a core
mechanic, not a backdrop. Inspired by Sudbury winters, not set in Sudbury.

---

## 1. The fantasy (one sentence)

Go from sleeping under a bridge with $0 and a shopping cart to either
getting back on your feet — or building the finest cardboard kingdom the
city has ever seen.

## 2. Core loop (60 seconds → 30 days)

1. **Morning:** Wake up cold and hungry. Check stats.
2. **Scavenge:** Search dumpsters, bins, and alleys for food, cans, scrap.
3. **Earn:** Beg at a busy corner, busk, or take an odd job (day labour,
   flyer handing, snow shovelling in winter).
4. **Spend smart:** Soup kitchen (free, limited), corner store, pawn shop,
   shelter bed (safe sleep, costs dignity/time, not money).
5. **Night:** Get back to camp before dark. Eat, sleep, stay warm.
6. **Repeat, improve:** Better gear → better camp → better options →
   story milestones → one of 5 endings.

## 3. Survival stats (0–100 unless noted)

| Stat | Drains from | Restored by | At 0 |
|---|---|---|---|
| Hunger | Time, cold, work | Food, soup kitchen | Health drains fast |
| Thirst | Time, work | Taps, bottles, drinks | Health drains |
| Health | Hunger/thirst at 0, fights, cold, sickness | Sleep, medicine, clinic | Hospital (lose days + gear) or game over |
| Warmth | Night, winter, rain | Fire barrel, blankets, clothes, shelter | Frostbite risk → health drain |
| Energy | Actions, walking far | Sleep, coffee | Can't act, forced sleep |
| Hygiene | Days without washing | Shelter showers, laundromat, rain | NPCs give less, jobs refuse you |
| Morale | Bad events, theft, cold nights | Music, friends, small wins, pets | Begging/job success drops |
| Money ($) | Spending | Begging, jobs, recycling, pawn | — |
| Respect (0–5★) | — | Helping others, story quests | Unlocks crew help, shelter perks, endings |

**Derived systems:**
- **Weather engine:** 4 seasons, daily forecast. Winter nights can hit
  −30°: warmth becomes the whole game. Rain soaks clothes (warmth penalty
  until dried). Summer brings heat + dehydration.
- **Day/night clock:** 24h compressed (~1 real minute = 1 game hour, tunable).
  Night = danger + cold. Shelters close at fixed hours; miss the cutoff and
  you sleep rough.
- **Sickness:** Wet + cold + low health → cold/flu: slower, hungrier, needs
  medicine or clinic visit.

## 4. Ways to earn (the economy)

- **Begging:** Pick a corner (foot-traffic rating varies by time/place).
  Mini-game: hold a sign, time your pitch. Better sign + cleaner look +
  good corner = more coins. Diminishing returns per spot per day.
- **Busking:** Needs an instrument (found/bought). Rhythm-tap mini-game.
  Scales with morale and crowd.
- **Odd jobs board:** Day labour, flyering, snow shovelling (winter only),
  moving help. Pays best, costs energy, needs minimum hygiene for some.
- **Recycling:** Collect cans/bottles → depot pays per bag. Reliable,
  low-prestige, always available.
- **Dumpster diving:** Loot table by district (rich district = better food,
  higher police attention). Noise/risk mechanic: too greedy → security.
- **Pawn shop:** Sell scrap, found items, crafted goods. Prices fluctuate
  daily (simple market ticker).
- **Crafting:** Scrap + wood → cart upgrades, fire barrel, tools, blankets,
  fishing rod (park pond), garden patch (late game).

## 5. Camp / base building (the "Kingdom" arc)

1. **Cardboard + sleeping bag** under the bridge (start).
2. **Tent** (found/bought) — warmth + storage.
3. **Shack** (wood + scrap + tarp) — fire barrel, stash, garden.
4. **Camp crew:** Recruit up to 3 NPCs; they gather while you work, guard
   at night, unlock group quests.
5. **Raids & theft:** Unattended camp can be raided. Defences (lock, dog,
   crew on guard) reduce risk. Rival crew "The Crows" escalates across days.

## 6. NPCs, factions, story

- **Mama June** — soup-kitchen runner, quest giver, moral compass.
- **Pigeon Pete** — comic relief; pigeon "network" delivers gossip/quests.
- **Officer Reyes** — police pressure: move-along orders, fines, jail night
  (lose a day) if you push your luck in rich districts.
- **The Crows** — rival crew; territory tug-of-war over the best corners.
- **Social worker Dana** — the "way back" questline: ID papers → job
  interview → apartment deposit. The hardest, most rewarding path.
- **30-day story arc:** Scripted beats on days 1, 3, 7, 14, 21, 30, plus
  random events (blizzard, festival crowd, shelter closure, found wallet
  with a moral choice).

## 7. Endings (5)

1. **Back on Your Feet** — job + apartment (hardest, needs hygiene, ID, savings).
2. **King of the Streets** — max camp + max respect, crew of 3.
3. **The Road Calls** — leave town with your cart (wanderer ending).
4. **Shelter Life** — stable shelter resident, volunteer path.
5. **Lost to the Cold** — fail state with dignity: memorial bench, stats
   recap, "try a different path" (never a joke ending).

## 8. Controls (touch-first, portrait)

- Virtual joystick (left) + context action button (right).
- Tap-to-move alternative for accessibility.
- One-thumb play: all mini-games are tap/hold timing, no twitch reflexes.
- Pause anywhere; autosave every in-game hour + on sleep.

## 9. Art direction + sprite plan ("accurate sprites")

**Style:** Top-down 2D pixel art, 16×16 base tiles, 48×48 character
canvases, dusk/night palette with warm firelight accents. Gritty but
readable — never muddy.

**Honest finding:** No free pack ships a literal "homeless person"
sprite. The accurate route is a free base character + a small custom
layer pass (beanie, beard, backpack, cardboard sign, shopping cart).
That pass is ~2–3 hours of pixel work and is the only custom art the
game truly needs.

| Need | Source | License | Notes |
|---|---|---|---|
| City tiles, buildings, roads | Kenney — Tiny City | CC0 | 130+ sprites, 16×16, free, no attribution needed |
| Streets, shops, subway, trash cans, vending machines, bus stop | muchopixels — City Street Tileset Pack | Check page (itch) | Day/afternoon/night skylines, broken-state variants |
| Dumpsters, benches, traffic lights, taxi, police car | BloodyFish — Pixel Streets | CC0 (demo free; full pack $1) | Dumpster states (open/closed/full/graffiti) are perfect |
| Town props: benches, bus stop, street lights, police/fire stations | Defected Pixel Artist — Top-Down Town Pack | Free on itch | Great for districts |
| Player base: walk/idle animations | Ruzty — Survival Player Character | Free base | 48×48, hand-drawn; custom layer pass adds beanie/beard/sign |
| Cozy streets, 14 storefronts, skylines | MutterPixel — Cozy Pixel City Street | itch | Backup/alternate district style |

**Custom art list (small, scoped):** player layers (beanie, beard,
backpack, sign, blanket), shopping cart, fire barrel, tent, shack,
pigeon, snow overlay, frost vignette. Everything else comes from packs.

**Animation list (player):** walk 4-dir, idle, sit, sleep, beg (kneel +
sign), rummage (dumpster), shiver (cold), wave/pitch.

## 10. Audio (free, no paid assets)

- Ambience: wind, rain, city hum (YouTube Audio Library / Pixabay,
  free license, credit in README).
- SFX: coins, rummage, fire crackle, footsteps on snow, busker guitar
  (synthesized or free packs).
- Music: one melancholy loop for night, one hopeful loop for morning
  (Kevin MacLeod CC-BY or YT Audio Library).

## 11. Tech architecture (Android/Kotlin, same pipeline as Doomscroll Guard)

- **Engine:** Custom Canvas-based 2D engine (no Unity/Godot — keeps the
  APK small, offline, and dependency-free). Tile map + entity system.
- **Structure:** `game/` (loop, clock, weather), `world/` (districts,
  tiles, loot tables), `actors/` (player, NPCs, AI), `economy/`
  (jobs, market, pawn), `camp/` (build, raids), `story/` (quests,
  endings), `ui/` (HUD, dialogs, mini-games), `save/` (JSON autosave).
- **Persistence:** JSON save + autosave; no account, no cloud.
- **Testing:** Unit tests for economy/loot/weather math; mock-event
  harness for stat drains; balance spreadsheet (target: day 7 reachable
  without grinding, day 30 hard but fair).
- **CI:** GitHub Actions → debug APK → rolling `latest` release, exactly
  like Doomscroll Guard.

## 12. Milestones

1. **M0 — Skeleton (1 week):** Walk around one district, clock, 4 core
   stats, sleep, game over. Playable greybox.
2. **M1 — Survive (2 weeks):** Dumpsters, begging, soup kitchen, weather,
   winter. First full loop.
3. **M2 — Earn & build (2 weeks):** Jobs, recycling, pawn, camp tiers,
   crafting.
4. **M3 — Story (2 weeks):** NPCs, quests, factions, police, raids.
5. **M4 — Polish (1 week):** Endings, balance, SFX, help screens, QC,
   release APK.

## 13. Risks & mitigations

- **Scope creep** (the #1 killer): lock M0–M2 scope; story is text +
  choices, never cutscenes.
- **Tone backlash:** empathy-first writing; no punch-down jokes; shelter
  resources on the end screen; playtest the tone with 2 readers.
- **Sprite mismatch:** single palette pass over all packs (re-tint to
  dusk palette) so mixed sources read as one game.
- **Balance:** spreadsheet + 3 full 30-day autopilot runs before release.

## 14. What it is NOT

- Not a mockery of homelessness, not a gore game, not pay-to-win,
  not always-online, not a clone of Bum Simulator (top-down mobile,
  own story, own systems, winter-first design).

---

*Repo name proposal: `cardboard-kingdom`. Next step on approval: scaffold
the repo + M0 greybox.*
