package thaumcraft.essentia;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.block.ItemInteractions;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandInteractable;
import thaumcraft.wand.Wands;

public class JarBlock extends Block implements EntityBlock, WandInteractable {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 12, 13);

    private final boolean voidJar;

    public JarBlock(boolean voidJar, Properties properties) {
        super(properties);
        this.voidJar = voidJar;
    }

    public boolean isVoidJar() {
        return voidJar;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return voidJar ? new VoidJarBlockEntity(pos, state) : new JarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<?> expected = voidJar ? ModBlockEntities.VOID_JAR.get() : ModBlockEntities.JAR.get();
        if (level.isClientSide() || type != expected) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> JarBlockEntity.serverTick(tickLevel, pos, tickState, (JarBlockEntity) entity);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        super.setPlacedBy(level, pos, state, by, stack);
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return;
        }
        if (by != null) {
            jar.setFacing(by.getDirection().getOpposite());
        }
        AspectList aspects = AspectKeys.ASPECTS.get(stack);
        if (aspects != null && aspects.size() == 1) {
            Aspect aspect = aspects.getAspects().get(0);
            jar.setAspect(aspect);
            jar.setAmount(aspects.getAmount(aspect));
        }
        Aspect filter = EssentiaKeys.FILTER.get(stack);
        if (filter != null) {
            jar.setAspectFilter(filter);
        }
        jar.sync();
    }

    public ItemStack createDrop(JarBlockEntity jar) {
        boolean filled = jar.getAmount() > 0 || jar.getAspectFilter() != null;
        if (!filled) {
            return new ItemStack(this);
        }
        ItemStack drop = new ItemStack(voidJar ? ModBlocks.FILLED_VOID_JAR.get() : ModBlocks.FILLED_JAR.get());
        Aspect aspect = jar.getAspect();
        if (jar.getAmount() > 0 && aspect != null) {
            AspectKeys.ASPECTS.set(drop, new AspectList().add(aspect, jar.getAmount()));
        }
        if (jar.getAspectFilter() != null) {
            EssentiaKeys.FILTER.set(drop, jar.getAspectFilter());
        }
        return drop;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof JarBlockEntity jar) {
            return List.of(createDrop(jar));
        }
        return List.of(new ItemStack(this));
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (Wands.isWand(stack) && player.isShiftKeyDown()) {
            return ItemInteractions.skipping(onWandUseOn(level, pos, player, stack, hitResult.getDirection()));
        }
        return ItemInteractions.skipping(interact(level, pos, player, stack, hitResult.getDirection()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return interact(level, pos, player, ItemStack.EMPTY, hitResult.getDirection());
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (!player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        emptyJar(level, pos, jar);
        return InteractionResult.SUCCESS;
    }

    private static void emptyJar(Level level, BlockPos pos, JarBlockEntity jar) {
        if (level.isClientSide()) {
            return;
        }
        jar.setAmount(0);
        jar.setAspect(jar.getAspectFilter());
        jar.sync();
        level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());
        level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
        level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F,
            1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
    }

    private InteractionResult interact(Level level, BlockPos pos, Player player, ItemStack held, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && jar.getAspectFilter() != null && side == jar.getFacing()) {
            if (!level.isClientSide()) {
                jar.setAspectFilter(null);
                jar.sync();
                level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                Direction facing = jar.getFacing();
                level.addFreshEntity(new ItemEntity(
                    level,
                    pos.getX() + 0.5F + facing.getStepX() / 3.0F,
                    pos.getY() + 0.5F,
                    pos.getZ() + 0.5F + facing.getStepZ() / 3.0F,
                    new ItemStack(ModItems.JAR_LABEL.get())
                ));
            }
        } else if (player.isShiftKeyDown() && held.isEmpty()) {
            emptyJar(level, pos, jar);
        } else if (!held.isEmpty() && jar.getAspectFilter() == null && held.is(ModItems.JAR_LABEL.get())) {
            AspectList labelAspects = AspectKeys.ASPECTS.get(held);
            boolean labelled = labelAspects != null && !labelAspects.isEmpty();
            if (jar.getAmount() == 0 && !labelled) {
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                if (jar.getAmount() == 0) {
                    jar.setAspect(labelAspects.getAspects().get(0));
                }
                held.consume(1, player);
                jar.setFacing(player.getDirection().getOpposite());
                jar.setAspectFilter(jar.getAspect());
                jar.sync();
                level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof JarBlockEntity jar ? jar.getComparatorSignal() : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
