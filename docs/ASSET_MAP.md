# ASSET MAP — what sprite is what, and where it came from

## Characters (in game now — `godot/sprites/`, billboards)

| Game character | File | Source |
|---|---|---|
| Rook (hero) | rook.png | Kenney Roguelike Characters, sheet cell 324 |
| Mama June | june.png | Kenney Roguelike Characters, sheet cell 379 |
| Pigeon Pete | pete.png | Kenney Roguelike Characters, sheet cell 325 |
| Officer Reyes | reyes.png | Cell 487 + drawn navy cap + gold badge |
| Caw (Crows leader) | caw.png | Cell 270 + drawn tall black hat |

## World objects (in game now — `godot/assets/objects/`)

| Object | File | Source |
|---|---|---|
| Pine tree | tree_pine.png | Kenney Tiny Town, tile_0004 |
| Leafy tree | tree_leafy.png | Kenney Tiny Town, tile_0005 |

## Items (staged for v0.2 inventory — `godot/assets/items/`)

| Item | File | Source |
|---|---|---|
| Crate | crate.png | Kenney Roguelike RPG sheet, cell (36, 15) |
| Barrel | barrel.png | Kenney Roguelike RPG sheet, cell (52, 16) |
| Coin | coin.png | Kenney Roguelike Characters, sheet cell 87 |
| Mushroom (food) | mushroom.png | Kenney Tiny Town, tile_0029 |

## UI (staged — `godot/assets/ui/`)

| Icon | File | Source |
|---|---|---|
| Star (respect) | star.png | Kenney Game Icons, Black 1x |

## Deliberately still 3D geometry (not sprites)

Dumpster, bench, fire barrel, shopping cart, buildings, bridge.
In a 3D game these read better as physical geometry you can walk
around and push (the cart) than as flat billboards. Sprites are used
where they shine: characters and foliage.
