package thaumcraft.world.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

abstract class BranchedTreeGenerator {
    static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

    protected final Random rand = new Random();
    protected final boolean notify;
    protected final List<BlockPos> placedLeaves = new ArrayList<>();
    protected LevelAccessor level;
    protected int[] basePos = {0, 0, 0};
    protected int heightLimit;
    protected int height;
    protected double heightAttenuation;
    protected double branchSlope;
    protected double scaleWidth;
    protected double leafDensity;
    protected int trunkSize;
    protected int heightLimitLimit;
    protected int leafDistanceLimit;
    protected int[][] leafNodes;

    protected BranchedTreeGenerator(boolean notify) {
        this.notify = notify;
    }

    protected abstract BlockState leafState();

    protected abstract BlockState logState();

    protected final void setBlock(int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, notify ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
    }

    protected final boolean isLeaf(BlockState state) {
        return state.is(leafState().getBlock());
    }

    protected final void scheduleLeafUpdates() {
        for (BlockPos pos : placedLeaves) {
            if (level.getBlockState(pos).is(leafState().getBlock())) {
                level.scheduleTick(pos, leafState().getBlock(), 1);
            }
        }
        placedLeaves.clear();
    }

    protected final void generateLeafNodeList() {
        height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }
        int density = (int) (1.382 + Math.pow(leafDensity * heightLimit / 13.0, 2.0));
        if (density < 1) {
            density = 1;
        }
        int[][] nodes = new int[density * heightLimit][4];
        int layerY = basePos[1] + heightLimit - leafDistanceLimit;
        int count = 1;
        int trunkTop = basePos[1] + height;
        int layer = layerY - basePos[1];
        nodes[0][0] = basePos[0];
        nodes[0][1] = layerY;
        nodes[0][2] = basePos[2];
        nodes[0][3] = trunkTop;
        layerY--;
        while (layer >= 0) {
            int attempt = 0;
            float size = layerSize(layer);
            if (size < 0.0F) {
                layerY--;
                layer--;
            } else {
                double offset = 0.5;
                while (attempt < density) {
                    double distance = scaleWidth * size * (rand.nextFloat() + 0.328);
                    double angle = rand.nextFloat() * 2.0 * Math.PI;
                    int nodeX = Mth.floor(distance * Math.sin(angle) + basePos[0] + offset);
                    int nodeZ = Mth.floor(distance * Math.cos(angle) + basePos[2] + offset);
                    int[] nodeStart = {nodeX, layerY, nodeZ};
                    int[] nodeEnd = {nodeX, layerY + leafDistanceLimit, nodeZ};
                    if (checkBlockLine(nodeStart, nodeEnd) == -1) {
                        int[] branchStart = {basePos[0], basePos[1], basePos[2]};
                        double horizontal = Math.sqrt(Math.pow(Math.abs(basePos[0] - nodeStart[0]), 2.0) + Math.pow(Math.abs(basePos[2] - nodeStart[2]), 2.0));
                        double rise = horizontal * branchSlope;
                        if (nodeStart[1] - rise > trunkTop) {
                            branchStart[1] = trunkTop;
                        } else {
                            branchStart[1] = (int) (nodeStart[1] - rise);
                        }
                        if (checkBlockLine(branchStart, nodeStart) == -1) {
                            nodes[count][0] = nodeX;
                            nodes[count][1] = layerY;
                            nodes[count][2] = nodeZ;
                            nodes[count][3] = branchStart[1];
                            count++;
                        }
                    }
                    attempt++;
                }
                layerY--;
                layer--;
            }
        }
        leafNodes = new int[count][4];
        System.arraycopy(nodes, 0, leafNodes, 0, count);
    }

    protected final void genTreeLayer(int centerX, int centerY, int centerZ, float radius, int axis) {
        int extent = (int) (radius + 0.618);
        int first = OTHER_COORD_PAIRS[axis];
        int second = OTHER_COORD_PAIRS[axis + 3];
        int[] center = {centerX, centerY, centerZ};
        int[] position = {0, 0, 0};
        position[axis] = center[axis];
        for (int offsetA = -extent; offsetA <= extent; offsetA++) {
            position[first] = center[first] + offsetA;
            for (int offsetB = -extent; offsetB <= extent; offsetB++) {
                double distance = Math.pow(Math.abs(offsetA) + 0.5, 2.0) + Math.pow(Math.abs(offsetB) + 0.5, 2.0);
                if (distance > radius * radius) {
                    continue;
                }
                position[second] = center[second] + offsetB;
                BlockPos pos = new BlockPos(position[0], position[1], position[2]);
                if (level.isOutsideBuildHeight(pos)) {
                    continue;
                }
                BlockState existing = level.getBlockState(pos);
                if (existing.isAir() || isLeaf(existing)) {
                    setBlock(position[0], position[1], position[2], leafState());
                    placedLeaves.add(pos);
                }
            }
        }
    }

    protected final float layerSize(int layer) {
        if (layer < heightLimit * 0.3) {
            return -1.618F;
        }
        float radius = heightLimit / 2.0F;
        float delta = heightLimit / 2.0F - layer;
        float size;
        if (delta == 0.0F) {
            size = radius;
        } else if (Math.abs(delta) >= radius) {
            size = 0.0F;
        } else {
            size = (float) Math.sqrt(Math.pow(Math.abs(radius), 2.0) - Math.pow(Math.abs(delta), 2.0));
        }
        return size * 0.5F;
    }

    protected final float leafSize(int layer) {
        return layer >= 0 && layer < leafDistanceLimit ? (layer != 0 && layer != leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
    }

    protected final void generateLeafNode(int x, int y, int z) {
        for (int layerY = y; layerY < y + leafDistanceLimit; layerY++) {
            genTreeLayer(x, layerY, z, leafSize(layerY - y), 1);
        }
    }

    protected final void placeBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        int major = 0;
        for (int axis = 0; axis < 3; axis++) {
            delta[axis] = to[axis] - from[axis];
            if (Math.abs(delta[axis]) > Math.abs(delta[major])) {
                major = axis;
            }
        }
        if (delta[major] == 0) {
            return;
        }
        int first = OTHER_COORD_PAIRS[major];
        int second = OTHER_COORD_PAIRS[major + 3];
        int step = delta[major] > 0 ? 1 : -1;
        double slopeFirst = (double) delta[first] / delta[major];
        double slopeSecond = (double) delta[second] / delta[major];
        int[] position = {0, 0, 0};
        int index = 0;
        for (int end = delta[major] + step; index != end; index += step) {
            position[major] = Mth.floor(from[major] + index + 0.5);
            position[first] = Mth.floor(from[first] + index * slopeFirst + 0.5);
            position[second] = Mth.floor(from[second] + index * slopeSecond + 0.5);
            Direction.Axis axis = Direction.Axis.Y;
            int distanceX = Math.abs(position[0] - from[0]);
            int distanceZ = Math.abs(position[2] - from[2]);
            int longest = Math.max(distanceX, distanceZ);
            if (longest > 0) {
                if (distanceX == longest) {
                    axis = Direction.Axis.X;
                } else if (distanceZ == longest) {
                    axis = Direction.Axis.Z;
                }
            }
            setBlock(position[0], position[1], position[2], logState().trySetValue(RotatedPillarBlock.AXIS, axis));
        }
    }

    protected final void generateLeaves() {
        for (int[] node : leafNodes) {
            generateLeafNode(node[0], node[1], node[2]);
        }
    }

    protected final boolean leafNodeNeedsBase(int offset) {
        return offset >= heightLimit * 0.2;
    }

    protected final void generateTrunk() {
        int x = basePos[0];
        int y = basePos[1];
        int top = basePos[1] + height;
        int z = basePos[2];
        int[] from = {x, y, z};
        int[] to = {x, top, z};
        placeBlockLine(from, to);
        if (trunkSize == 2) {
            from[0]++;
            to[0]++;
            placeBlockLine(from, to);
            from[2]++;
            to[2]++;
            placeBlockLine(from, to);
            from[0]--;
            to[0]--;
            placeBlockLine(from, to);
        }
    }

    protected final void generateLeafNodeBases() {
        int[] start = {basePos[0], basePos[1], basePos[2]};
        for (int[] node : leafNodes) {
            int[] end = {node[0], node[1], node[2]};
            start[1] = node[3];
            if (leafNodeNeedsBase(start[1] - basePos[1])) {
                placeBlockLine(start, end);
            }
        }
    }

    protected final int checkBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        int major = 0;
        for (int axis = 0; axis < 3; axis++) {
            delta[axis] = to[axis] - from[axis];
            if (Math.abs(delta[axis]) > Math.abs(delta[major])) {
                major = axis;
            }
        }
        if (delta[major] == 0) {
            return -1;
        }
        int first = OTHER_COORD_PAIRS[major];
        int second = OTHER_COORD_PAIRS[major + 3];
        int step = delta[major] > 0 ? 1 : -1;
        double slopeFirst = (double) delta[first] / delta[major];
        double slopeSecond = (double) delta[second] / delta[major];
        int[] position = {0, 0, 0};
        int index = 0;
        int end = delta[major] + step;
        for (; index != end; index += step) {
            position[major] = from[major] + index;
            position[first] = Mth.floor(from[first] + index * slopeFirst);
            position[second] = Mth.floor(from[second] + index * slopeSecond);
            BlockPos pos = new BlockPos(position[0], position[1], position[2]);
            if (level.isOutsideBuildHeight(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !isLeaf(state)) {
                break;
            }
        }
        return index == end ? -1 : Math.abs(index);
    }
}
