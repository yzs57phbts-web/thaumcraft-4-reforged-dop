"""Reuse the native recipe button silhouette; supply neutral and Thaumcraft wood frames."""
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parent
out=root/'src/main/resources/assets/thaumonomiconui/textures/gui/sprites'
out.mkdir(parents=True,exist_ok=True)
base=Image.open(root/'reference/button.png').convert('RGBA')
for y in range(18):
    for x in range(20):
        r,g,b,a=base.getpixel((x,y))
        if (2<=x<18 and 2<=y<16) or (a and (r!=g or g!=b)):
            base.putpixel((x,y),(198,198,198,a))
# Recolour the clean frame, not the original book artwork. Native hover uses
# blue shades for its outline/background; these must not be erased as "icon".
hover_palette={0:(0,7,62),85:(52,62,117),198:(136,146,201),255:(255,255,255)}
for hover in [False,True]:
    image=base.copy()
    if hover:
        for y in range(18):
            for x in range(20):
                r,g,b,a=image.getpixel((x,y))
                if a:image.putpixel((x,y),(*hover_palette[r],a))
    image.save(out/('book_button_highlighted.png' if hover else 'book_button.png'))
    wood=base.copy()
    for y in range(18):
        for x in range(20):
            r,g,b,a=wood.getpixel((x,y))
            if not a:continue
            if r<30:color=(28,18,11,a)
            elif r>220:color=((192 if hover else 159),(152 if hover else 120),(93 if hover else 70),a)
            elif r<110:color=(50,31,18,a)
            else:color=((111 if hover else 85),(77 if hover else 57),(44 if hover else 31),a)
            wood.putpixel((x,y),color)
    wood.save(out/('arcane_button_highlighted.png' if hover else 'arcane_button.png'))
