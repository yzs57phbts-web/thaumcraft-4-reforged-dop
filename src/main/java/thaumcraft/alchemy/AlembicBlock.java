package thaumcraft.alchemy;

import thaumcraft.blockentity.TcBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.Wands;

public class AlembicBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AlembicBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic) {
            float ratio = (float) alembic.getAmount() / alembic.getMaxAmount();
            return (int) Math.floor(ratio * 14.0F) + (alembic.getAmount() > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (Wands.isWand(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        RandomSource random = level.getRandom();
        if (stack.is(ModItems.JAR_LABEL.get()) && alembic.getAspectFilter() == null) {
            AspectList labelAspects = AspectKeys.ASPECTS.get(stack);
            Aspect labelAspect = labelAspects == null || labelAspects.isEmpty() ? null : labelAspects.getAspects().get(0);
            if (alembic.getAmount() == 0 && labelAspect == null) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                if (alembic.getAmount() == 0) {
                    alembic.setAspect(labelAspect);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                alembic.setAspectFilter(alembic.getAspect());
                alembic.sync();
                level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        boolean plainJar = stack.is(ModBlocks.WARDED_JAR.get().asItem()) || stack.is(ModBlocks.VOID_JAR.get().asItem());
        if (alembic.getAmount() > 0 && alembic.getAspect() != null && (stack.getItem() instanceof FilledJarItem || plainJar)) {
            if (!level.isClientSide() && fillJar(alembic, stack, player, hand)) {
                alembic.sync();
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.3F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean fillJar(AlembicBlockEntity alembic, ItemStack stack, Player player, InteractionHand hand) {
        Aspect aspect = alembic.getAspect();
        if (stack.getItem() instanceof FilledJarItem jar) {
            AspectList held = jar.getAspects(stack);
            Aspect filter = AlchemyHooks.getJarFilter(stack);
            boolean accepts = (held == null || held.visSize() == 0 || held.getAmount(aspect) > 0) && (filter == null || filter == aspect);
            if (!accepts) {
                return false;
            }
            boolean voidJar = stack.is(ModBlocks.FILLED_VOID_JAR.get());
            int amount = Math.min(held == null ? 64 : 64 - held.visSize(), alembic.getAmount());
            if (voidJar) {
                amount = alembic.getAmount();
            }
            if (amount <= 0) {
                return false;
            }
            ItemStack single = stack.copyWithCount(1);
            AspectList contents = held == null ? new AspectList() : held;
            contents.add(aspect, amount);
            if (contents.getAmount(aspect) > 64) {
                contents.reduce(aspect, contents.getAmount(aspect) - 64);
            }
            jar.setAspects(single, contents);
            alembic.removeAmount(amount);
            if (stack.getCount() > 1) {
                stack.shrink(1);
                giveOrDrop(player, single);
            } else {
                player.setItemInHand(hand, single);
            }
            return true;
        }
        boolean voidJar = stack.is(ModBlocks.VOID_JAR.get().asItem());
        ItemStack filled = new ItemStack(voidJar ? ModBlocks.FILLED_VOID_JAR.get() : ModBlocks.FILLED_JAR.get());
        FilledJarItem filledItem = (FilledJarItem) filled.getItem();
        filledItem.setAspects(filled, new AspectList().add(aspect, alembic.getAmount()));
        alembic.removeAmount(alembic.getAmount());
        stack.shrink(1);
        giveOrDrop(player, filled);
        return true;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic) || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        RandomSource random = level.getRandom();
        if (player.isShiftKeyDown()) {
            if (alembic.getAspectFilter() != null) {
                if (!level.isClientSide()) {
                    alembic.setAspectFilter(null);
                    alembic.sync();
                    Direction face = hitResult.getDirection();
                    ItemEntity label = new ItemEntity(
                        level,
                        pos.getX() + 0.5 + face.getStepX() / 3.0,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + face.getStepZ() / 3.0,
                        new ItemStack(ModItems.JAR_LABEL.get())
                    );
                    level.addFreshEntity(label);
                    level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.1F);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                alembic.clear();
                alembic.sync();
                level.playSound(null, pos, ModSounds.ALEMBICKNOCK.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.3F);
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            int amount = alembic.getAmount();
            int max = alembic.getMaxAmount();
            String key = "tile.alembic.msg.1";
            if (alembic.getAspect() != null && amount != 0) {
                if (amount < max * 0.4) {
                    key = "tile.alembic.msg.2";
                } else if (amount < max * 0.8) {
                    key = "tile.alembic.msg.3";
                } else if (amount < max) {
                    key = "tile.alembic.msg.4";
                } else {
                    key = "tile.alembic.msg.5";
                }
            }
            player.sendSystemMessage(Component.literal("§3").append(Component.translatable(key)));
            level.playSound(null, pos, ModSounds.ALEMBICKNOCK.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
