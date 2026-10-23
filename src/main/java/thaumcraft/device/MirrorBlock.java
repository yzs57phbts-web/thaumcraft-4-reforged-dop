package thaumcraft.device;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class MirrorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
        Direction.DOWN, Block.box(0, 15, 0, 16, 16, 16),
        Direction.UP, Block.box(0, 0, 0, 16, 1, 16),
        Direction.NORTH, Block.box(0, 0, 15, 16, 16, 16),
        Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 1),
        Direction.WEST, Block.box(15, 0, 0, 16, 16, 16),
        Direction.EAST, Block.box(0, 0, 0, 1, 16, 16)
    );

    private static @Nullable SoundType sounds;

    private final boolean essentia;

    public MirrorBlock(boolean essentia, Properties properties) {
        super(properties);
        this.essentia = essentia;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public boolean isEssentia() {
        return essentia;
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        if (sounds == null) {
            SoundEvent jar = ModSounds.JAR.get();
            sounds = new SoundType(0.5F, 2.0F, jar, jar, jar, jar, jar);
        }
        return sounds;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return essentia ? new EssentiaMirrorBlockEntity(pos, state) : new MirrorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (!essentia && type == ModBlockEntities.MAGIC_MIRROR.get()) {
            return (tickLevel, pos, tickState, entity) -> MirrorBlockEntity.serverTick(tickLevel, pos, tickState, (MirrorBlockEntity) entity);
        }
        if (essentia && type == ModBlockEntities.ESSENTIA_MIRROR.get()) {
            return (tickLevel, pos, tickState, entity) -> EssentiaMirrorBlockEntity.serverTick(tickLevel, pos, tickState, (EssentiaMirrorBlockEntity) entity);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        super.setPlacedBy(level, pos, state, by, stack);
        GlobalPos target = MirrorKeys.LINK.get(stack);
        if (!level.isClientSide() && target != null && level.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity mirror) {
            mirror.setLinkTarget(target);
            mirror.restoreLink(true);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack drop = new ItemStack(this);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LinkedMirrorBlockEntity mirror && mirror.isLinked() && mirror.getLink() != null) {
            MirrorKeys.LINK.set(drop, mirror.getLink());
            mirror.invalidateLink();
        }
        return List.of(drop);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!essentia && !level.isClientSide() && entity instanceof ItemEntity item && item.isAlive() && !item.isOnPortalCooldown()
            && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            mirror.transport(item);
        }
        super.entityInside(state, level, pos, entity);
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
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
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
        if (!state.is(newState.getBlock())) {
            thaumcraft.blockentity.TcBlockEntity.beforeRemove(state, level, pos, newState);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
