#!/usr/bin/env python3
"""Convert Kenney CC0 pack art into Cardboard Kingdom 3D game sprites.
Sources (all CC0, kenney.nl — re-download into godot/assets/_packs/ to rerun):
  - kenney_roguelike-characters (sheet: 16x16 cells, 1px margin, 54 cols)
  - kenney_tiny-town (individual 16x16 tiles)
  - kenney_roguelike-rpg-pack (sheet: 16x16 cells, 1px margin, 57 cols)
  - kenney_game-icons (individual PNGs)
Outputs: godot/sprites/*.png (characters, same names the game loads),
godot/assets/objects/*.png, godot/assets/items/*.png, godot/assets/ui/*.png.
"""
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACKS = os.path.join(ROOT, "assets", "_packs")
OUT_SPR = os.path.join(ROOT, "sprites")
OUT_OBJ = os.path.join(ROOT, "assets", "objects")
OUT_ITM = os.path.join(ROOT, "assets", "items")
OUT_UI = os.path.join(ROOT, "assets", "ui")
for d in (OUT_SPR, OUT_OBJ, OUT_ITM, OUT_UI):
    os.makedirs(d, exist_ok=True)

CHAR_SHEET = os.path.join(PACKS, "kenney_roguelike-characters",
                          "Spritesheet", "roguelikeChar_transparent.png")
RPG_SHEET = os.path.join(PACKS, "kenney_roguelike-rpg-pack",
                         "Spritesheet", "roguelikeSheet_transparent.png")
TINY = os.path.join(PACKS, "kenney_tiny-town", "Tiles")
ICONS = os.path.join(PACKS, "kenney_game-icons", "PNG", "Black", "1x")


def cell(sheet, col, row, pitch=17, size=16):
    return sheet.crop((col * pitch, row * pitch,
                       col * pitch + size, row * pitch + size))


def char_index(sheet, idx, cols=54):
    return cell(sheet, idx % cols, idx // cols)


def make_character(sheet, idx, name, hat=None, badge=False):
    """16x16 char cell -> 256x384 billboard canvas (feet at bottom)."""
    img = char_index(sheet, idx).copy()
    d = ImageDraw.Draw(img)
    if hat == "cap":
        for x in range(5, 11):
            for y in range(0, 3):
                d.point((x, y), fill=(25, 35, 70, 255))
        for x in range(4, 12):
            d.point((x, 3), fill=(15, 20, 45, 255))
    if hat == "tall":
        for x in range(5, 11):
            for y in range(0, 3):
                d.point((x, y), fill=(15, 15, 18, 255))
        for x in range(4, 12):
            d.point((x, 3), fill=(15, 15, 18, 255))
    if badge:
        for x in (7, 8):
            for y in (9, 10):
                d.point((x, y), fill=(240, 200, 60, 255))
    big = img.resize((256, 256), Image.NEAREST)
    canvas = Image.new("RGBA", (256, 384), (0, 0, 0, 0))
    canvas.paste(big, (0, 128), big)
    canvas.save(os.path.join(OUT_SPR, name + ".png"))
    print("character:", name, "from cell", idx)


def main():
    chars = Image.open(CHAR_SHEET).convert("RGBA")
    make_character(chars, 324, "rook")
    make_character(chars, 379, "june")
    make_character(chars, 325, "pete")
    make_character(chars, 487, "reyes", hat="cap", badge=True)
    make_character(chars, 270, "caw", hat="tall")

    for tile, name in [("tile_0004.png", "tree_pine"),
                       ("tile_0005.png", "tree_leafy")]:
        t = Image.open(os.path.join(TINY, tile)).convert("RGBA")
        t.resize((128, 128), Image.NEAREST).save(
            os.path.join(OUT_OBJ, name + ".png"))
        print("object:", name, "from", tile)

    rpg = Image.open(RPG_SHEET).convert("RGBA")
    items = [
        (cell(rpg, 36, 15), "crate"),
        (cell(rpg, 52, 16), "barrel"),
    ]
    for img, name in items:
        img.resize((128, 128), Image.NEAREST).save(
            os.path.join(OUT_ITM, name + ".png"))
        print("item:", name)
    coin = char_index(chars, 87)
    coin.resize((128, 128), Image.NEAREST).save(
        os.path.join(OUT_ITM, "coin.png"))
    print("item: coin from char cell 87")
    mush = Image.open(os.path.join(TINY, "tile_0029.png")).convert("RGBA")
    mush.resize((128, 128), Image.NEAREST).save(
        os.path.join(OUT_ITM, "mushroom.png"))
    print("item: mushroom from tile_0029")

    star = Image.open(os.path.join(ICONS, "star.png")).convert("RGBA")
    star.save(os.path.join(OUT_UI, "star.png"))
    print("ui: star")


if __name__ == "__main__":
    main()
