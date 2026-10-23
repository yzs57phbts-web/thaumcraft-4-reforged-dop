"""Native 16px ore sprites: copper-like raw cinnabar and gently concentrated clusters."""
from pathlib import Path
import io,zipfile
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parent.parent
with zipfile.ZipFile(Path.home()/'curseforge/minecraft/Install/versions/1.21.1/1.21.1.jar') as z:
 raw=np.array(Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/raw_copper.png'))).convert('RGBA'))
palette=np.array([(53,9,32),(91,9,41),(130,19,48),(165,26,55),(199,44,63),(229,71,83),(247,110,118),(255,164,171),(255,213,216)])
light=raw[:,:,:3]@np.array([.2126,.7152,.0722]);visible=raw[:,:,3]>0
low,high=light[visible].min(),light[visible].max()
raw[:,:,:3]=palette[np.rint(np.clip((light-low)/(high-low),0,1)*(len(palette)-1)).astype(int)]
raw[~visible,:3]=0
for base in ['src/generated/resources','tools/modern_resources/1.21.1']:
 p=R/base/'assets/thaumcraft/textures/item/raw_cinnabar.png';p.parent.mkdir(parents=True,exist_ok=True);Image.fromarray(raw).save(p)
# Originals stay in assets_extra as well, so conversion of legacy art cannot undo these.
targets={'iron':(198,178,146),'gold':(238,181,42),'copper':(197,105,62),'tin':(167,194,196),'silver':(186,206,211),'lead':(133,136,172),'cinnabar':(189,52,79)}
archive=R.parents[1]/'diagnostics/tc-refinement-bbtt19/thaumcraft-before.zip'
with zipfile.ZipFile(archive) as z:
 for metal,target in targets.items():
  rel='assets/thaumcraft/textures/item/cluster'+metal+'.png'
  data=np.array(Image.open(io.BytesIO(z.read('src/main/resources/'+rel))).convert('RGBA'))
  opaque=data[:,:,3]>0;interior=opaque.copy()
  interior[0]=False;interior[-1]=False;interior[:,0]=False;interior[:,-1]=False
  interior[1:-1,1:-1]&=opaque[:-2,1:-1]&opaque[2:,1:-1]&opaque[1:-1,:-2]&opaque[1:-1,2:]
  dark=interior&((data[:,:,:3]@np.array([.2126,.7152,.0722]))<110)
  data[dark,:3]=np.rint(data[dark,:3]*.72+np.array(target)*.28).astype('uint8')
  for base in ['src/main/resources','tools/assets_extra']:
   p=R/base/rel;p.parent.mkdir(parents=True,exist_ok=True);Image.fromarray(data).save(p)
  print(metal,int(dark.sum()),'interior pixels adjusted')
