package thaumcraft.platform;

import net.minecraft.world.entity.Entity;

public final class EntityMountPlatform {
    private EntityMountPlatform() {
    }

    public static boolean startRiding(Entity passenger, Entity vehicle, boolean force, boolean sendEventAndTriggers) {
        return passenger != vehicle && passenger.startRiding(vehicle, force);
    }
}
