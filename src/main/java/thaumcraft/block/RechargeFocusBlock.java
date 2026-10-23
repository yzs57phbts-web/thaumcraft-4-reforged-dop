package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RechargeFocusBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 7, 15);

    public RechargeFocusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).getBlock() instanceof WandPedestalBlock) {
            return ItemInteractions.skipping(WandPedestalBlock.interact(level, below, player, hand, stack));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).getBlock() instanceof WandPedestalBlock) {
            return WandPedestalBlock.interact(level, below, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        return InteractionResult.PASS;
    }
}
