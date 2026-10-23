package thaumcraft.entity.monster.taint;

import java.util.Comparator;
import java.util.List;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public final class TaintedVillagerIndoorGoal extends Goal {
    private final TaintedVillager villager;
    @Nullable
    private VillageDoorInfo doorInfo;
    @Nullable
    private BlockPos lastDoor;

    public TaintedVillagerIndoorGoal(TaintedVillager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(this.villager.level() instanceof ServerLevel level) || !isNightRainOrNoPrecipitation(level, this.villager.blockPosition())) {
            return false;
        }
        if (this.villager.getRandom().nextInt(50) != 0) {
            return false;
        }
        BlockPos pos = this.villager.blockPosition();
        if (this.lastDoor != null && this.villager.distanceToSqr(this.lastDoor.getX(), pos.getY(), this.lastDoor.getZ()) < 4.0) {
            return false;
        }
        this.doorInfo = nearestVillageDoor(level, pos, 14);
        return this.doorInfo != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.villager.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.doorInfo == null) {
            return;
        }
        BlockPos inside = this.doorInfo.insidePos();
        if (this.villager.distanceToSqr(inside.getX() + 0.5, inside.getY(), inside.getZ() + 0.5) > 256.0) {
            Vec3 target = DefaultRandomPos.getPosTowards(
                this.villager,
                14,
                3,
                Vec3.atBottomCenterOf(inside),
                (float) (Math.PI / 2.0)
            );
            if (target != null) {
                this.villager.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
            }
        } else {
            this.villager.getNavigation().moveTo(inside.getX() + 0.5, inside.getY(), inside.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void stop() {
        this.lastDoor = this.doorInfo == null ? null : this.doorInfo.insidePos();
        this.doorInfo = null;
    }

    public static boolean isNightRainOrNoPrecipitation(ServerLevel level, BlockPos pos) {
        return (!isDay(level, pos) || level.isRaining() || !level.getBiome(pos).value().hasPrecipitation())
            && level.dimensionType().hasSkyLight();
    }

    public static boolean isDay(ServerLevel level, BlockPos pos) {
        return level.isDay() && level.canSeeSky(pos);
    }

    @Nullable
    public static VillageDoorInfo nearestVillageDoor(ServerLevel level, BlockPos origin, int villageRadius) {
        List<BlockPos> villagePoints = level.getPoiManager()
            .findAll(type -> type.is(PoiTypeTags.VILLAGE), pos -> true, origin, villageRadius, PoiManager.Occupancy.ANY)
            .sorted(Comparator.comparingDouble(origin::distSqr))
            .limit(8)
            .toList();
        if (villagePoints.isEmpty()) {
            return null;
        }
        BlockPos villagePoint = villagePoints.getFirst();
        VillageDoorInfo nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = -villageRadius; x <= villageRadius; x++) {
            for (int z = -villageRadius; z <= villageRadius; z++) {
                for (int y = -3; y <= 3; y++) {
                    BlockPos candidate = villagePoint.offset(x, y, z);
                    var state = level.getBlockState(candidate);
                    if (!(state.getBlock() instanceof DoorBlock) || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
                        || !state.is(BlockTags.WOODEN_DOORS)) {
                        continue;
                    }
                    double distance = origin.distSqr(candidate);
                    if (distance < nearestDistance) {
                        Direction.Axis insideAxis = state.getValue(DoorBlock.FACING).getAxis();
                        int positiveSky = 0;
                        int negativeSky = 0;
                        for (int step = 1; step <= 5; step++) {
                            BlockPos positive = insideAxis == Direction.Axis.X ? candidate.offset(step, 0, 0) : candidate.offset(0, 0, step);
                            BlockPos negative = insideAxis == Direction.Axis.X ? candidate.offset(-step, 0, 0) : candidate.offset(0, 0, -step);
                            if (level.canSeeSky(positive)) {
                                positiveSky++;
                            }
                            if (level.canSeeSky(negative)) {
                                negativeSky++;
                            }
                        }
                        if (positiveSky == negativeSky) {
                            continue;
                        }
                        int insideDirection = positiveSky > negativeSky ? -2 : 2;
                        int insideX = insideAxis == Direction.Axis.X ? insideDirection : 0;
                        int insideZ = insideAxis == Direction.Axis.Z ? insideDirection : 0;
                        nearest = new VillageDoorInfo(candidate.immutable(), candidate.offset(insideX, 0, insideZ), insideX, insideZ);
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    public record VillageDoorInfo(BlockPos doorPos, BlockPos insidePos, int insideDirectionX, int insideDirectionZ) {
        public boolean isInside(BlockPos pos) {
            return (pos.getX() - this.doorPos.getX()) * this.insideDirectionX
                + (pos.getZ() - this.doorPos.getZ()) * this.insideDirectionZ >= 0;
        }
    }
}
