"""Native-size vanilla item silhouettes and centered UV sampling for magical wood boats."""
from pathlib import Path
import argparse
import io
import json
import zipfile
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent


def sprite(template, wood):
    """Keep the vanilla pixels/alpha exactly; map their shading into the wood's palette."""
    source = np.array(template.convert('RGBA'))
    palette = np.unique(np.array(wood.convert('RGB')).reshape(-1, 3), axis=0)
    weight = np.array([.2126, .7152, .0722])
    palette = palette[np.argsort(palette @ weight)]
    brightness = source[:, :, :3] @ weight
    opaque = source[:, :, 3] > 0
    low, high = brightness[opaque].min(), brightness[opaque].max()
    index = np.rint(np.clip((brightness - low) / max(1, high - low), 0, 1) * (len(palette) - 1)).astype(int)
    result = np.zeros_like(source)
    result[:, :, :3] = palette[index]
    result[:, :, 3] = source[:, :, 3]
    result[~opaque, :3] = 0
    return Image.fromarray(result, 'RGBA')


def boat_texture(template, wood):
    source = np.array(template.convert('RGBA')).astype(float)
    grain = np.array(wood.convert('RGB')).astype(float)
    y, x = np.indices(source.shape[:2])
    brightness = source[:, :, :3] @ np.array([.2126, .7152, .0722])
    median = np.median(brightness[source[:, :, 3] > 0])
    shading = np.clip(brightness / max(median, 1), .38, 1.6)
    color = grain[y % 16, x % 16].copy()
    # Vanilla BoatModel bottom box (28 x 16 x 3), texOffs(0, 0): its upward
    # NORTH face occupies U=3..31, V=3..19 after the X-axis rotation.
    # Put a complete wood motif at the middle of the 28px hull, rather than
    # centering the seam between two motifs. Pixels retain their native scale.
    inside = (x >= 3) & (x < 31) & (y >= 3) & (y < 19)
    color[inside] = grain[(y[inside] - 3) % 16, (x[inside] + 7) % 16]
    # BoatRenderer reverses the model Y axis, so SOUTH is the visible inner
    # floor. Its separate UV island must be centered as well (U=34..62).
    visible = (x >= 34) & (x < 62) & (y >= 3) & (y < 19)
    color[visible] = grain[(y[visible] - 3) % 16, (x[visible] + 8) % 16]
    rgba = np.dstack([np.clip(color * shading[:, :, None], 0, 255), source[:, :, 3]]).astype('uint8')
    return Image.fromarray(rgba, 'RGBA')


def framed_panel(wood, recess=.82):
    """A solid 16px end-grain panel, surrounded by a raised two-pixel frame."""
    grain=np.array(wood.convert('RGBA')).astype(float)
    out=grain.copy()
    out[2:14,2:14,:3]*=recess
    out[:2,:,:3]*=1.24
    out[:,:2,:3]*=1.12
    out[14:,:,:3]*=.88
    out[:,14:,:3]*=.92
    out[:,:,3]=255
    return np.clip(out,0,255).astype('uint8')


def hardware(panel, door=False, top=False):
    out=panel.copy()
    # Grey iron fittings: small highlights with a dark edge, never flat black dots.
    for y in ([4] if top else [11] if door else [3,11]):
        out[y:y+2,0:3]=[(60,57,49,255),(150,145,125,255),(90,86,73,255)]
    if door and not top:
        out[1,11:14]=[(65,61,51,255),(185,179,152,255),(185,179,152,255)]
        out[2,11]=[103,98,81,255]
    return Image.fromarray(out,'RGBA')


def silver_lattice(plank):
    """Four openings with one-pixel inset shadows and raised outer rails."""
    out=np.array(plank.convert('RGBA')).astype(float)
    out[0,:,:3]*=.70;out[-1,:,:3]*=.66
    out[:,0,:3]*=.73;out[:,-1,:3]*=.64
    out[1,1:15,:3]*=1.12;out[1:15,1,:3]*=1.10
    for y0 in [3,9]:
        for x0 in [3,9]:
            out[y0-1,x0-1:x0+4,:3]*=.52
            out[y0:y0+4,x0-1,:3]*=.60
            out[y0+4,x0:x0+5,:3]*=1.23
            out[y0:y0+4,x0+4,:3]*=1.15
            out[y0:y0+4,x0:x0+4]=0
    return np.clip(out,0,255).astype('uint8')


def gold_door_fittings(panel,top):
    out=panel.copy()
    dark=(92,58,20,255);shade=(160,108,30,255)
    gold=(225,172,54,255);light=(255,224,121,255)
    # One shaded hinge per half; the opening's wood shading remains independent.
    y=4 if top else 11
    out[y:y+3,0]=[dark,gold,shade]
    out[y:y+3,1]=[gold,light,gold]
    if not top:
        # A small metal backplate and horizontal lever, in native block pixels.
        out[0:4,14]=[shade,light,gold,dark]
        out[1:4,13]=[gold,light,shade]
        out[1,11:14]=[shade,light,light]
        out[2,11:13]=[dark,shade]
    return Image.fromarray(out,'RGBA')


def wood_only(template,other_wood,plank):
    """Vanilla hanging-sign artwork; preserve its unchanged metal chain pixels."""
    original=np.array(template);result=np.array(sprite(template,plank))
    same=np.all(original==np.array(other_wood),axis=2)
    result[same]=original[same]
    return Image.fromarray(result,'RGBA')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--client-jar', type=Path, default=Path.home() / 'curseforge/minecraft/Install/versions/1.21.1/1.21.1.jar')
    args = parser.parse_args()
    generated = ROOT / 'src/generated/resources'
    portable = ROOT / 'tools/modern_resources/1.21.1'
    assets = ROOT / 'src/main/resources/assets/thaumcraft/textures/block'
    with zipfile.ZipFile(args.client_jar) as archive:
        def vanilla(name):
            return Image.open(io.BytesIO(archive.read('assets/minecraft/textures/' + name + '.png'))).convert('RGBA')
        for wood in ['greatwood', 'silverwood']:
            plank = Image.open(assets / ('planks_' + wood + '.png')).convert('RGBA')
            hull = boat_texture(vanilla('entity/boat/oak'), plank)
            chest = np.array(vanilla('entity/chest_boat/oak'))
            # ChestBoatModel keeps the hull UVs in the same upper 64px. The
            # original chest lid/lock/body texture starts below the hull islands.
            hull_mask = np.array(vanilla('entity/boat/oak'))[:, :, 3] > 0
            chest[:64][hull_mask] = np.array(hull)[hull_mask]
            chest_icon = np.array(sprite(vanilla('item/oak_chest_boat'), plank))
            oak_icon = np.array(vanilla('item/oak_chest_boat'))
            birch_icon = np.array(vanilla('item/birch_chest_boat'))
            same = np.all(oak_icon == birch_icon, axis=2)
            chest_icon[same] = oak_icon[same]
            outputs = {
                'textures/item/' + wood + '_boat.png': sprite(vanilla('item/oak_boat'), plank),
                'textures/item/' + wood + '_chest_boat.png': Image.fromarray(chest_icon, 'RGBA'),
                'textures/entity/modern_boat_' + wood + '.png': hull,
                'textures/entity/modern_chest_boat_' + wood + '.png': Image.fromarray(chest, 'RGBA'),
            }
            if wood=='greatwood':
                # Use the actual, centered inner-floor UV island. Its oak-derived
                # end-grain shading is part of the boat motif, not just the planks.
                floor=hull.crop((40,3,56,19))
                top=hardware(framed_panel(floor),True,True)
                bottom=hardware(framed_panel(floor),True,False)
            else:
                lattice=silver_lattice(plank)
                top=gold_door_fittings(lattice,True);bottom=gold_door_fittings(lattice,False)
            outputs['textures/block/'+wood+'_door_top.png']=top
            outputs['textures/block/'+wood+'_door_bottom.png']=bottom
            outputs['textures/block/'+wood+'_trapdoor.png']=hardware(framed_panel(floor,.86)) if wood=='greatwood' else Image.fromarray(lattice,'RGBA')
            # Exact vanilla item silhouettes and pixel positions, no miniature
            # block-texture composition or bespoke door artwork.
            outputs['textures/item/'+wood+'_door.png']=sprite(vanilla('item/spruce_door' if wood=='greatwood' else 'item/oak_door'),plank)
            outputs['textures/item/'+wood+'_sign.png']=sprite(vanilla('item/oak_sign'),plank)
            outputs['textures/entity/signs/'+wood+'.png']=sprite(vanilla('entity/signs/oak'),plank)
            outputs['textures/item/'+wood+'_hanging_sign.png']=wood_only(vanilla('item/oak_hanging_sign'),vanilla('item/birch_hanging_sign'),plank)
            outputs['textures/entity/signs/hanging/'+wood+'.png']=wood_only(vanilla('entity/signs/hanging/oak'),vanilla('entity/signs/hanging/birch'),plank)
            outputs['textures/gui/hanging_signs/'+wood+'.png']=wood_only(vanilla('gui/hanging_signs/oak'),vanilla('gui/hanging_signs/birch'),plank)
            for relative, image in outputs.items():
                for base in [generated, portable]:
                    target = base / 'assets/thaumcraft' / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    image.save(target)
                print(relative, image.size)


if __name__ == '__main__':
    main()
