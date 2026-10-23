package thaumcraft.device;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.wand.WandInteractable;
import thaumcraft.wand.WandItem;

public class ArcanePressurePlateBlock extends Block implements EntityBlock, WandInteractable {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty SETTING = IntegerProperty.create("setting", 0, 2);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    private static final VoxelShape SHAPE_POWERED = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);

    public ArcanePressurePlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(SETTING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SETTING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwnedBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? SHAPE_POWERED : SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        if (owned != null && by instanceof Player player) {
            owned.setOwner(player.getUUID());
        }
        super.setPlacedBy(level, pos, state, by, stack);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            check(level, pos, state);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            check(level, pos, state);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && !state.getValue(POWERED)) {
            check(level, pos, state);
        }
    }

    private void check(Level level, BlockPos pos, BlockState state) {
        boolean wasPowered = state.getValue(POWERED);
        boolean powered = isTriggered(level, pos, state.getValue(SETTING));
        if (powered && !wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, true), 2);
            updateNeighbours(level, pos);
            level.setBlocksDirty(pos, state, state.setValue(POWERED, true));
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.2F, 0.6F);
        }
        if (!powered && wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, false), 2);
            updateNeighbours(level, pos);
            level.setBlocksDirty(pos, state, state.setValue(POWERED, false));
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.2F, 0.5F);
        }
        if (powered) {
            level.scheduleTick(pos, this, 20);
        }
    }

    private boolean isTriggered(Level level, BlockPos pos, int setting) {
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        AABB box = new AABB(pos.getX() + 0.125, pos.getY(), pos.getZ() + 0.125, pos.getX() + 0.875, pos.getY() + 0.25, pos.getZ() + 0.875);
        List<? extends Entity> entities = setting == 2
            ? level.getEntitiesOfClass(Player.class, box, EntitySelector.NO_SPECTATORS)
            : level.getEntitiesOfClass(Entity.class, box, EntitySelector.NO_SPECTATORS);
        for (Entity entity : entities) {
            if (counts(entity, setting, owned)) {
                return true;
            }
        }
        return false;
    }

    public static boolean counts(Entity entity, int setting, @Nullable OwnedBlockEntity owned) {
        if (entity.isIgnoringBlockTriggers() || setting == 2 && !(entity instanceof Player)) {
            return false;
        }
        boolean allowed = entity instanceof Player player && owned != null && owned.isOwnerOrHasAccess(player.getUUID());
        if (setting == 1 && allowed) {
            return false;
        }
        return setting != 2 || allowed;
    }

    private void updateNeighbours(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
    }

    private void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston && state.getValue(POWERED)) {
            updateNeighbours(level, pos);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP && state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        if (stack.getItem() instanceof WandItem && !state.getValue(POWERED) && owned != null && owned.isOwner(player.getUUID())) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        UUID id = player.getUUID();
        if (owned != null && (owned.isOwner(id) || owned.hasAccess(id, OwnedBlockEntity.GOLD))) {
            int setting = (state.getValue(SETTING) + 1) % 3;
            level.setBlock(pos, state.setValue(SETTING, setting), 2);
            player.sendSystemMessage(Component.translatable(switch (setting) {
                case 0 -> "tc.device.plate_all";
                case 1 -> "tc.device.plate_not_you";
                default -> "tc.device.plate_only_you";
            }));
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.1F, 0.9F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(POWERED)) {
            return InteractionResult.PASS;
        }
        OwnedBlockEntity owned = OwnedBlockEntity.at(level, pos);
        if (WardedConfig.WARDED_STONE && (owned == null || !owned.isOwner(player.getUUID()))) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this)));
            level.levelEvent(2001, pos, Block.getId(state));
            level.removeBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            thaumcraft.blockentity.TcBlockEntity.beforeRemove(state, level, pos, newState);
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                affectNeighborsAfterRemoval(state, serverLevel, pos, movedByPiston);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
