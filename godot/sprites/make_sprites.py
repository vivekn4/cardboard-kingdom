#!/usr/bin/env python3
"""Greybox pixel-art sprites for Cardboard Kingdom 3D.
Drawn on a 32x48 grid, scaled 8x with NEAREST to 256x384.
These are placeholders: the Art Director's detailed sheets replace them.
"""
from PIL import Image, ImageDraw

W, H, S = 32, 48, 8
SKIN = (232, 190, 150, 255)
DARK = (40, 42, 48, 255)


def canvas():
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def rect(d, x, y, w, h, c):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=c)


def base(d, coat, pants, boots):
    # Legs, boots, torso, arms, head, eyes.
    rect(d, 11, 36, 4, 8, pants)
    rect(d, 17, 36, 4, 8, pants)
    rect(d, 10, 44, 5, 3, boots)
    rect(d, 17, 44, 5, 3, boots)
    rect(d, 8, 20, 16, 16, coat)
    rect(d, 5, 21, 3, 12, coat)
    rect(d, 24, 21, 3, 12, coat)
    rect(d, 10, 8, 12, 12, SKIN)
    rect(d, 12, 13, 2, 2, DARK)
    rect(d, 18, 13, 2, 2, DARK)
    rect(d, 13, 17, 6, 1, DARK)


def save(img, name):
    img = img.resize((W * S, H * S), Image.NEAREST)
    img.save(f"sprites/{name}.png")
    print("wrote", name)


def rook():
    img, d = canvas()
    base(d, (52, 64, 84, 255), (48, 48, 54, 255), (30, 28, 26, 255))
    rect(d, 9, 5, 14, 4, (200, 45, 45, 255))   # red beanie
    rect(d, 9, 8, 14, 2, (160, 30, 30, 255))  # beanie brim
    rect(d, 11, 22, 10, 12, (120, 85, 50, 255))  # backpack
    rect(d, 13, 22, 6, 2, (90, 60, 35, 255))
    save(img, "rook")


def june():
    img, d = canvas()
    base(d, (60, 130, 125, 255), (70, 60, 90, 255), (50, 45, 40, 255))
    rect(d, 11, 4, 10, 4, (200, 200, 200, 255))  # grey bun
    rect(d, 10, 22, 12, 12, (240, 240, 235, 255))  # apron
    rect(d, 10, 8, 12, 2, (200, 200, 200, 255))   # hair
    save(img, "june")


def pete():
    img, d = canvas()
    base(d, (110, 90, 60, 255), (60, 55, 45, 255), (45, 40, 32, 255))
    rect(d, 9, 5, 14, 4, (90, 70, 45, 255))   # wild hair
    for x, y in [(9, 24), (14, 28), (20, 25), (11, 31), (18, 32)]:
        rect(d, x, y, 2, 2, (150, 140, 120, 255))  # feather speckles
    rect(d, 23, 18, 5, 4, (160, 160, 165, 255))  # pigeon on shoulder
    rect(d, 27, 19, 2, 1, (240, 180, 60, 255))   # beak
    save(img, "pete")


def reyes():
    img, d = canvas()
    base(d, (35, 50, 95, 255), (30, 42, 80, 255), (20, 20, 22, 255))
    rect(d, 9, 5, 14, 4, (25, 35, 70, 255))   # cap
    rect(d, 9, 8, 14, 2, (15, 20, 45, 255))   # cap brim
    rect(d, 15, 6, 2, 2, (240, 200, 60, 255))  # badge
    rect(d, 9, 30, 14, 2, (15, 15, 18, 255))   # belt
    save(img, "reyes")


def caw():
    img, d = canvas()
    base(d, (22, 22, 26, 255), (25, 25, 30, 255), (15, 15, 16, 255))
    rect(d, 10, 2, 12, 6, (15, 15, 18, 255))  # tall hat
    rect(d, 9, 7, 14, 2, (15, 15, 18, 255))   # hat brim
    rect(d, 22, 3, 2, 4, (200, 200, 210, 255))  # feather
    rect(d, 11, 8, 10, 12, (235, 220, 205, 255))  # pale face redraw
    rect(d, 13, 13, 2, 2, DARK)
    rect(d, 18, 13, 2, 2, DARK)
    rect(d, 8, 20, 16, 20, (22, 22, 26, 255))  # long coat
    save(img, "caw")


def icon():
    img = Image.new("RGBA", (128, 128), (30, 34, 44, 255))
    d = ImageDraw.Draw(img)
    d.polygon([(24, 96), (24, 40), (44, 62), (64, 32), (84, 62), (104, 40),
               (104, 96)], fill=(205, 165, 95, 255))
    d.rectangle([24, 96, 104, 104], fill=(160, 125, 65, 255))
    img.save("icon.png")
    print("wrote icon")


if __name__ == "__main__":
    import os
    os.makedirs("sprites", exist_ok=True)
    rook()
    june()
    pete()
    reyes()
    caw()
    icon()
