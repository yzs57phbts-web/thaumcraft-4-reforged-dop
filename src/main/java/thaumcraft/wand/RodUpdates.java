package thaumcraft.wand;

import java.util.ArrayList;
import java.util.List;
import thaumcraft.aspect.Aspect;

public final class RodUpdates {
    private RodUpdates() {
    }

    public static RodUpdate single(Aspect aspect) {
        return (stack, player) -> {
            if (player.tickCount % 200 == 0 && Wands.getVis(stack, aspect) < Wands.getMaxVis(stack) / 10) {
                Wands.addVis(stack, aspect, 1, true);
            }
        };
    }

    public static RodUpdate primal() {
        return (stack, player) -> {
            if (player.tickCount % 50 != 0) {
                return;
            }
            List<Aspect> low = new ArrayList<>();
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (Wands.getVis(stack, aspect) < Wands.getMaxVis(stack) / 10) {
                    low.add(aspect);
                }
            }
            if (!low.isEmpty()) {
                Wands.addVis(stack, low.get(player.getRandom().nextInt(low.size())), 1, true);
            }
        };
    }
}
