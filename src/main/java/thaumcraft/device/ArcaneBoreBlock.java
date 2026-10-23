package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandInteractable;
import thaumcraft.wand.Wands;

public class ArcaneBoreBlock extends Block implements EntityBlock, WandInteractable {
    public static final EnumProperty<Direction> BASE = EnumProperty.create("base", Direction.class, Direction.UP, Direction.DOWN);

    public ArcaneBoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BASE, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE);
    }

    public static boolean isSupport(BlockState state) {
        return state.is(ModBlocks.ARCANE_BORE_BASE.get()) || state.is(ModBlocks.GREATWOOD_PLANKS.get()) || state.is(ModBlocks.SILVERWOOD_PLANKS.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupport(level.getBlockState(pos.relative(state.getValue(BASE).getOpposite())));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis() != Direction.Axis.Y) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(BASE, face);
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    public static Direction determineOrientation(BlockPos pos, LivingEntity placer) {
        if (Math.abs((float) placer.getX() - pos.getX()) < 2.0F && Math.abs((float) placer.getZ() - pos.getZ()) < 2.0F) {
            double head = placer.getY() + 1.82;
            if (head - pos.getY() > 2.0) {
                return Direction.UP;
            }
            if (pos.getY() - head > 0.0) {
                return Direction.DOWN;
            }
        }
        return switch (Mth.floor(placer.getYRot() * 4.0F / 360.0F + 0.5) & 3) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            default -> Direction.WEST;
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            bore.setOrientation(determineOrientation(pos, placer), true);
            if (!level.isClientSide()) {
                bore.sync();
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            Direction direction = bore.getOrientation();
            return Shapes.box(
                direction.getStepX() < 0 ? -1.0 : 0.0,
                direction.getStepY() < 0 ? -1.0 : 0.0,
                direction.getStepZ() < 0 ? -1.0 : 0.0,
                direction.getStepX() > 0 ? 2.0 : 1.0,
                direction.getStepY() > 0 ? 2.0 : 1.0,
                direction.getStepZ() > 0 ? 2.0 : 1.0
            );
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (!level.isClientSide() && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return Wands.isWand(stack) ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                Menus.open(serverPlayer, bore, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            bore.setOrientation(side, false);
            if (!level.isClientSide()) {
                bore.sync();
            }
            level.playSound(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ARCANE_BORE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ArcaneBoreBlockEntity.tick(tickLevel, pos, tickState, (ArcaneBoreBlockEntity) entity);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            thaumcraft.blockentity.TcBlockEntity.beforeRemove(state, level, pos, newState);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
