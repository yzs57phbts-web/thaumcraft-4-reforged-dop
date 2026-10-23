package thaumcraft.research;

import net.minecraft.world.entity.player.Player;

public final class ResearchGate {
    private ResearchGate() {
    }

    public static boolean isComplete(Player player, String key) {
        return ResearchManager.isComplete(player, key);
    }
}
