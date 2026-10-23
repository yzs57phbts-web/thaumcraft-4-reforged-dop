package thaumcraft.device;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class InfernalFurnaceBlock extends Block implements EntityBlock {
    public static final IntegerProperty TILE = IntegerProperty.create("tile", 0, InfernalTiles.COUNT - 1);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int EVENT_SPARKLE = 1;
    public static final int EVENT_LAVA = 3;
    private static final VoxelShape CORE = Block.box(0, 0, 0, 16, 4, 16);

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TILE, InfernalTiles.CORE).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILE, FACING);
    }

    public static int lightLevel(BlockState state) {
        int tile = state.getValue(TILE);
        return tile == InfernalTiles.CORE || tile == InfernalTiles.BARS ? 13 : 3;
    }

    public BlockState tileState(int tile, Direction facing) {
        return defaultBlockState().setValue(TILE, tile).setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TILE) == InfernalTiles.CORE ? CORE : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int tile = state.getValue(TILE);
        if (tile == InfernalTiles.CORE) {
            return CORE;
        }
        if (tile == InfernalTiles.BARS) {
            return switch (state.getValue(FACING).getOpposite()) {
                case WEST -> Block.box(0, 0, 0, 8, 16, 16);
                case EAST -> Block.box(8, 0, 0, 16, 16, 16);
                case NORTH -> Block.box(0, 0, 0, 16, 16, 8);
                default -> Block.box(0, 0, 8, 16, 16, 16);
            };
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(TILE) == InfernalTiles.BARS ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel) || state.getValue(TILE) != InfernalTiles.CORE) {
            return;
        }
        if (entity.getX() < pos.getX() + 0.3F) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(1.0E-4F, 0.0, 0.0));
        }
        if (entity.getX() > pos.getX() + 0.7F) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(-1.0E-4F, 0.0, 0.0));
        }
        if (entity.getZ() < pos.getZ() + 0.3F) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.0, 1.0E-4F));
        }
        if (entity.getZ() > pos.getZ() + 0.7F) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.0, -1.0E-4F));
        }
        if (entity instanceof ItemEntity item) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0.025F, entity.getDeltaMovement().z);
            if (entity.onGround() && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace) {
                ItemStack stack = item.getItem().copy();
                if (furnace.addItemsToInventory(stack)) {
                    item.discard();
                }
            }
        } else if (entity instanceof LivingEntity && !entity.fireImmune()) {
            DamageSource lava = level.damageSources().lava();
            entity.hurt(lava, 3.0F);
            entity.igniteForSeconds(10.0F);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        int tile = state.getValue(TILE);
        if (tile == InfernalTiles.CORE) {
            return List.of();
        }
        return List.of(new ItemStack(InfernalTiles.revertBlock(tile).asItem()));
    }

    private void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (state.getValue(TILE) == InfernalTiles.CORE) {
            revertAround(level, pos);
        } else {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos candidate = pos.offset(x, y, z);
                        BlockState candidateState = level.getBlockState(candidate);
                        if (candidateState.is(this) && candidateState.getValue(TILE) == InfernalTiles.CORE) {
                            level.setBlock(candidate, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void revertAround(ServerLevel level, BlockPos core) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos target = core.offset(x, y, z);
                    BlockState targetState = level.getBlockState(target);
                    if (targetState.is(this)) {
                        level.setBlock(target, InfernalTiles.revertBlock(targetState.getValue(TILE)).defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(TILE) == InfernalTiles.CORE) {
            BlockState above = level.getBlockState(pos.above());
            if (above.isAir() && !above.canOcclude()) {
                for (int index = 0; index < 3; index++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 1.0F + random.nextFloat() * 0.5F, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == EVENT_SPARKLE) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos, 16736256, 5);
            }
            return true;
        }
        if (id == EVENT_LAVA && state.getValue(TILE) == InfernalTiles.CORE) {
            if (level.isClientSide()) {
                RandomSource random = level.getRandom();
                Direction facing = state.getValue(FACING);
                for (int index = 0; index < 5; index++) {
                    MachineFx.get().furnaceLava(level, pos, facing);
                    level.playLocalSound(pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
                }
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int tile = state.getValue(TILE);
        if (tile == InfernalTiles.CORE) {
            return new InfernalFurnaceBlockEntity(pos, state);
        }
        return InfernalTiles.isNozzle(tile) ? new InfernalFurnaceNozzleBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (type == ModBlockEntities.INFERNAL_FURNACE.get()) {
            return (tickLevel, pos, tickState, entity) -> InfernalFurnaceBlockEntity.serverTick(tickLevel, pos, tickState, (InfernalFurnaceBlockEntity) entity);
        }
        if (type == ModBlockEntities.INFERNAL_FURNACE_NOZZLE.get()) {
            return (tickLevel, pos, tickState, entity) -> InfernalFurnaceNozzleBlockEntity.serverTick(tickLevel, pos, tickState, (InfernalFurnaceNozzleBlockEntity) entity);
        }
        return null;
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
