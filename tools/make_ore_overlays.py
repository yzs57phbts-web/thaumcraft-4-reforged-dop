"""Extract legacy ore pixels for host-rock models; preserve their original RGBA values."""
from pathlib import Path
from PIL import Image

TEXTURES = Path(__file__).resolve().parent.parent / "src/main/resources/assets/thaumcraft/textures/block"
# The legacy stone background uses this five-color grayscale palette.
STONE = {(v, v, v, 255) for v in (104, 116, 127, 143, 153)}


def generate():
    for name in ("cinnibar", "amberore"):
        original = Image.open(TEXTURES / f"{name}.png").convert("RGBA")
        overlay = original.copy()
        for y in range(original.height):
            for x in range(original.width):
                if original.getpixel((x, y)) in STONE:
                    overlay.putpixel((x, y), (0, 0, 0, 0))
        overlay.save(TEXTURES / f"{name}_overlay.png")


if __name__ == "__main__":
    generate()
