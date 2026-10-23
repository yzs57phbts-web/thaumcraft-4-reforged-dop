package thaumcraft.item.tool;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.platform.FollowingItemMotion;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class FollowingItems {
    private static final int START_AGE = 20;
    private static final double ATTRACT_RANGE = 2.0;
    private static final double ARRIVE_DISTANCE = 0.5;
    private static final Map<ItemEntity, Follow> FOLLOWING = new WeakHashMap<>();
    private static final String SAVED_GRAVITY = "thaumcraft:following_gravity";

    private static final class Follow {
        private final Entity target;
        private int age = START_AGE;

        private Follow(Entity target) {
            this.target = target;
        }
    }

    private FollowingItems() {
    }

    public static boolean isFollowing(ItemEntity item) {
        return item.level().isClientSide()
            ? ToolKeys.FOLLOWING.has(item) && ToolKeys.FOLLOWING.get(item)
            : FOLLOWING.containsKey(item);
    }

    public static void follow(ItemEntity item, Entity target) {
        if (item.level().isClientSide() || isFollowing(item)) {
            return;
        }
        FOLLOWING.put(item, new Follow(target));
        item.getPersistentData().putBoolean(SAVED_GRAVITY, item.isNoGravity());
        item.setNoGravity(true);
        item.noPhysics = true;
        item.setDeltaMovement(Vec3.ZERO);
        ToolKeys.FOLLOWING.set(item, true);
        FollowingItemMotion.sync(item);
    }

    @SubscribeEvent
    static void onBlockDrops(BlockDropsEvent event) {
        if (event.getTool().getItem() instanceof PrimalCrusherItem && event.getBreaker() instanceof net.minecraft.world.entity.player.Player player && !player.isShiftKeyDown()) {
            for (ItemEntity item : event.getDrops()) {
                follow(item, player);
            }
        }
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity item && !isFollowing(item) && item.getPersistentData().contains(SAVED_GRAVITY)) {
            release(item);
        }
    }

    public static void attractNear(ServerLevel level, BlockPos pos, Entity target) {
        AABB area = new AABB(pos).inflate(ATTRACT_RANGE - 0.5);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.isRemoved() && item.tickCount == 0 && !isFollowing(item)) {
                follow(item, target);
            }
        }
    }

    @SubscribeEvent
    static void onItemTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }
        if (item.level().isClientSide()) {
            if (isFollowing(item)) {
                event.setCanceled(true);
                FollowingItemMotion.tick(item, item.getDeltaMovement());
            }
            return;
        }
        Follow follow = FOLLOWING.get(item);
        if (follow == null) {
            return;
        }
        Entity target = follow.target;
        if (target == null || target.isRemoved() || target.level() != item.level()) {
            release(item);
            return;
        }
        Vec3 goal = new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ());
        Vec3 offset = goal.subtract(item.position());
        if (follow.age > 1) {
            follow.age--;
        }
        double distance = offset.length();
        if (distance > ARRIVE_DISTANCE) {
            event.setCanceled(true);
            FollowingItemMotion.tick(item, offset.scale(1.0 / (distance * follow.age)));
        } else {
            item.setDeltaMovement(item.getDeltaMovement().scale(0.1));
            release(item);
        }
    }

    private static void release(ItemEntity item) {
        FOLLOWING.remove(item);
        item.setNoGravity(item.getPersistentData().getBoolean(SAVED_GRAVITY));
        item.getPersistentData().remove(SAVED_GRAVITY);
        item.noPhysics = false;
        ToolKeys.FOLLOWING.set(item, false);
        FollowingItemMotion.sync(item);
    }
}
