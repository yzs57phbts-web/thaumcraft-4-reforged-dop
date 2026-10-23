package thaumcraft.infusion;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlocks;

public class InfusionPillarBlock extends Block implements EntityBlock {
    private static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    private final boolean top;

    public InfusionPillarBlock(boolean top, Properties properties) {
        super(properties);
        this.top = top;
    }

    public boolean isTop() {
        return top;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return top ? null : new InfusionPillarBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return top ? Shapes.block() : BOTTOM_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Block drop = top ? ModBlocks.ARCANE_STONE.get() : ModBlocks.ARCANE_STONE_BRICKS.get();
        return List.of(new ItemStack(drop));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockPos partner = top ? pos.below() : pos.above();
            Block expected = top ? ModBlocks.INFUSION_PILLAR.get() : ModBlocks.INFUSION_PILLAR_TOP.get();
            if (!level.getBlockState(partner).is(expected)) {
                Block.dropResources(state, level, pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos, 11960575, 2);
                level.levelEvent(2001, pos, Block.getId(Blocks.STONE_BRICKS.defaultBlockState()));
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }
}
