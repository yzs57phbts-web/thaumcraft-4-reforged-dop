import shutil

NS = "thaumcraft"

FACINGS = ["down", "up", "north", "south", "west", "east"]

CAPS = {
    "iron": "wand_cap_iron",
    "gold": "wand_cap_gold",
    "thaumium": "wand_cap_thaumium",
    "copper": "wand_cap_copper",
    "silver": "wand_cap_silver",
    "inert_silver": "wand_cap_silver_inert",
    "inert_thaumium": "wand_cap_thaumium_inert",
    "void": "wand_cap_void",
    "inert_void": "wand_cap_void_inert",
}

RODS = {
    "greatwood": "greatwood",
    "obsidian": "obsidian",
    "silverwood": "silverwood",
    "icy": "ice",
    "quartz": "quartz",
    "reed": "reed",
    "blazing": "blaze",
    "bone": "bone",
}


def particle_model(ctx, name, texture, kind="block"):
    ctx.block_model(name, {"parent": "minecraft:block/block", "textures": {"particle": ctx.ref(texture, kind)}})


def special_item(ctx, name, model, base=None):
    ctx.special_item(name, base or ctx.ref(name, "item"), model)


LEGACY_VIEWS = [
    ("inventory", ["gui"]),
    ("equipped", ["thirdperson_lefthand", "thirdperson_righthand"]),
    ("first_person", ["firstperson_lefthand", "firstperson_righthand"]),
]


def legacy_special_item(ctx, name, model, particle, kind="minecraft:special"):
    ctx.item_model(name, {"gui_light": "side", "textures": {"particle": ctx.ref(particle, "item")}})
    ctx.view_select_item(name, ctx.ref(name, "item"), model, LEGACY_VIEWS, "entity", kind)


def item_base_from_block(ctx, name):
    ctx.item_model(name, {"parent": ctx.ref(name)})


def item_base_particle(ctx, name, texture, kind="block"):
    ctx.item_model(name, {"parent": "minecraft:block/block", "textures": {"particle": ctx.ref(texture, kind)}})


def box(ctx, start, end, faces):
    return {"from": start, "to": end, "faces": faces}


def nodes(ctx):
    for name in ["aura_node", "energized_aura_node"]:
        particle_model(ctx, name, "blank", "item")
        ctx.simple_state(name)
        ctx.write_loot(name, [])
    item_base_particle(ctx, "aura_node", "blank", "item")
    special_item(ctx, "aura_node", {"type": f"{NS}:aura_node"})

    ctx.simple_state("charged_obsidian_totem", ctx.ref("obsidian_totem"))
    ctx.block_item("charged_obsidian_totem", model=ctx.ref("obsidian_totem"))
    ctx.self_drop("charged_obsidian_totem")
    ctx.mineable("pickaxe", "charged_obsidian_totem")

    ctx.cube_column("silverwood_knot", "silverwoodknottop", "silverwoodknotside")
    ctx.block_model("silverwood_knot_horizontal", {
        "parent": "minecraft:block/cube_column_horizontal",
        "textures": {"end": ctx.ref("silverwoodknottop"), "side": ctx.ref("silverwoodknotside")},
    })
    ctx.blockstate("silverwood_knot", {"variants": {
        "axis=x": {"model": ctx.ref("silverwood_knot_horizontal"), "x": 90, "y": 90},
        "axis=y": {"model": ctx.ref("silverwood_knot")},
        "axis=z": {"model": ctx.ref("silverwood_knot_horizontal"), "x": 90},
    }})
    ctx.write_loot("silverwood_knot", [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{NS}:silverwood_log"}],
        "condition": {"type": "minecraft:survives_explosion"},
    }])
    ctx.mineable("axe", "silverwood_knot")
    ctx.tag_both("minecraft:logs", "silverwood_knot")
    ctx.tag_both("minecraft:logs_that_burn", "silverwood_knot")


def devices(ctx):
    for name, model in [
        ("node_stabilizer", {"type": f"{NS}:node_stabilizer", "advanced": False}),
        ("advanced_node_stabilizer", {"type": f"{NS}:node_stabilizer", "advanced": True}),
        ("node_transducer", {"type": f"{NS}:node_transducer"}),
        ("vis_relay", {"type": f"{NS}:vis_relay"}),
    ]:
        texture = "thaumiumblock" if name == "vis_relay" else "pedestal_top"
        particle_model(ctx, name, texture)
        item_base_particle(ctx, name, texture)
        special_item(ctx, name, model)
        ctx.self_drop(name)
        ctx.mineable("pickaxe", name)
    for name in ["node_stabilizer", "advanced_node_stabilizer", "node_transducer"]:
        ctx.simple_state(name)
    ctx.blockstate("vis_relay", {"variants": {
        f"connected={connected},facing={facing}": {"model": ctx.ref("vis_relay")}
        for facing in FACINGS
        for connected in ["false", "true"]
    }})

    ctx.block_model("wand_recharge_pedestal", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ctx.ref("wandpedestal_side"), "side": ctx.ref("wandpedestal_side"), "top": ctx.ref("wandpedestal_top")},
        "elements": [
            box(ctx, [0, 0, 0], [16, 4, 16], {
                "down": {"texture": "#top", "cullface": "down"}, "up": {"texture": "#top"},
                "north": {"texture": "#side", "cullface": "north"}, "south": {"texture": "#side", "cullface": "south"},
                "west": {"texture": "#side", "cullface": "west"}, "east": {"texture": "#side", "cullface": "east"},
            }),
            box(ctx, [2, 4, 2], [14, 8, 14], {
                "up": {"texture": "#top"}, "north": {"texture": "#side"}, "south": {"texture": "#side"},
                "west": {"texture": "#side"}, "east": {"texture": "#side"},
            }),
            box(ctx, [4, 8, 4], [12, 16, 12], {
                "up": {"texture": "#top", "cullface": "up"}, "north": {"texture": "#side"}, "south": {"texture": "#side"},
                "west": {"texture": "#side"}, "east": {"texture": "#side"},
            }),
        ],
    })
    ctx.block_model("compound_recharge_focus", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ctx.ref("wandpedestal_focus_side"),
            "bottom": ctx.ref("wandpedestal_focus_bot"),
            "top": ctx.ref("wandpedestal_focus_top"),
            "side": ctx.ref("wandpedestal_focus_side"),
        },
        "elements": [box(ctx, start, end, {
            "down": {"texture": "#bottom"}, "up": {"texture": "#top"},
            "north": {"texture": "#side"}, "south": {"texture": "#side"},
            "west": {"texture": "#side"}, "east": {"texture": "#side"},
        }) for start, end in [
            ([5, 0, 5], [11, 1, 11]),
            ([1, 0, 7], [5, 1, 9]),
            ([11, 0, 7], [15, 1, 9]),
            ([7, 0, 1], [9, 1, 5]),
            ([7, 0, 11], [9, 1, 15]),
            ([1, 1, 7], [3, 7, 9]),
            ([7, 1, 1], [9, 7, 3]),
            ([13, 1, 7], [15, 7, 9]),
            ([7, 1, 13], [9, 7, 15]),
        ]],
    })
    for name in ["wand_recharge_pedestal", "compound_recharge_focus"]:
        ctx.simple_state(name)
        ctx.block_item(name)
        ctx.self_drop(name)
        ctx.mineable("pickaxe", name)


def jar(ctx):
    ctx.block_model("node_jar", {"parent": ctx.ref("warded_jar")})
    ctx.simple_state("node_jar")
    item_base_from_block(ctx, "node_jar")
    special_item(ctx, "node_jar", {"type": f"{NS}:node_jar"})


def wands(ctx):
    legacy_special_item(ctx, "wand", {"type": f"{NS}:wand"}, "wand_rod_greatwood", f"{NS}:special_owner")
    for cap, texture in CAPS.items():
        ctx.generated_item(f"{cap}_wand_cap", texture)
    for rod, texture in RODS.items():
        ctx.generated_item(f"{rod}_rod", f"wand_rod_{texture}")
        ctx.generated_item(f"{rod}_staff_core", f"staff_rod_{texture}")
    ctx.generated_item("primal_staff_core", "staff_rod_primal")
    legacy_special_item(ctx, "thaumometer", {"type": f"{NS}:thaumometer"}, "blank")
    ctx.generated_item("goggles_of_revealing", "gogglesrevealing")
    if ctx.profile.modern:
        ctx.write(ctx.ASSETS / "equipment" / "goggles.json", {
            "layers": {"humanoid": [{"texture": f"{NS}:goggles"}]},
        })
    else:
        target = ctx.ASSETS / "textures" / "models" / "armor" / "goggles_layer_1.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ctx.SOURCE_TEXTURES / "entity" / "equipment" / "humanoid" / "goggles.png", target)
        ctx.MANIFEST["armor_layers"]["goggles"] = {
            "layer_id": f"{NS}:goggles",
            "texture": f"{NS}:textures/models/armor/goggles_layer_1.png",
            "meaning": "ArmorMaterial.Layer(ResourceLocation thaumcraft:goggles) resolves to textures/models/armor/goggles_layer_1.png; the png is generated into the resources from the 26.3 humanoid equipment texture",
        }


def recipes(ctx):
    ctx.shaped("iron_wand_cap", f"{NS}:iron_wand_cap", 1, ["NNN", "N N"], {"N": "#c:nuggets/iron"})
    ctx.shaped(
        "wand_iron_wood", f"{NS}:wand", 1, ["  I", " S ", "I  "],
        {"I": f"{NS}:iron_wand_cap", "S": "#c:rods/wooden"},
        components={f"{NS}:wand_parts": {"cap": "iron", "rod": "wood"}},
    )


def generate(ctx):
    nodes(ctx)
    devices(ctx)
    jar(ctx)
    wands(ctx)
    recipes(ctx)
