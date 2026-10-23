package thaumcraft.wand.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.HoleBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class PortableHoleFocus extends FocusItem {
    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 10).add(Aspect.AIR, 10);
    private static final ResourceLocation OUTER_LANDS = Thaumcraft.id("outer_lands");

    public PortableHoleFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BPH" + super.getSortingHelper(focus);
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_portablehole");
    }

    @Override
    public @Nullable ResourceLocation getDepthLayerTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_portablehole_depth");
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 594985;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    private static boolean isBlocked(Level level, BlockPos pos, BlockState state) {
        return state.is(FocusUtils.PORTABLE_HOLE_BLACKLIST)
            || state.is(Blocks.BEDROCK)
            || state.is(ModBlocks.HOLE.get())
            || state.isAir()
            || state.getDestroySpeed(level, pos) == -1.0F;
    }

    public static boolean createHole(Level level, BlockPos pos, int side, byte count, int max) {
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null || isBlocked(level, pos, state) || state.canBeReplaced()) {
            return false;
        }
        if (level.isClientSide()) {
            Fx.get().blockSparkle(level, pos, HoleBlockEntity.SPARKLE_COLOR, 1);
            return true;
        }
        if (level.setBlock(pos, ModBlocks.HOLE.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS)
            && level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.setup(state, (short) max, count, (byte) side);
        }
        return true;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos origin = blockHit.getBlockPos();
        if (level.dimension().location().equals(OUTER_LANDS)) {
            if (!level.isClientSide()) {
                level.playSound(null, origin, ModSounds.WANDFAIL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        Direction side = blockHit.getDirection();
        Direction step = side.getOpposite();
        int maxDistance = 33 + Wands.getFocusEnlarge(wand) * 8;
        BlockPos pos = origin;
        int distance;
        for (distance = 0; distance < maxDistance; distance++) {
            if (isBlocked(level, pos, level.getBlockState(pos))) {
                break;
            }
            pos = pos.relative(step);
        }
        AspectList cost = getVisCost(wand);
        for (Aspect aspect : cost.getAspects()) {
            cost.merge(aspect, cost.getAmount(aspect) * distance);
        }
        if (((WandItem) wand.getItem()).consumeAllVis(wand, player, cost, true, false)) {
            int extend = getUpgradeLevel(Wands.getFocusItem(wand), FocusUpgradeType.EXTEND);
            createHole(level, origin, side.get3DDataValue(), (byte) (distance + 1), (short) (120 + 60 * extend));
        }
        if (!level.isClientSide()) {
            level.playSound(null, origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return rank >= 1 && rank <= 5 ? new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.EXTEND} : null;
    }
}
