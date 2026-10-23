package thaumcraft.block;

import thaumcraft.blockentity.TcBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.NodeJarBlockEntity;
import thaumcraft.item.NodeJarData;
import thaumcraft.item.NodeJarItem;
import thaumcraft.node.Node;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.WandInteractable;

public class NodeJarBlock extends Block implements EntityBlock, WandInteractable {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 12, 13);

    public NodeJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeJarBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        super.setPlacedBy(level, pos, state, by, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
            AspectList aspects = AspectKeys.ASPECTS.get(stack);
            if (aspects != null) {
                NodeJarData data = NodeJarItem.getData(stack);
                jar.setAspects(aspects);
                jar.setNodeType(data.type());
                jar.setNodeModifier(data.modifier());
                jar.setId(data.id());
                jar.sync();
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (player.isCreative() && level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
            jar.setDrop(false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
            jar.setDrop(false);
            AspectList aspects = jar.getAspects().copy();
            NodeType type = jar.getNodeType();
            NodeModifier modifier = jar.getNodeModifier();
            String id = jar.getId();
            level.setBlock(pos, ModBlocks.AURA_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof Node node) {
                node.setAspects(aspects);
                node.setNodeModifier(modifier);
                node.setNodeType(type);
                node.setId(id);
                if (node instanceof NodeBlockEntity released) {
                    released.sync();
                }
            }
        }
        level.levelEvent(2001, pos, Block.getId(defaultBlockState()));
        level.playSound(player, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
