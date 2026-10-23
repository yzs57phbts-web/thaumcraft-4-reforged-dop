package thaumcraft.essentia;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.alchemy.BellowsBlockEntity;

public final class TubeArms {
    public enum Arm implements StringRepresentable {
        NONE("none"),
        NORMAL("normal"),
        EXTENDED("extended");

        private final String name;

        Arm(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record TraceBox(int index, AABB box) {
    }

    public static final int CENTER = 6;

    public static final EnumProperty<Arm> DOWN = EnumProperty.create("down", Arm.class);
    public static final EnumProperty<Arm> UP = EnumProperty.create("up", Arm.class);
    public static final EnumProperty<Arm> NORTH = EnumProperty.create("north", Arm.class);
    public static final EnumProperty<Arm> SOUTH = EnumProperty.create("south", Arm.class);
    public static final EnumProperty<Arm> WEST = EnumProperty.create("west", Arm.class);
    public static final EnumProperty<Arm> EAST = EnumProperty.create("east", Arm.class);

    private static final Map<Direction, EnumProperty<Arm>> PROPERTIES = new EnumMap<>(Direction.class);
    private static final Map<Integer, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    static {
        PROPERTIES.put(Direction.DOWN, DOWN);
        PROPERTIES.put(Direction.UP, UP);
        PROPERTIES.put(Direction.NORTH, NORTH);
        PROPERTIES.put(Direction.SOUTH, SOUTH);
        PROPERTIES.put(Direction.WEST, WEST);
        PROPERTIES.put(Direction.EAST, EAST);
    }

    private TubeArms() {
    }

    public static EnumProperty<Arm> property(Direction direction) {
        return PROPERTIES.get(direction);
    }

    public static BlockState noArms(BlockState state) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            result = result.setValue(property(direction), Arm.NONE);
        }
        return result;
    }

    public static Arm compute(BlockGetter level, BlockPos pos, BlockState state, Direction side) {
        if (level.getBlockEntity(pos) instanceof EssentiaTransport self && !self.isConnectable(side)) {
            return Arm.NONE;
        }
        if (!(level.getBlockEntity(pos.relative(side)) instanceof EssentiaTransport other) || !other.isConnectable(side.getOpposite())) {
            return Arm.NONE;
        }
        if (!(state.getBlock() instanceof EssentiaBufferBlock) && other instanceof BellowsBlockEntity) {
            return Arm.NONE;
        }
        return other.renderExtendedTube() ? Arm.EXTENDED : Arm.NORMAL;
    }

    public static BlockState withArms(BlockGetter level, BlockPos pos, BlockState state) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            result = result.setValue(property(direction), compute(level, pos, state, direction));
        }
        return result;
    }

    public static void refresh(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(DOWN)) {
            return;
        }
        BlockState updated = withArms(level, pos, state);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
    }

    public static void refreshAround(Level level, BlockPos pos) {
        refresh(level, pos);
        for (Direction direction : Direction.values()) {
            refresh(level, pos.relative(direction));
        }
    }

    public static VoxelShape shape(BlockState state, double bodyMin, double bodyMax) {
        int armMask = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(property(direction)) != Arm.NONE) {
                armMask |= 1 << direction.ordinal();
            }
        }
        int mask = armMask;
        int key = mask | (bodyMin == 0.25 ? 64 : 0);
        return SHAPES.computeIfAbsent(key, ignored -> buildShape(mask, bodyMin, bodyMax));
    }

    private static VoxelShape buildShape(int mask, double bodyMin, double bodyMax) {
        VoxelShape result = Shapes.create(bodyMin, bodyMin, bodyMin, bodyMax, bodyMax, bodyMax);
        double armMin = 0.375;
        double armMax = 0.625;
        for (Direction direction : Direction.values()) {
            if ((mask & (1 << direction.ordinal())) == 0) {
                continue;
            }
            double minX = armMin;
            double maxX = armMax;
            double minY = armMin;
            double maxY = armMax;
            double minZ = armMin;
            double maxZ = armMax;
            switch (direction) {
                case DOWN -> minY = 0.0;
                case UP -> maxY = 1.0;
                case NORTH -> minZ = 0.0;
                case SOUTH -> maxZ = 1.0;
                case WEST -> minX = 0.0;
                case EAST -> maxX = 1.0;
            }
            result = Shapes.or(result, Shapes.create(minX, minY, minZ, maxX, maxY, maxZ));
        }
        return result;
    }

    public static List<TraceBox> traceBoxes(BlockGetter level, BlockPos pos, double centerMin, double centerMax) {
        List<TraceBox> boxes = new ArrayList<>();
        double min = 0.42;
        double max = 0.58;
        for (Direction direction : Direction.values()) {
            if (!(level.getBlockEntity(pos.relative(direction)) instanceof EssentiaTransport)) {
                continue;
            }
            double minX = min;
            double maxX = max;
            double minY = min;
            double maxY = max;
            double minZ = min;
            double maxZ = max;
            switch (direction) {
                case DOWN -> {
                    minY = 0.0;
                    maxY = 0.5;
                }
                case UP -> {
                    minY = 0.5;
                    maxY = 1.0;
                }
                case NORTH -> {
                    minZ = 0.0;
                    maxZ = 0.5;
                }
                case SOUTH -> {
                    minZ = 0.5;
                    maxZ = 1.0;
                }
                case WEST -> {
                    minX = 0.0;
                    maxX = 0.5;
                }
                case EAST -> {
                    minX = 0.5;
                    maxX = 1.0;
                }
            }
            boxes.add(new TraceBox(direction.ordinal(), new AABB(minX, minY, minZ, maxX, maxY, maxZ)));
        }
        boxes.add(new TraceBox(CENTER, new AABB(centerMin, centerMin, centerMin, centerMax, centerMax, centerMax)));
        return boxes;
    }

    public static VoxelShape traceShape(List<TraceBox> boxes) {
        VoxelShape result = Shapes.empty();
        for (TraceBox traceBox : boxes) {
            result = Shapes.or(result, Shapes.create(traceBox.box()));
        }
        return result;
    }

    public static int trace(Player player, BlockPos pos, List<TraceBox> boxes) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(player.blockInteractionRange() + 1.0));
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (TraceBox traceBox : boxes) {
            Optional<Vec3> hit = traceBox.box().move(pos).clip(start, end);
            if (hit.isPresent()) {
                double distance = hit.get().distanceToSqr(start);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = traceBox.index();
                }
            }
        }
        return best;
    }

    public static @Nullable Direction direction(int index) {
        return index >= 0 && index < 6 ? Direction.from3DDataValue(index) : null;
    }
}
