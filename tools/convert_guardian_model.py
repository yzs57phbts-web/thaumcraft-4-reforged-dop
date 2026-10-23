import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_SOURCE = ROOT / "reference" / "decompiled" / "thaumcraft" / "client" / "renderers" / "models" / "entities" / "ModelEldritchGuardian.java"
TARGET = ROOT / "src" / "main" / "java" / "thaumcraft" / "client" / "render" / "entity" / "eldritch" / "EldritchGuardianModel.java"
DECLARATION = re.compile(r"this\.(\w+) = new ModelRenderer\(this, (-?\d+), (-?\d+)\);")


def number(value):
    value = value.strip().replace("Math.PI", "3.141592653589793")
    value = value.removeprefix("(float)").strip().strip("()")
    return float(eval(value.rstrip("F").replace("F", ""), {"__builtins__": {}}))


def snake(name):
    return re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name).lower()


def camel(name):
    head, *tail = snake(name).split("_")
    return head + "".join(value.capitalize() for value in tail)


def constructor(source):
    start = source.index("public ModelEldritchGuardian() {")
    start = source.index("{", start)
    depth = 0
    for index in range(start, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start + 1:index]
    raise ValueError("unterminated ModelEldritchGuardian constructor")


def parse(source):
    parts = {}
    children = []
    for raw in constructor(source).splitlines():
        line = raw.strip()
        if not line:
            continue
        if match := DECLARATION.fullmatch(line):
            parts[match.group(1)] = {
                "texture": (int(match.group(2)), int(match.group(3))),
                "mirror": False,
                "boxes": [],
                "pivot": (0.0, 0.0, 0.0),
                "rotation": (0.0, 0.0, 0.0),
            }
        elif match := re.fullmatch(r"this\.(\w+)\.mirror = (true|false);", line):
            parts[match.group(1)]["mirror"] = match.group(2) == "true"
        elif match := re.fullmatch(r"this\.(\w+)\.addBox\((.*)\);", line):
            parts[match.group(1)]["boxes"].append(tuple(number(value) for value in match.group(2).split(",")))
        elif match := re.fullmatch(r"this\.(\w+)\.setRotationPoint\((.*)\);", line):
            parts[match.group(1)]["pivot"] = tuple(number(value) for value in match.group(2).split(","))
        elif match := re.fullmatch(r"this\.setRotation\(this\.(\w+), (.*)\);", line):
            parts[match.group(1)]["rotation"] = tuple(number(value) for value in re.split(r",\s*(?![^()]*\))", match.group(2)))
        elif match := re.fullmatch(r"this\.(\w+)\.addChild\(this\.(\w+)\);", line):
            children.append((match.group(1), match.group(2)))
        elif re.fullmatch(r"this\.texture(Width|Height) = \d+;|this\.\w+\.setTextureSize\(\d+, \d+\);", line):
            continue
        else:
            raise ValueError(f"unhandled guardian model statement: {line}")
    return parts, children


def float_literal(value):
    return f"{value:.9g}F"


def part_expression(part):
    builder = f"CubeListBuilder.create().texOffs({part['texture'][0]}, {part['texture'][1]})"
    if part["mirror"]:
        builder += ".mirror()"
    for box in part["boxes"]:
        values = ", ".join(float_literal(value) for value in box[:6])
        if len(box) > 6:
            values += f", new CubeDeformation({float_literal(box[6])})"
        builder += f".addBox({values})"
    values = (*part["pivot"], *part["rotation"])
    return builder, values


def emit_tree(parent_variable, name, parts, child_map, emitted):
    if name in emitted or name == "HoodEye":
        return []
    emitted.add(name)
    part = parts[name]
    variable = camel(name)
    builder, values = part_expression(part)
    pose = ", ".join(float_literal(value) for value in values)
    lines = [f"        PartDefinition {variable} = {parent_variable}.addOrReplaceChild(\"{snake(name)}\", {builder}, PartPose.offsetAndRotation({pose}));"]
    for child in child_map.get(name, []):
        lines.extend(emit_tree(variable, child, parts, child_map, emitted))
    return lines


def generate(source):
    parts, children = parse(source)
    child_names = {child for _, child in children}
    child_map = {}
    for parent, child in children:
        child_map.setdefault(parent, []).append(child)
    roots = [name for name in parts if name not in child_names and name != "HoodEye"]
    lines = [
        "package thaumcraft.client.render.entity.eldritch;",
        "",
        "import net.minecraft.client.model.EntityModel;",
        "import net.minecraft.client.model.geom.ModelPart;",
        "import net.minecraft.client.model.geom.PartPose;",
        "import net.minecraft.client.model.geom.builders.CubeDeformation;",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "import net.minecraft.client.model.geom.builders.LayerDefinition;",
        "import net.minecraft.client.model.geom.builders.MeshDefinition;",
        "import net.minecraft.client.model.geom.builders.PartDefinition;",
        "import net.minecraft.util.Mth;",
        "import thaumcraft.Thaumcraft;",
        "",
        "public class EldritchGuardianModel extends EntityModel<GuardianRenderState> {",
        "    public static final net.minecraft.client.model.geom.ModelLayerLocation LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Thaumcraft.id(\"eldritch_guardian\"), \"main\");",
        "    private final ModelPart hood1;",
        "    private final ModelPart armL1;",
        "    private final ModelPart armR1;",
        "    private final ModelPart legpanelC1;",
        "    private final ModelPart legpanelC2;",
        "    private final ModelPart legpanelC3;",
        "    private final ModelPart cloak1;",
        "    private final ModelPart cloak2;",
        "    private final ModelPart cloak3;",
        "    private final ModelPart sidepanelL2;",
        "    private final ModelPart sidepanelL3;",
        "    private final ModelPart sidepanelL4;",
        "    private final ModelPart sidepanelR2;",
        "    private final ModelPart sidepanelR3;",
        "    private final ModelPart sidepanelR4;",
        "",
        "    public EldritchGuardianModel(ModelPart root) {",
        "        super(root);",
        "        this.hood1 = root.getChild(\"hood1\");",
        "        this.armL1 = root.getChild(\"arm_l1\");",
        "        this.armR1 = root.getChild(\"arm_r1\");",
        "        this.legpanelC1 = root.getChild(\"legpanel_c1\");",
        "        this.legpanelC2 = this.legpanelC1.getChild(\"legpanel_c2\");",
        "        this.legpanelC3 = this.legpanelC2.getChild(\"legpanel_c3\");",
        "        this.cloak1 = root.getChild(\"cloak1\");",
        "        this.cloak2 = this.cloak1.getChild(\"cloak2\");",
        "        this.cloak3 = this.cloak2.getChild(\"cloak3\");",
        "        this.sidepanelL2 = root.getChild(\"sidepanel_l2\");",
        "        this.sidepanelL3 = this.sidepanelL2.getChild(\"sidepanel_l3\");",
        "        this.sidepanelL4 = this.sidepanelL3.getChild(\"sidepanel_l4\");",
        "        this.sidepanelR2 = root.getChild(\"sidepanel_r2\");",
        "        this.sidepanelR3 = this.sidepanelR2.getChild(\"sidepanel_r3\");",
        "        this.sidepanelR4 = this.sidepanelR3.getChild(\"sidepanel_r4\");",
        "    }",
        "",
        "    public static LayerDefinition createBodyLayer() {",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
    ]
    emitted = set()
    for name in roots:
        lines.extend(emit_tree("root", name, parts, child_map, emitted))
    if emitted != set(parts) - {"HoodEye"}:
        raise ValueError(f"unattached guardian model parts: {sorted(set(parts) - emitted - {'HoodEye'})}")
    lines += [
        "        return LayerDefinition.create(mesh, 128, 64);",
        "    }",
        "",
        "    @Override",
        "    public void setupAnim(GuardianRenderState state) {",
        "        this.hood1.yRot = state.yRot * ((float) Math.PI / 180.0F);",
        "        this.hood1.xRot = state.xRot * ((float) Math.PI / 180.0F);",
        "        this.armL1.xRot = -1.0F - state.armLiftLeft + Mth.sin((state.ageInTicks + 20.0F) / 10.0F) * 0.08F;",
        "        this.armR1.xRot = -1.0F - state.armLiftRight + Mth.sin(state.ageInTicks / 10.0F) * 0.08F;",
        "        this.legpanelC1.xRot = -0.15F + Mth.sin(state.ageInTicks / 8.0F) * 0.12F;",
        "        this.legpanelC2.xRot = Mth.sin((state.ageInTicks - 5.0F) / 8.0F) * 0.13F;",
        "        this.legpanelC3.xRot = Mth.sin((state.ageInTicks - 10.0F) / 8.0F) * 0.14F;",
        "        this.cloak1.xRot = 0.2F + Mth.sin(state.ageInTicks / 7.0F) * 0.08F;",
        "        this.cloak2.xRot = Mth.sin((state.ageInTicks - 5.0F) / 7.0F) * 0.1F;",
        "        this.cloak3.xRot = Mth.sin((state.ageInTicks - 10.0F) / 7.0F) * 0.12F;",
        "        this.sidepanelL2.zRot = -0.2F + Mth.sin((state.ageInTicks + 10.0F) / 8.0F) * 0.12F;",
        "        this.sidepanelL3.zRot = Mth.sin((state.ageInTicks + 5.0F) / 8.0F) * 0.13F;",
        "        this.sidepanelL4.zRot = Mth.sin(state.ageInTicks / 8.0F) * 0.14F;",
        "        this.sidepanelR2.zRot = 0.2F + Mth.sin((state.ageInTicks - 5.0F) / 8.0F) * 0.12F;",
        "        this.sidepanelR3.zRot = Mth.sin((state.ageInTicks - 10.0F) / 8.0F) * 0.13F;",
        "        this.sidepanelR4.zRot = Mth.sin((state.ageInTicks - 15.0F) / 8.0F) * 0.14F;",
        "    }",
        "}",
    ]
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE)
    parser.add_argument("--stdout", action="store_true")
    args = parser.parse_args()
    content = generate(args.source.read_text(encoding="utf-8"))
    if args.stdout:
        sys.stdout.write(content)
    else:
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        TARGET.write_text(content, encoding="utf-8")
        print(f"{TARGET.relative_to(ROOT)}: guardian geometry and animation")


if __name__ == "__main__":
    main()
