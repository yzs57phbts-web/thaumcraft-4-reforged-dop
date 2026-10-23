package thaumcraft.node;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public class AuraNodeBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public AuraNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return NodeBlockEntity.ticker(type);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (!level.isClientSide() && by instanceof Player) {
            NodeGenerator.createRandomNodeAt(level, pos, level.getRandom(), false, false, false);
        }
        super.setPlacedBy(level, pos, state, by, stack);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        dropWispEssence(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static void dropWispEssence(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof Node node && node.getAspects().size() > 0) {
            for (Aspect aspect : node.getAspects().getAspects()) {
                for (int index = 0; index <= node.getAspects().getAmount(aspect) / 10; index++) {
                    if (node.getAspects().getAmount(aspect) >= 5) {
                        ItemStack essence = new ItemStack(ModItems.WISP_ESSENCE.get());
                        AspectKeys.ASPECTS.set(essence, new AspectList().add(aspect, 2));
                        Block.popResource(level, pos, essence);
                    }
                }
            }
        }
        level.playSound(null, pos, ModSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
