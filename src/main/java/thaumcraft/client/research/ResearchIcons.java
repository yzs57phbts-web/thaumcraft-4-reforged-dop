package thaumcraft.client.research;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.BrowserDraw;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.research.ResearchItem;

final class ResearchIcons {
    private ResearchIcons() {
    }

    static void drawFallback(GuiGraphics graphics, ResearchItem research, int x, int y, int tint) {
        if (research.iconTexture() != null) {
            BrowserDraw.full(graphics, research.iconTexture(), x, y, 16, 16, tint);
            return;
        }
        ResourceLocation texture = fallbackTexture(research);
        if (texture != null) {
            int height = texture.getPath().contains("/golem_core_") ? 48 : 16;
            GuiBlit.blit(graphics, texture, x, y, 16, 16, 0, 0, 16, 16, 16, height, tint);
        }
    }

    static @Nullable ResourceLocation fallbackTexture(ResearchItem research) {
        if (research.iconItem() == null || !research.iconItem().item().getNamespace().equals(Thaumcraft.MODID)) {
            return null;
        }
        String texture = switch (research.iconItem().item().getPath()) {
            case "straw_golem" -> "golem_straw";
            case "wood_golem" -> "golem_wood";
            case "tallow_golem" -> "golem_tallow";
            case "clay_golem" -> "golem_clay";
            case "flesh_golem" -> "golem_flesh";
            case "stone_golem" -> "golem_stone";
            case "iron_golem" -> "golem_iron";
            case "thaumium_golem" -> "golem_thaumium";
            case "golem_bell" -> "ironbell";
            case "golem_core_fishing" -> "golem_core_fish";
            case "golem_core_chop" -> "golem_core_lumber";
            case "golem_core_decanting" -> "golem_core_liquid";
            case "golem_core_alchemy" -> "golem_core_essentia";
            case "golem_core_gather", "golem_core_fill", "golem_core_empty", "golem_core_sorting",
                "golem_core_use", "golem_core_harvest", "golem_core_guard", "golem_core_butcher",
                "golem_upgrade_air", "golem_upgrade_earth", "golem_upgrade_fire", "golem_upgrade_water",
                "golem_upgrade_order", "golem_upgrade_entropy" -> research.iconItem().item().getPath();
            case "top_hat" -> "golemdecotophat";
            case "spectacles" -> "golemdecoglasses";
            case "bowtie" -> "golemdecobowtie";
            case "fez" -> "golemdecofez";
            case "dart_launcher" -> "golemdecodart";
            case "visor" -> "golemdecovisor";
            case "iron_plating" -> "golemdecoarmor";
            case "mace_arm" -> "golemdecomace";
            default -> null;
        };
        return texture == null ? null : Thaumcraft.id("textures/item/" + texture + ".png");
    }
}
