package thaumcraft.platform;

import net.minecraft.server.level.ServerPlayer;

public final class FlightGuard {
    private FlightGuard() {
    }

    public static void reset(ServerPlayer player) {
        player.connection.aboveGroundTickCount = 0;
    }
}
