package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ScanClientHooks;

public final class AspectTooltips {
    private AspectTooltips() {
    }

    public static boolean isDiscovered(Aspect aspect) {
        Player player = ScanClientHooks.localPlayer();
        return aspect.isEnabled() && (player == null || Knowledge.get(player).hasDiscoveredAspect(aspect));
    }

    public static AspectList getAspects(ItemStack stack) {
        return AspectKeys.ASPECTS.getOrDefault(stack, AspectList.EMPTY);
    }

    public static void setSingle(ItemStack stack, Aspect aspect, int amount) {
        AspectKeys.ASPECTS.set(stack, new AspectList().add(aspect, amount));
    }

    public static void append(ItemStack stack, Consumer<Component> builder, String separator, boolean withAmount) {
        AspectList aspects = getAspects(stack);
        for (Aspect aspect : aspects.getAspectsSorted()) {
            if (!aspect.isEnabled()) continue;
            if (!isDiscovered(aspect)) {
                builder.accept(Component.translatable("tc.aspect.unknown"));
            } else if (withAmount) {
                builder.accept(Component.literal(aspect.getName() + separator + aspects.getAmount(aspect)));
            } else {
                builder.accept(Component.literal(aspect.getName()));
            }
        }
    }
}
