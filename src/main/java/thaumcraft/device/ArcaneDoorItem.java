package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import thaumcraft.registry.ModBlocks;

public class ArcaneDoorItem extends Item {
    private static final Direction[] ORIENTATIONS = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    private static final int[][] OFFSETS = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};

    public ArcaneDoorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().above();
        if (!player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || !player.mayUseItemAt(pos.above(), context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!canPlace(level, pos)) {
            return InteractionResult.FAIL;
        }
        int orientation = Mth.floor((player.getYRot() + 180.0F) * 4.0F / 360.0F - 0.5) & 3;
        if (!level.isClientSide()) {
            place(level, pos, orientation, player);
        }
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static boolean canPlace(Level level, BlockPos pos) {
        if (pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
            && level.getBlockState(pos).canBeReplaced()
            && level.getBlockState(pos.above()).canBeReplaced();
    }

    public static void place(Level level, BlockPos pos, int orientation, Player player) {
        ArcaneDoorBlock block = ModBlocks.ARCANE_DOOR.get();
        int stepX = OFFSETS[orientation][0];
        int stepZ = OFFSETS[orientation][1];
        BlockPos above = pos.above();
        BlockPos behind = pos.offset(-stepX, 0, -stepZ);
        BlockPos ahead = pos.offset(stepX, 0, stepZ);
        int solidBehind = (solid(level, behind) ? 1 : 0) + (solid(level, behind.above()) ? 1 : 0);
        int solidAhead = (solid(level, ahead) ? 1 : 0) + (solid(level, ahead.above()) ? 1 : 0);
        boolean doorBehind = level.getBlockState(behind).is(block) || level.getBlockState(behind.above()).is(block);
        boolean doorAhead = level.getBlockState(ahead).is(block) || level.getBlockState(ahead.above()).is(block);
        boolean flip = doorBehind && !doorAhead || solidAhead > solidBehind;
        BlockState lower = block.defaultBlockState()
            .setValue(ArcaneDoorBlock.FACING, ORIENTATIONS[orientation])
            .setValue(ArcaneDoorBlock.HINGE, flip ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
            .setValue(ArcaneDoorBlock.OPEN, false)
            .setValue(ArcaneDoorBlock.POWERED, false)
            .setValue(ArcaneDoorBlock.HALF, DoubleBlockHalf.LOWER);
        level.setBlock(pos, lower, 2);
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        if (owned != null) {
            owned.setOwner(player.getUUID());
        }
        level.setBlock(above, lower.setValue(ArcaneDoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        OwnedBlockEntity ownedAbove = OwnedBlockEntity.at(level, above);
        if (ownedAbove != null) {
            ownedAbove.setOwner(player.getUUID());
        }
        level.updateNeighborsAt(pos, block);
        level.updateNeighborsAt(above, block);
    }

    private static boolean solid(Level level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }
}
