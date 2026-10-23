package thaumcraft.world.tree;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.PlantSoil;

public class BigMagicTreeGenerator extends BranchedTreeGenerator {
    public BigMagicTreeGenerator(boolean notify) {
        super(notify);
        heightAttenuation = 0.6618;
        branchSlope = 0.381;
        scaleWidth = 1.0;
        leafDensity = 1.0;
        trunkSize = 1;
        heightLimitLimit = 12;
        leafDistanceLimit = 3;
    }

    @Override
    protected BlockState leafState() {
        return Blocks.OAK_LEAVES.defaultBlockState();
    }

    @Override
    protected BlockState logState() {
        return Blocks.OAK_LOG.defaultBlockState();
    }

    public void setScale(double height, double width, double density) {
        heightLimitLimit = (int) (height * 12.0);
        if (height > 0.5) {
            leafDistanceLimit = 3;
        }
        scaleWidth = width;
        leafDensity = density * 0.8;
    }

    private boolean validTreeLocation() {
        int[] from = {basePos[0], basePos[1], basePos[2]};
        int[] to = {basePos[0], basePos[1] + heightLimit - 1, basePos[2]};
        BlockPos soilPos = new BlockPos(basePos[0], basePos[1] - 1, basePos[2]);
        if (level.isOutsideBuildHeight(soilPos) || !PlantSoil.PLAINS.canSustain(level.getBlockState(soilPos), level, soilPos)) {
            return false;
        }
        int obstruction = checkBlockLine(from, to);
        if (obstruction == -1) {
            return true;
        }
        if (obstruction < 6) {
            return false;
        }
        heightLimit = obstruction;
        return true;
    }

    public boolean generate(LevelAccessor level, Random random, BlockPos pos) {
        this.level = level;
        rand.setSeed(random.nextLong());
        basePos[0] = pos.getX();
        basePos[1] = pos.getY();
        basePos[2] = pos.getZ();
        if (heightLimit == 0) {
            heightLimit = 11 + rand.nextInt(heightLimitLimit);
        }
        if (!validTreeLocation()) {
            return false;
        }
        generateLeafNodeList();
        generateLeaves();
        generateTrunk();
        generateLeafNodeBases();
        scheduleLeafUpdates();
        return true;
    }
}
