package thaumcraft.vis;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.network.VisDrainFxPayload;
import thaumcraft.platform.ModNetwork;

public final class VisNet {
    private record Candidate(VisNode node, float distance) {
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, WeakReference<VisNode>>> SOURCES = new HashMap<>();
    private static final List<GlobalPos> CACHE = new ArrayList<>();
    private static final Map<GlobalPos, List<WeakReference<VisNode>>> NEARBY_NODES = new HashMap<>();
    private static final Map<GlobalPos, Long> EFFECT_BUFFER = new HashMap<>();

    private VisNet() {
    }

    public static int drainVis(Level level, BlockPos pos, Aspect aspect, int amount) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int drainedAmount = 0;
        GlobalPos drainer = GlobalPos.of(level.dimension(), pos);
        if (!NEARBY_NODES.containsKey(drainer)) {
            calculateNearbyNodes(level, pos);
        }
        List<WeakReference<VisNode>> nodes = NEARBY_NODES.get(drainer);
        if (nodes != null && !nodes.isEmpty()) {
            int remaining = amount;
            for (WeakReference<VisNode> reference : new ArrayList<>(nodes)) {
                VisNode node = reference.get();
                if (node != null) {
                    int drained = node.consumeVis(aspect, remaining);
                    drainedAmount += drained;
                    remaining -= drained;
                    if (drained > 0) {
                        generateVisEffect(serverLevel, pos, node.getBlockPos(), Aspect.getPrimalAspects().indexOf(aspect));
                    }
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
        return drainedAmount;
    }

    public static void generateVisEffect(ServerLevel level, BlockPos drainer, BlockPos source, int primal) {
        GlobalPos key = GlobalPos.of(level.dimension(), drainer);
        long time = System.currentTimeMillis();
        Long expires = EFFECT_BUFFER.get(key);
        if (expires != null) {
            if (expires < time) {
                EFFECT_BUFFER.remove(key);
            }
        } else {
            EFFECT_BUFFER.put(key, time + 500L + level.getRandom().nextInt(100));
            ModNetwork.sendToNear(level, null, drainer.getX(), drainer.getY(), drainer.getZ(), 64.0, new VisDrainFxPayload(drainer, source, primal));
        }
    }

    public static void addSource(Level level, VisNode source) {
        SOURCES.computeIfAbsent(level.dimension(), dimension -> new HashMap<>()).put(source.getBlockPos(), new WeakReference<>(source));
        NEARBY_NODES.clear();
    }

    public static void removeSource(Level level, VisNode source) {
        Map<BlockPos, WeakReference<VisNode>> sources = SOURCES.get(level.dimension());
        if (sources != null) {
            sources.remove(source.getBlockPos());
        }
        NEARBY_NODES.clear();
    }

    public static void clearNearby() {
        NEARBY_NODES.clear();
    }

    public static void clearLevel(ResourceKey<Level> dimension) {
        SOURCES.remove(dimension);
        NEARBY_NODES.keySet().removeIf(key -> key.dimension().equals(dimension));
        EFFECT_BUFFER.keySet().removeIf(key -> key.dimension().equals(dimension));
    }

    public static void clearAll() {
        SOURCES.clear();
        CACHE.clear();
        NEARBY_NODES.clear();
        EFFECT_BUFFER.clear();
    }

    public static boolean isNodeValid(@Nullable WeakReference<VisNode> node) {
        return node != null && node.get() != null && !node.get().isRemoved();
    }

    public static @Nullable WeakReference<VisNode> addNode(Level level, VisNode node) {
        Map<BlockPos, WeakReference<VisNode>> sources = SOURCES.get(level.dimension());
        if (sources == null) {
            return null;
        }
        List<Candidate> nearby = new ArrayList<>();
        for (WeakReference<VisNode> root : new ArrayList<>(sources.values())) {
            if (isNodeValid(root)) {
                VisNode source = root.get();
                float distance = inRange(node.getBlockPos(), source.getBlockPos(), node.getRange());
                if (distance > 0.0F) {
                    nearby.add(new Candidate(source, distance - node.getRange() * 2));
                }
                findClosestNodes(node, source, nearby);
                CACHE.clear();
            }
        }
        float closestDistance = Float.MAX_VALUE;
        VisNode closest = null;
        for (Candidate candidate : nearby) {
            if (candidate.distance() < closestDistance
                && (node.getAttunement() == -1 || candidate.node().getAttunement() == -1 || node.getAttunement() == candidate.node().getAttunement())
                && canNodeBeSeen(node, candidate.node())) {
                closestDistance = candidate.distance();
                closest = candidate.node();
            }
        }
        if (closest != null) {
            closest.getChildren().add(new WeakReference<>(node));
            NEARBY_NODES.clear();
            return new WeakReference<>(closest);
        }
        return null;
    }

    private static void findClosestNodes(VisNode target, VisNode parent, List<Candidate> found) {
        GlobalPos location = parent.getLocation();
        if (CACHE.size() <= 512 && !CACHE.contains(location)) {
            CACHE.add(location);
            for (WeakReference<VisNode> reference : new ArrayList<>(parent.getChildren())) {
                VisNode child = reference.get();
                if (child != null && child != target && child != parent) {
                    float distance = inRange(child.getBlockPos(), target.getBlockPos(), target.getRange());
                    if (distance > 0.0F) {
                        found.add(new Candidate(child, distance));
                    }
                    findClosestNodes(target, child, found);
                }
            }
        }
    }

    private static float inRange(BlockPos first, BlockPos second, int range) {
        float distance = (float) first.distSqr(second);
        return distance > range * range ? -1.0F : distance;
    }

    private static void calculateNearbyNodes(Level level, BlockPos pos) {
        Map<BlockPos, WeakReference<VisNode>> sources = SOURCES.get(level.dimension());
        if (sources == null) {
            return;
        }
        List<WeakReference<VisNode>> nearby = new ArrayList<>();
        GlobalPos drainer = GlobalPos.of(level.dimension(), pos);
        for (WeakReference<VisNode> root : new ArrayList<>(sources.values())) {
            if (isNodeValid(root)) {
                VisNode source = root.get();
                VisNode closest = null;
                float range = Float.MAX_VALUE;
                float distance = inRange(pos, source.getBlockPos(), source.getRange());
                if (distance > 0.0F) {
                    range = distance;
                    closest = source;
                }
                for (WeakReference<VisNode> child : getAllChildren(source, new ArrayList<>())) {
                    VisNode node = child.get();
                    if (node != null && node != source) {
                        float childDistance = inRange(node.getBlockPos(), pos, node.getRange());
                        if (childDistance > 0.0F && childDistance < range) {
                            range = childDistance;
                            closest = node;
                        }
                    }
                }
                if (closest != null) {
                    nearby.add(new WeakReference<>(closest));
                }
            }
        }
        NEARBY_NODES.put(drainer, nearby);
    }

    private static List<WeakReference<VisNode>> getAllChildren(VisNode source, List<WeakReference<VisNode>> list) {
        for (WeakReference<VisNode> child : new ArrayList<>(source.getChildren())) {
            VisNode node = child.get();
            if (node != null && node.getLevel() != null && node.getLevel().hasChunkAt(node.getBlockPos())) {
                list.add(child);
                getAllChildren(node, list);
            }
        }
        return list;
    }

    public static boolean canNodeBeSeen(VisNode source, VisNode target) {
        Level level = source.getLevel();
        if (level == null) {
            return false;
        }
        BlockPos sourcePos = source.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        Vec3 from = Vec3.atCenterOf(sourcePos);
        Vec3 to = Vec3.atCenterOf(targetPos);
        BlockHitResult hit = BlockGetter.traverseBlocks(from, to, level, (traceLevel, pos) -> {
            if (pos.equals(sourcePos) || !traceLevel.hasChunkAt(pos)) {
                return null;
            }
            BlockState state = traceLevel.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(traceLevel, pos);
            return shape.clip(from, to, pos);
        }, traceLevel -> null);
        return hit == null || hit.getBlockPos().equals(targetPos);
    }
}
