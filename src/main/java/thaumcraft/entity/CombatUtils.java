package thaumcraft.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.wand.Wands;

public final class CombatUtils {
    private CombatUtils() {
    }

    public static void launch(Projectile projectile, LivingEntity shooter, float speed, float scatter) {
        launch(projectile, shooter, speed);
        Vec3 movement = projectile.getDeltaMovement();
        aim(projectile, movement.x, movement.y, movement.z, speed, scatter);
    }

    public static void launch(Projectile projectile, LivingEntity shooter, float speed) {
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        double x = shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = shooter.getEyeY() - 0.1F;
        double z = shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        projectile.setOwner(shooter);
        projectile.moveTo(x, y, z, yaw, pitch);
        double motionX = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double motionZ = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double motionY = -Mth.sin(pitch / 180.0F * (float) Math.PI);
        aim(projectile, motionX, motionY, motionZ, speed, 1.0F);
    }

    public static void aim(Projectile projectile, double x, double y, double z, float speed, float scatter) {
        Vec3 direction = new Vec3(x, y, z).normalize();
        direction = direction.add(
            projectile.getRandom().nextGaussian() * 0.0075F * scatter,
            projectile.getRandom().nextGaussian() * 0.0075F * scatter,
            projectile.getRandom().nextGaussian() * 0.0075F * scatter
        ).scale(speed);
        projectile.setDeltaMovement(direction);
        double horizontal = direction.horizontalDistance();
        projectile.setYRot((float) (Mth.atan2(direction.x, direction.z) * 180.0 / Math.PI));
        projectile.setXRot((float) (Mth.atan2(direction.y, horizontal) * 180.0 / Math.PI));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }

    public static void startUsingWand(Player player, ItemStack wand) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand) == wand) {
                player.startUsingItem(hand);
                return;
            }
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (Wands.isWand(player.getItemInHand(hand))) {
                player.startUsingItem(hand);
                return;
            }
        }
    }

    public static @Nullable Entity getPointedEntity(Level level, Entity source, double minRange, double range, float padding) {
        return getPointedEntity(level, source, minRange, range, padding, false, null);
    }

    public static @Nullable Entity getPointedEntity(Level level, Entity source, double range, Class<? extends Entity> excluded) {
        return getPointedEntity(level, source, 0.0, range, 1.1F, false, excluded);
    }

    public static @Nullable Entity getPointedEntity(
        Level level,
        Entity source,
        double minRange,
        double range,
        float padding,
        boolean nonCollide,
        @Nullable Class<? extends Entity> excluded
    ) {
        Entity pointed = null;
        Vec3 start = source.getEyePosition();
        Vec3 look = source.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
        AABB area = source.getBoundingBox().expandTowards(look.x * range, look.y * range, look.z * range).inflate(padding);
        double best = 0.0;
        for (Entity entity : level.getEntities(source, area)) {
            if (entity.distanceTo(source) < minRange) {
                continue;
            }
            if (!(entity.isPickable() || nonCollide) || (excluded != null && excluded.isInstance(entity))) {
                continue;
            }
            if (level.clip(new ClipContext(start, entity.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).getType() != HitResult.Type.MISS) {
                continue;
            }
            float border = Math.max(0.8F, entity.getPickRadius());
            AABB box = entity.getBoundingBox().inflate(border);
            if (box.contains(start)) {
                pointed = entity;
                best = 0.0;
            } else {
                Vec3 hit = box.clip(start, end).orElse(null);
                if (hit != null) {
                    double distance = start.distanceTo(hit);
                    if (distance < best || best == 0.0) {
                        pointed = entity;
                        best = distance;
                    }
                }
            }
        }
        return pointed;
    }

    public static List<Entity> getEntitiesInRange(Level level, double x, double y, double z, @Nullable Entity exclude, Class<? extends Entity> type, double range) {
        List<Entity> result = new ArrayList<>();
        AABB area = new AABB(x, y, z, x, y, z).inflate(range);
        for (Entity entity : level.getEntitiesOfClass(type, area)) {
            if (exclude == null || exclude.getId() != entity.getId()) {
                result.add(entity);
            }
        }
        return result;
    }

    public static boolean canEntityBeSeen(Entity entity, Entity other) {
        return canSee(entity.level(), entity.position(), other.position(), entity);
    }

    public static boolean canEntityBeSeen(Entity entity, double x, double y, double z) {
        return canSee(entity.level(), new Vec3(x, y, z), entity.position(), entity);
    }

    private static boolean canSee(Level level, Vec3 from, Vec3 to, Entity context) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context)).getType() == HitResult.Type.MISS;
    }
}
