# CARDBOARD KINGDOM — Big Project Edition (GDD v2)

**Status:** Design approved by user 2026-10-21 (3D style, detailed
sprites, character design, NPCs, open world, missions, story, narration,
physics, replayability, release-ready).
**Tone (unchanged):** empathy-first, never mocking. Game-over screen shows
real shelter resources (211 line).
**Business rules (unchanged):** free, offline, no ads, no IAP. VN signature.
**Tech target:** Godot 3D (mobile renderer) + detailed billboard sprites
in a 3D world. The shipped Kotlin M0 (APK live) stays as the playable
prototype that proved the loop; the Big Edition is the release target.
**M0 scope is NOT discarded:** every M0 system (stats, clock, sleep,
soup kitchen, shelter warm-up, dumpster food, camp, game over) carries
over, deepened.

---

## 1. VISION & PILLARS

You arrive in Frostgate with nothing. You survive winter, build a camp
into a home, earn the streets' trust, and choose who you become.
Five pillars: **Survive** (needs + weather) → **Belong** (NPCs +
reputation) → **Build** (camp tiers) → **Decide** (moral choices with
consequences) → **Become** (5 endings, then play again differently).

## 2. WHAT SIMILAR GAMES HAVE — AND WHAT WE IMPLANT

| Source game | Feature | Implanted in CK as |
|---|---|---|
| Bum Simulator | Open-world city, custom begging sign | 5-district Frostgate; begging with a sign you write + busking |
| Bum Simulator | Base building + rival gang raids | Camp tiers 1–5 + night raids by The Crows (defend or lose stores) |
| Bum Simulator | Pigeon companions, pawn shops | Pigeon Pete's pigeon flock (scout + fetch); pawnbroker NPC with haggle |
| Bum Simulator | Story + side quests, voiced characters | 5-act story, 40+ side missions, narration + dialogue |
| Hobo: Tough Life | Skill/perk trees | 6 skills (Begging, Scavenge, Craft, Repair, Trade, Guts) with perks |
| Hobo: Tough Life | NPC trust unlocks training | Trust levels; NPCs teach perks (Mama June: cooking; Pete: pigeons) |
| Hobo: Tough Life | Clothing warmth, seasons get harder | Clothing system with warmth ratings; each season colder/harder |
| Hobo: Tough Life | Cooking, hygiene, sickness | Cook meals; hygiene affects begging success; trash food can sicken |
| This War of Mine | Karma endings | Every ending graded by your choices (steal/help/betray) |
| This War of Mine | Night shelter defense, scavenging tools | Night-guard choice; crowbar/lockpick for locked spots |
| This War of Mine | Morale changes behavior, radio news | Morale stat changes dialogue options; Frostgate Radio event bulletins |
| Stardew Valley | NPC schedules, friendship hearts | Every NPC has a schedule + 0–10 hearts |
| Stardew Valley | Gifts with preferences, heart events | Gift system; story scenes unlock at 2/4/6/8/10 hearts |
| Stardew Valley | Bulletin board, festivals, collections | Mission board radiant quests; 4 seasonal festivals; collections journal |

## 3. OPEN WORLD — FROSTGATE (5 districts)

One continuous city, no loading screens between districts (mobile-safe
low-poly 3D + billboard sprites). Walk everywhere; unlock a battered
bicycle in Act 2 for faster travel.

| District | Vibe | Key locations | Unlock |
|---|---|---|---|
| Underbridge | Home turf | Camp, soup kitchen, shelter, Pete's roost | Start |
| Downtown | Money + danger | Pawn shop, offices (begging spots), police station, mission board | Start |
| Riverside | Quiet + secrets | Fishing spot, hidden caches, old boathouse | Befriend Pete (2 hearts) |
| Old Town | Story hub | Library (warmth + skill books), clinic, Dana's office | Act 2 |
| Industrial | Risk + reward | Warehouses (locked, tools needed), scrapyard, Crows' turf | Act 3 / Guts perk |

**World life:** day/night clock (1 real min = 1 game hour), 4 seasons,
weather (snow, blizzard, rain, wind), pedestrians with routines,
police patrols, stray dogs, pigeons. Random events: donation drive,
police sweep, blizzard warning, lost wallet (moral choice), rival raid.

## 4. CHARACTER DESIGN (detailed billboard sprites)

**Style:** 3D world, characters as detailed 2D sprites (8-direction
walk, idle, emote, sit, sleep, work poses), layered clothing so outfits
change visibly. Readable silhouette at phone size; expressive faces
(close-up portraits in dialogue).

**Hero — "Rook" (default name, player can rename):** late 20s, layered
hoodie + patched coat + beanie + backpack + shopping cart. Design
tokens: red beanie, fingerless gloves, a cardboard crown (late game,
earned — the "kingdom" joke made visual).

**NPC roster (design + role):**
- **Mama June** — soup-kitchen cook, warm, huge. Teaches cooking.
  Loves: canned peaches. Heart events: her lost son's story.
- **Pigeon Pete** — wild-eyed, feathered coat. Pigeon flock system.
  Loves: breadcrumbs. Teaches pigeon scout/fetch.
- **Officer Reyes** — tired, fair cop. Law axis: help her → police
  leave you alone; cross her → sweeps. Loves: black coffee.
- **Dana** — social worker, clipboard, kind but blunt. Main questline
  to job + apartment ending. Loves: notebooks.
- **The Crows** — rival crew, 4 members, leader **Caw** (tall, black
  coat, crow feather). Raid your camp; can be befriended, beaten, or
  out-earned. Ending branch.
- **Milo the Pawnbroker** — haggle minigame, rare items, rumors.
- **Sister Agnes** — shelter keeper, warmth + medicine.
- **Busker Ray** — teaches busking; rhythm minigame.
- **The King (Hobo King ladder)** — seasonal title: most respect in
  Frostgate wins the cardboard crown for a season (replay driver).

## 5. INTERACTABLE ITEMS (the "everything can be touched" rule)

Every object in the world has at least one verb. Categories:
- **Containers:** dumpsters (loot tiers by district + skill), bins,
  lockers (lockpick), backpacks, caches (hidden, pigeon-scouted).
- **Comfort:** benches (rest), fire barrels (warmth + cook), phones
  (call shelter line / story beats), library books (skill XP),
  showers (hygiene, shelter/clinic).
- **Work:** recycling machines (cans → coins), busking spots (rhythm
  minigame), day-labor board, pawn shop, market stalls.
- **Camp objects:** tent, bedroll, storage crate, workbench (craft),
  pigeon coop, defenses (tripwire, barricade), garden pots (grow food).
- **Story objects:** notice board, radio (news + warnings), photo wall
  (endings gallery), lost items (return for karma).

## 6. GAME PHYSICS (Godot, mobile-budgeted)

- Character controller with momentum; **ice = slippery** (winter
  hazard), mud slows, wind gusts push on bridges.
- **Shopping cart physics:** pushable, carries loot, rolls downhill,
  can be stolen/lost (recover quest). Cart + slope = comedy + strategy.
- **Throwing:** snowballs, cans, pigeons (arc + gravity, aim assist).
- **Ragdoll-lite:** falls, shoves, and raid scuffles (no gore — comic
  tumble, stars, dust puff).
- **Fire barrel:** warmth radius, wind spreads smoke, rain puts it out.
- **Crowd flow:** pedestrians avoid you, part around carts, gather at
  events. Police AI: patrol paths, chase on theft, cool-down.

## 7. SURVIVAL SYSTEMS (deepened M0)

Stats: hunger, thirst, health, warmth, energy, hygiene, morale, money,
respect, karma (hidden until ending). Skills (6, leveled by use):
Begging, Scavenge, Craft, Repair, Trade, Guts — each with 3 perk tiers
taught by NPCs or learned by doing. Clothing: 4 slots, warmth +
condition; washing/repair loop. Cooking: recipes from ingredients;
trash food risks sickness; medicine from clinic. Sleep: camp (best),
bench (risky), shelter (safe, limited nights).

## 8. STORY — 5 ACTS

- **Act 1 — Arrival:** first night, freeze or find shelter. Meet June.
- **Act 2 — Roots:** build camp tier 2, meet Pete + Dana, get the bike.
- **Act 3 — The Crows:** turf war — fight, befriend, or out-earn them.
- **Act 4 — The Winter:** blizzard season, hard choices, Reyes's test.
- **Act 5 — The Choice:** job + apartment path, King path, or stay.
**5 endings:** Apartment (best), King of Frostgate, Wanderer (leave),
Street Legend (stay, loved), Lost (bad/karma). Epilogue grades every
major choice, TWoM-style.

## 9. MISSIONS

- **Main:** 25 story missions across 5 acts.
- **Side:** 40+ NPC missions (June's lost recipes, Pete's lost birds,
  Reyes's stolen badge, Dana's paperwork chain, Milo's rare-item hunts).
- **Radiant:** mission board refreshes daily (deliver, fetch, busk,
  clean, escort) — money + respect + hearts.
- **Festivals (seasonal):** Winter Lantern Night, Spring Market,
  Summer Busk-Off, Autumn Harvest Share — minigames + big heart gains.

## 10. NARRATION

- **Narrator:** warm, dry storyteller voice in text (voiced intros
  planned for v1.1) — opens each act, closes each ending.
- **Diary:** Rook's journal auto-writes after big events; readable
  anytime; doubles as a hint system ("Dana mentioned the clinic…").
- **Dialogue:** portrait + text, choices with visible stakes
  (hearts/karma/money icons). NPC barks react to weather, season,
  your reputation.
- **Radio:** Frostgate Radio bulletins warn of blizzards, sweeps,
  festivals — story delivered as world news, not pop-ups.

## 11. REPLAYABILITY

5 endings × karma branches; Hobo King seasonal ladder; New Game+
(keep skills, harder winter); collections journal (birds, recipes,
photos, lost items); achievements (30); difficulty modes (Story /
Survival / Hardcore Winter); daily board quests; randomized events
and loot; 3 hero backgrounds (choice at start changes opening week
and one NPC relationship).

## 12. POLISH DETAILS (the "pay attention" list)

Footprints in snow; visible breath vapor; wet footprints after rain;
clothes visibly tatter; camp lights at night; pigeons scatter when you
run; steam from soup; day/night lighting + long winter shadows; ambient
audio (wind, distant traffic, soup-kitchen clatter, pigeon coos);
footstep sounds change by surface (snow crunch, pavement, wood);
UI: needs icons pulse when critical; weather forecast on the pause
screen; photo mode (hide UI, snap camp).

## 13. RELEASE PLAN — ROAD TO v1.0 (release-ready)

| Phase | Scope | Exit criteria (greenlight gate) |
|---|---|---|
| v0.2 Systems | Skills, clothing, cooking, hygiene, cart physics | All 6 skills playable; no crash in 30-min session |
| v0.4 World | 5 districts, schedules, mission board, events | NPCs follow schedules; board refreshes daily |
| v0.6 Story | Acts 1–3, 20 side missions, narration + diary | Full playthrough to Act 3 without blockers |
| v0.8 Full game | Acts 4–5, 5 endings, festivals, NG+ | All endings reachable; balance pass |
| v1.0 Release | Polish (§12), QA sweep, store page, trailer | Crew unanimous green (TEAM.md rule); APK + page live |

**Release checklist:** icon, screenshots (5), description, content
rating note, offline verified, save/load verified, 60-min soak test,
old-device test, credits, 211-line game-over screen.

## 14. RISKS & MITIGATIONS

- Scope creep → phases are gated; v1.0 cuts festivals before story.
- Mobile perf → low-poly 3D + sprite characters; district streaming.
- Tone risk → empathy rule in every review; no mocking quests.
- Save corruption → 3 rotating slots + backup on sleep.
