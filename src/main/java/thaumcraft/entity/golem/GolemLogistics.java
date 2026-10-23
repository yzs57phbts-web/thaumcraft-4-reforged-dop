package thaumcraft.entity.golem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.platform.Transfer;

final class GolemLogistics {
    static final int GOLEM_DELAY = 5;

    private GolemLogistics() {
    }

    static BlockPos homeContainer(GolemBase golem) {
        return golem.getRestrictCenter().relative(golem.getHomeFacing().getOpposite());
    }

    static List<ItemStack> filters(GolemBase golem) {
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < golem.inventory().getContainerSize(); slot++) {
            ItemStack stack = golem.inventory().getItem(slot);
            if (!stack.isEmpty()) {
                result.add(stack.copy());
            }
        }
        return result;
    }

    static Predicate<ItemStack> matches(GolemBase golem, ItemStack filter) {
        return stack -> golem.matchesFilter(filter, stack);
    }

    static List<Integer> colors(GolemBase golem, ItemStack stack) {
        return golem.getColorsMatching(stack);
    }

    static boolean matchesColor(GolemBase golem, Marker marker, ItemStack stack) {
        for (int color : colors(golem, stack)) {
            if (color == -1 || marker.color() == color) {
                return true;
            }
        }
        return false;
    }

    static List<Marker> markedItemHandlers(GolemBase golem, Predicate<Marker> filter) {
        List<Marker> result = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (marker.inLevel(golem.level()) && filter.test(marker) && Transfer.hasItems(golem.level(), marker.pos(), marker.side())) {
                result.add(marker);
            }
        }
        return result;
    }

    static List<ItemStack> neededAtHome(GolemBase golem) {
        List<ItemStack> result = filters(golem);
        if (!golem.getToggles()[0] && !Transfer.hasItems(golem.level(), homeContainer(golem), golem.getHomeFacing())) return List.of();
        if (golem.getToggles()[0]) {
            return result;
        }
        BlockPos home = homeContainer(golem);
        Direction side = golem.getHomeFacing();
        Level level = golem.level();
        List<ItemStack> missing = new ArrayList<>();
        for (ItemStack filter : result) {
            int stored = Transfer.count(level, home, side, matches(golem, filter));
            int amount = required(golem, filter) - stored;
            if (amount > 0) {
                missing.add(filter.copyWithCount(amount));
            }
        }
        return missing;
    }

    static int missingAtHome(GolemBase golem, ItemStack filter) {
        if (golem.getToggles()[0]) {
            return golem.getCarrySpace();
        }
        int stored = Transfer.count(golem.level(), homeContainer(golem), golem.getHomeFacing(), matches(golem, filter));
        return Math.max(0, required(golem, filter) - stored);
    }

    private static int required(GolemBase golem, ItemStack stack) {
        int total = 0;
        for (ItemStack filter : filters(golem)) {
            if (ItemStack.isSameItemSameComponents(filter, stack) || golem.getUpgradeAmount(5) > 0 && (filter.is(stack.getItem()) && filter.getDamageValue() == stack.getDamageValue() || golem.sharesOreTag(filter, stack))) total += filter.getCount();
        }
        return total;
    }

    static boolean closeTo(GolemBase golem, BlockPos pos, double distanceSquared) {
        return golem.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= distanceSquared;
    }

    static boolean isHomeBlock(GolemBase golem, BlockPos pos) {
        return homeContainer(golem).equals(pos);
    }
}
