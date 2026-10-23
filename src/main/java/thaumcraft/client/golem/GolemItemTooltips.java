package thaumcraft.client.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.golem.GolemKeys;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.item.golem.GolemItems;
import thaumcraft.item.golem.GolemPlacerItem;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class GolemItemTooltips {
    private static final String[] DECORATIONS = {"top_hat", "spectacles", "bowtie", "fez", "dart_launcher", "visor", "iron_plating", "mace_arm"};
    private static final String[] FLAGS = {"H", "G", "B", "F", "R", "V", "P", "M"};

    private GolemItemTooltips() {
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        if (stack.getItem() instanceof GolemPlacerItem) {
            addGolem(lines, GolemKeys.snapshot(stack));
        }
        Integer upgrade = GolemKeys.TRUNK_UPGRADE.get(stack);
        if (upgrade != null && upgrade >= 0 && upgrade < GolemItems.UPGRADES.length) {
            lines.add(Component.translatable("item.thaumcraft.golem_upgrade_" + GolemItems.UPGRADES[upgrade]).withStyle(ChatFormatting.BLUE));
        }
        var contents = GolemKeys.TRUNK_CONTENTS.get(stack);
        if (contents != null && contents.nonEmptyItems().iterator().hasNext()) {
            lines.add(Component.translatable("item.TrunkSpawner.text.1"));
        }
    }

    private static void addGolem(List<Component> lines, GolemSnapshot state) {
        if (state.core() >= 0 && state.core() < GolemItems.CORES.length) {
            lines.add(Component.translatable("item.thaumcraft.golem_core")
                .append(": ")
                .append(Component.translatable("item.thaumcraft.golem_core_" + GolemItems.CORES[state.core()]).withStyle(ChatFormatting.GOLD)));
        }
        if (state.advanced()) {
            lines.add(Component.translatable("tc.adv"));
        }
        List<Component> upgrades = new ArrayList<>();
        for (int upgrade : state.upgrades()) {
            if (upgrade >= 0 && upgrade < GolemItems.UPGRADES.length) {
                upgrades.add(Component.translatable("item.thaumcraft.golem_upgrade_" + GolemItems.UPGRADES[upgrade]));
            }
        }
        if (!upgrades.isEmpty()) {
            Component line = Component.empty().withStyle(ChatFormatting.BLUE);
            for (int index = 0; index < upgrades.size(); index++) {
                if (index > 0) {
                    line = line.copy().append(" ");
                }
                line = line.copy().append(upgrades.get(index));
            }
            lines.add(line);
        }
        if (!state.markers().isEmpty()) {
            lines.add(Component.literal(state.markers().size() + " ").withStyle(ChatFormatting.DARK_PURPLE).append(Component.translatable("tc.markedloc").withStyle(ChatFormatting.DARK_PURPLE)));
        }
        String decoration = state.decoration();
        Component decoLine = Component.empty().withStyle(ChatFormatting.DARK_GREEN);
        boolean hasDecoration = false;
        for (int index = 0; index < FLAGS.length; index++) {
            if (decoration.contains(FLAGS[index])) {
                if (hasDecoration) {
                    decoLine = decoLine.copy().append(" ");
                }
                decoLine = decoLine.copy().append(Component.translatable("item.thaumcraft." + DECORATIONS[index]));
                hasDecoration = true;
            }
        }
        if (hasDecoration) {
            lines.add(decoLine);
        }
    }
}
