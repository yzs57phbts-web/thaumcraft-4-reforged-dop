package thaumcraft.entity.golem.goal.work;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.platform.GolemActions;

public final class LogHarvestGoal extends Goal {
    private static final int GOLEM_DELAY = 5;
    private final GolemBase golem;
    private final int range;
    private BlockPos target;
    private BlockState targetState;
    private int delay = -1;
    private int maxDelay = 1;
    private int step = 1;
    private int count;
    private Path approach;
    private boolean finished;
    private final Map<BlockPos, Long> rejected = new HashMap<>();

    public LogHarvestGoal(GolemBase golem) {
        this.golem = golem;
        this.range = Mth.ceil(golem.getRange() / 3.0F);
        setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!golem.isWorking() || delay >= 0 || golem.tickCount % GOLEM_DELAY != 0 || !golem.getNavigation().isDone()) return false;
        rejected.values().removeIf(until -> until <= golem.level().getGameTime());
        target = findLog();
        if (target == null) return false;
        targetState = golem.level().getBlockState(target);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        BlockState state = target == null ? null : golem.level().getBlockState(target);
        return golem.isWorking()
            && target != null
            && !finished
            && state == targetState
            && isLog(state)
            && count-- > 0;
    }

    @Override
    public void start() {
        count = 200;
        finished = false;
        if (inReach(target)) {
            golem.getNavigation().stop();
        } else if (approach == null || !golem.getNavigation().moveTo(approach, 1.0)) {
            rejectTarget();
        }
    }

    @Override
    public void tick() {
        if (target == null) return;
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 30, 30);
        if (!inReach(target)) {
            if (golem.getNavigation().isDone()) rejectTarget();
            return;
        }
        golem.getNavigation().stop();
        if (delay < 0) {
            delay = (int) Math.max(5.0F, (20.0F - golem.getGolemStrength() * 3.0F) * targetState.getDestroySpeed(golem.level(), target));
            maxDelay = delay;
            step = delay / Math.max(1, Math.round(delay / 6.0F));
        }
        if (delay <= 0) return;
        delay--;
        if (delay > 0 && delay % step == 0 && golem.getNavigation().isDone()) {
            golem.startActionTimer();
            var sound = targetState.getSoundType(golem.level(), target, golem).getBreakSound();
            float volume = targetState.getSoundType(golem.level(), target, golem).getVolume();
            float pitch = targetState.getSoundType(golem.level(), target, golem).getPitch();
            golem.level().playSound(null, target, sound, SoundSource.BLOCKS, (volume + 0.7F) / 8.0F, pitch * 0.5F);
            if (golem.level() instanceof ServerLevel server) server.destroyBlockProgress(golem.getId(), target, (int) (9.0F * (1.0F - (float) delay / maxDelay)));
        }
        if (delay == 0) {
            if (!harvest()) {
                rejectTarget();
                return;
            }
            if (isLog(golem.level().getBlockState(target))) {
                delay = -1;
                targetState = golem.level().getBlockState(target);
                approach = null;
                start();
            } else {
                checkAdjacent();
            }
        }
    }

    @Override
    public void stop() {
        if (target != null && golem.level() instanceof ServerLevel server) server.destroyBlockProgress(golem.getId(), target, -1);
        delay = -1;
        target = null;
        targetState = null;
        approach = null;
        golem.getNavigation().stop();
    }

    private BlockPos findLog() {
        BlockPos home = golem.getRestrictCenter();
        for (int attempt = 0; attempt < range * 4; attempt++) {
            int x = home.getX() + golem.getRandom().nextInt(1 + range * 2) - range;
            int y = home.getY() + golem.getRandom().nextInt(1 + range) - range / 2;
            int z = home.getZ() + golem.getRandom().nextInt(1 + range * 2) - range;
            BlockPos candidate = new BlockPos(x, y, z);
            if (!isLog(golem.level().getBlockState(candidate))) continue;
            BlockPos lowest = candidate;
            double distance = golem.distanceToSqr(x + 0.5, y + 0.5, z + 0.5);
            for (int down = 1; isLog(golem.level().getBlockState(candidate.below(down)))
                && golem.distanceToSqr(x + 0.5, y - down + 0.5, z + 0.5) < distance; down++) {
                lowest = candidate.below(down);
                distance = golem.distanceToSqr(x + 0.5, y - down + 0.5, z + 0.5);
            }
            if (rejected.containsKey(lowest)) continue;
            approach = inReach(lowest) ? null : findApproach(lowest);
            if (approach != null || inReach(lowest)) return lowest;
            rejected.put(lowest.immutable(), golem.level().getGameTime() + 200);
        }
        return null;
    }

    private void checkAdjacent() {
        BlockPos home = golem.getRestrictCenter();
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) for (int y = -1; y <= 1; y++) {
            BlockPos pos = target.offset(x, y, z);
            if (Math.abs(home.getX() - pos.getX()) <= range
                && Math.abs(home.getY() - pos.getY()) <= range
                && Math.abs(home.getZ() - pos.getZ()) <= range
                && !rejected.containsKey(pos)
                && isLog(golem.level().getBlockState(pos))) {
                Path path = inReach(pos) ? null : findApproach(pos);
                if (path == null && !inReach(pos)) continue;
                if (golem.level() instanceof ServerLevel server) server.destroyBlockProgress(golem.getId(), target, -1);
                approach = path;
                target = pos;
                targetState = golem.level().getBlockState(pos);
                delay = -1;
                start();
                return;
            }
        }
        finished = true;
    }

    private boolean harvest() {
        count = 200;
        if (golem.level() instanceof ServerLevel server) {
            BlockPos furthest = furthestLog(target, targetState.getBlock());
            BlockState before = server.getBlockState(furthest);
            boolean worked = GolemActions.harvest(golem, furthest) && server.getBlockState(furthest) != before;
            if (worked) {
                server.levelEvent(2001, target, Block.getId(targetState));
                golem.startActionTimer();
            }
            return worked;
        }
        return false;
    }

    private boolean inReach(BlockPos pos) {
        return golem.distanceToSqr(Vec3.atCenterOf(pos)) <= 4.0;
    }

    private Path findApproach(BlockPos log) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(log.offset(-1, -1, -1), log.offset(1, 1, 1))) {
            Vec3 feet = Vec3.atBottomCenterOf(pos);
            if (feet.distanceToSqr(Vec3.atCenterOf(log)) > 4.0
                || !golem.level().hasChunkAt(pos)
                || !golem.level().getBlockState(pos.below()).isFaceSturdy(golem.level(), pos.below(), Direction.UP)
                || !golem.level().noCollision(golem, golem.getBoundingBox().move(feet.subtract(golem.position())))) continue;
            candidates.add(pos.immutable());
        }
        candidates.sort(Comparator.comparingDouble(pos -> golem.distanceToSqr(Vec3.atBottomCenterOf(pos))));
        for (BlockPos pos : candidates) {
            Path path = golem.getNavigation().createPath(pos, 0);
            if (path != null && path.canReach()) return path;
        }
        return null;
    }

    private void rejectTarget() {
        rejected.put(target.immutable(), golem.level().getGameTime() + 200);
        finished = true;
        golem.getNavigation().stop();
    }

    private BlockPos furthestLog(BlockPos origin, net.minecraft.world.level.block.Block block) {
        Queue<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(origin);
        visited.add(origin);
        BlockPos furthest = origin;
        double maxDistance = 0;
        while (!pending.isEmpty()) {
            BlockPos pos = pending.remove();
            if (!golem.level().getBlockState(pos).is(block) || !isLog(golem.level().getBlockState(pos))) continue;
            double dx = pos.getX() - origin.getX();
            double dy = pos.getY() - origin.getY();
            double dz = pos.getZ() - origin.getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance > maxDistance) {
                maxDistance = distance;
                furthest = pos;
            }
            for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
                if (x == 0 && y == 0 && z == 0) continue;
                BlockPos next = pos.offset(x, y, z);
                if (Math.abs(next.getX() - origin.getX()) <= 24
                    && Math.abs(next.getY() - origin.getY()) <= 48
                    && Math.abs(next.getZ() - origin.getZ()) <= 24
                    && visited.add(next)
                    && golem.level().getBlockState(next).is(block)
                    && isLog(golem.level().getBlockState(next))
                    && golem.level().getBlockState(next).getDestroySpeed(golem.level(), next) >= 0) pending.add(next);
            }
        }
        return furthest;
    }

    private boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }
}
