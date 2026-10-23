import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "decompiled" / "thaumcraft" / "client" / "renderers" / "models" / "gear"
TARGET = ROOT / "src" / "main" / "java" / "thaumcraft" / "client" / "render" / "armor" / "ArmorLayers.java"
MODELS = {
    "robe": "ModelRobe",
    "fortress": "ModelFortressArmor",
    "knight": "ModelKnightArmor",
    "leader": "ModelLeaderArmor",
}
BIPEDS = {
    "bipedHead": "head",
    "bipedHeadwear": "hat",
    "bipedBody": "body",
    "bipedRightArm": "right_arm",
    "bipedLeftArm": "left_arm",
    "bipedRightLeg": "right_leg",
    "bipedLeftLeg": "left_leg",
}
MASK_LOOP = re.compile(r"this\.Mask = new ModelRenderer\[3\];\s*for \(int a = 0; a < 3; a\+\+\) \{(.*?)\n\s*\}", re.S)


def number(text):
    text = text.strip()
    if text.startswith("(float)"):
        text = text[len("(float)"):].strip()
    text = text.strip("()").replace("Math.PI", "3.141592653589793")
    return float(eval(text.rstrip("F").replace("F", ""), {"__builtins__": {}}))


def unroll_masks(body):
    match = MASK_LOOP.search(body)
    if not match:
        return body
    template = match.group(1)
    unrolled = []
    for index in range(3):
        unrolled.append(template.replace("Mask[a]", f"Mask{index}").replace("52 + a * 24", str(52 + index * 24)))
    body = body[:match.start()] + "\n".join(unrolled) + body[match.end():]
    return re.sub(r"Mask\[(\d)\]", r"Mask\1", body)


def constructor(name):
    text = (SOURCE / f"{name}.java").read_text(encoding="utf-8")
    start = text.index(f"public {name}(float f) {{")
    depth = 0
    for index in range(start, len(text)):
        if text[index] == "{":
            depth += 1
        elif text[index] == "}":
            depth -= 1
            if depth == 0:
                return unroll_masks(text[text.index("{", start) + 1:index])
    raise ValueError(name)


def parse(name):
    parts = {}
    children = []
    condition = []
    for raw in constructor(name).splitlines():
        line = raw.strip()
        if not line:
            continue
        if line == "if (f < 1.0F) {":
            condition.append("inner")
            continue
        if line == "if (!(f < 1.0F)) {":
            condition.append("outer")
            continue
        if line == "} else {":
            condition[-1] = "outer" if condition[-1] == "inner" else "inner"
            continue
        if line == "}":
            condition.pop()
            continue
        if match := re.fullmatch(r"this\.(\w+) = new ModelRenderer\(this, (-?\d+), (-?\d+)\);", line):
            parts[match.group(1)] = {"tex": (int(match.group(2)), int(match.group(3))), "mirror": False, "boxes": [], "pivot": (0.0, 0.0, 0.0), "rot": (0.0, 0.0, 0.0)}
        elif match := re.fullmatch(r"this\.(\w+)\.mirror = true;", line):
            parts[match.group(1)]["mirror"] = True
        elif match := re.fullmatch(r"this\.(\w+)\.addBox\((.*)\);", line):
            values = [number(value) for value in match.group(2).split(",")]
            part = parts[match.group(1)]
            part["boxes"].append((values, part["mirror"]))
        elif match := re.fullmatch(r"this\.(\w+)\.setRotationPoint\((.*)\);", line):
            parts[match.group(1)]["pivot"] = tuple(number(value) for value in match.group(2).split(","))
        elif match := re.fullmatch(r"this\.setRotation\(this\.(\w+), (.*)\);", line):
            parts[match.group(1)]["rot"] = tuple(number(value) for value in re.split(r",\s*(?![^()]*\))", match.group(2)))
        elif match := re.fullmatch(r"this\.(biped\w+)\.addChild\(this\.(\w+)\);", line):
            children.append((match.group(1), match.group(2), condition[-1] if condition else None))
        elif re.fullmatch(r"this\.(biped\w+)\.cubeList\.clear\(\);|this\.\w+\.setTextureSize\(\d+, \d+\);|super\(f, 0\.0F, 128, 64\);|this\.texture(Width|Height) = \d+;", line):
            continue
        else:
            raise ValueError(f"{name}: {line}")
    return parts, children


def snake(name):
    return re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name).lower()


def java_float(value):
    text = repr(float(value))
    return f"{text}F"


def part_code(parent, child, part, indent):
    cubes = "CubeListBuilder.create()"
    cubes += f".texOffs({part['tex'][0]}, {part['tex'][1]})"
    mirrored = False
    for values, mirror in part["boxes"]:
        if mirror != mirrored:
            cubes += ".mirror()" if mirror else ".mirror(false)"
            mirrored = mirror
        box = ", ".join(java_float(value) for value in values[:6])
        if len(values) > 6:
            box += f", new CubeDeformation({java_float(values[6])})"
        cubes += f".addBox({box})"
    pivot = ", ".join(java_float(value) for value in part["pivot"])
    rot = ", ".join(java_float(value) for value in part["rot"])
    return f"{indent}{parent}.addOrReplaceChild(\"{snake(child)}\", {cubes}, PartPose.offsetAndRotation({pivot}, {rot}));"


def layer_method(key, name):
    parts, children = parse(name)
    lines = [
        f"    public static LayerDefinition {key}(float f) {{",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
        "        PartDefinition head = root.addOrReplaceChild(\"head\", CubeListBuilder.create(), PartPose.ZERO);",
        "        PartDefinition hat = root.addOrReplaceChild(\"hat\", CubeListBuilder.create(), PartPose.ZERO);",
        "        PartDefinition body = root.addOrReplaceChild(\"body\", CubeListBuilder.create(), PartPose.ZERO);",
        "        PartDefinition rightArm = root.addOrReplaceChild(\"right_arm\", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));",
        "        PartDefinition leftArm = root.addOrReplaceChild(\"left_arm\", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));",
        "        PartDefinition rightLeg = root.addOrReplaceChild(\"right_leg\", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));",
        "        PartDefinition leftLeg = root.addOrReplaceChild(\"left_leg\", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));",
    ]
    variables = {"bipedHead": "head", "bipedHeadwear": "hat", "bipedBody": "body", "bipedRightArm": "rightArm", "bipedLeftArm": "leftArm", "bipedRightLeg": "rightLeg", "bipedLeftLeg": "leftLeg"}
    for condition in [None, "inner", "outer"]:
        selected = [(parent, child) for parent, child, when in children if when == condition]
        if not selected:
            continue
        indent = "        "
        if condition:
            lines.append("        if (f < 1.0F) {" if condition == "inner" else "        if (f >= 1.0F) {")
            indent += "    "
        for parent, child in selected:
            lines.append(part_code(variables[parent], child, parts[child], indent))
        if condition:
            lines.append("        }")
    lines += ["        return LayerDefinition.create(mesh, 128, 64);", "    }"]
    paths = []
    for parent, child, _ in children:
        if parent == "bipedHeadwear":
            path = "root.getChild(\"hat\")"
        else:
            path = f"root.getChild(\"{BIPEDS[parent]}\")"
        paths.append((snake(child), path))
    lines += [
        "",
        f"    public static Map<String, ModelPart> {key}Parts(ModelPart root) {{",
        "        Map<String, ModelPart> parts = new HashMap<>();",
    ]
    for child, path in paths:
        lines.append(f"        if ({path}.hasChild(\"{child}\")) {{")
        lines.append(f"            parts.put(\"{child}\", {path}.getChild(\"{child}\"));")
        lines.append("        }")
    lines += ["        return parts;", "    }"]
    return lines


def main():
    lines = [
        "package thaumcraft.client.render.armor;",
        "",
        "import java.util.HashMap;",
        "import java.util.Map;",
        "import net.minecraft.client.model.geom.ModelPart;",
        "import net.minecraft.client.model.geom.PartPose;",
        "import net.minecraft.client.model.geom.builders.CubeDeformation;",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "import net.minecraft.client.model.geom.builders.LayerDefinition;",
        "import net.minecraft.client.model.geom.builders.MeshDefinition;",
        "import net.minecraft.client.model.geom.builders.PartDefinition;",
        "",
        "public final class ArmorLayers {",
        "    private ArmorLayers() {",
        "    }",
    ]
    for key, name in MODELS.items():
        lines.append("")
        lines += layer_method(key, name)
    lines.append("}")
    text = "\n".join(lines) + "\n"
    if "new CubeDeformation(" not in text:
        text = text.replace("import net.minecraft.client.model.geom.builders.CubeDeformation;\n", "")
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    TARGET.write_text(text, encoding="utf-8")
    print(f"{TARGET}: {', '.join(MODELS)}")


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--output-root", type=Path, default=ROOT, help="project-layout output directory")
    args = parser.parse_args()
    TARGET = args.output_root / TARGET.relative_to(ROOT)
    main()
