"""Isolate original 26px research frames with transparent padding, without resampling."""
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parent
source=Image.open(root/'reference/gui_research.png').convert('RGBA')
out=root/'src/main/resources/assets/thaumonomiconui/textures/gui'
out.mkdir(parents=True,exist_ok=True)
for u in [0,26,54,86,110,230]:
    image=Image.new('RGBA',(34,34))
    image.paste(source.crop((u,230,u+26,256)),(4,4))
    image.save(out/f'frame_{u}.png')
