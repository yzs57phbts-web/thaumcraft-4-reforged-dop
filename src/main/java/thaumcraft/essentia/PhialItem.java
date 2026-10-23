package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectProvidingItem;
import thaumcraft.item.AspectContainerItem;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;

public class PhialItem extends AspectContainerItem implements AspectProvidingItem {
    public static final int CAPACITY = 8;

    private final boolean filled;

    public PhialItem(boolean filled, Properties properties) {
        super(properties, " x ", true);
        this.filled = filled;
    }

    public boolean isFilled() {
        return filled;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (!filled) {
            return fill(level, pos, player, stack, entity);
        }
        return empty(level, pos, player, stack, entity);
    }

    private InteractionResult fill(Level level, BlockPos pos, Player player, ItemStack stack, @Nullable BlockEntity entity) {
        Aspect aspect = null;
        if (entity instanceof JarBlockEntity jar && jar.getAmount() >= CAPACITY) {
            aspect = jar.getAspect();
        } else if (entity instanceof AspectContainer container && level.getBlockState(pos).is(ModBlocks.ALEMBIC.get())) {
            AspectList contents = container.getAspects();
            for (Aspect candidate : contents.getAspects()) {
                if (contents.getAmount(candidate) >= CAPACITY) {
                    aspect = candidate;
                    break;
                }
            }
        }
        if (aspect == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        AspectContainer container = (AspectContainer) entity;
        if (container.takeFromContainer(aspect, CAPACITY)) {
            ItemStack phial = new ItemStack(ModItems.ESSENTIA_PHIAL.get());
            AspectKeys.ASPECTS.set(phial, new AspectList().add(aspect, CAPACITY));
            stack.consume(1, player);
            give(level, pos, player, phial);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 0.25F, 1.0F);
            player.containerMenu.broadcastChanges();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult empty(Level level, BlockPos pos, Player player, ItemStack stack, @Nullable BlockEntity entity) {
        AspectList contents = AspectKeys.ASPECTS.get(stack);
        if (contents == null || contents.size() != 1 || !(entity instanceof JarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        Aspect aspect = contents.getAspects().get(0);
        if (jar.getAmount() > jar.getMaxAmount() - CAPACITY || !jar.doesContainerAccept(aspect)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (jar.addToContainer(aspect, CAPACITY) == 0) {
            stack.consume(1, player);
            give(level, pos, player, new ItemStack(ModItems.GLASS_PHIAL.get()));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 0.25F, 1.0F);
            player.containerMenu.broadcastChanges();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void give(Level level, BlockPos pos, Player player, ItemStack result) {
        if (!player.getInventory().add(result)) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, result));
        }
    }
}
