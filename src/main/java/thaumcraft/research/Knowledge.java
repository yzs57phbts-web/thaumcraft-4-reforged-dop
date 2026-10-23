package thaumcraft.research;

import net.minecraft.world.entity.player.Player;
import thaumcraft.platform.DataKey;

public final class Knowledge {
    public static final DataKey<PlayerKnowledge> KEY = DataKey.builder("knowledge", PlayerKnowledge::new)
        .codec(PlayerKnowledge.CODEC)
        .copyOnDeath()
        .syncToSelf(PlayerKnowledge.STREAM_CODEC)
        .build();

    private Knowledge() {
    }

    public static void init() {
    }

    public static PlayerKnowledge get(Player player) {
        return KEY.get(player);
    }

    public static void sync(Player player) {
        KEY.sync(player);
    }
}
