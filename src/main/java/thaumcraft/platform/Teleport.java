package thaumcraft.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public final class Teleport {
    private Teleport() {
    }

    public static @Nullable Entity to(Entity entity, ServerLevel level, Vec3 position, float yaw, float pitch) {
        return entity.changeDimension(new DimensionTransition(level, position, Vec3.ZERO, yaw, pitch, DimensionTransition.DO_NOTHING));
    }
}
