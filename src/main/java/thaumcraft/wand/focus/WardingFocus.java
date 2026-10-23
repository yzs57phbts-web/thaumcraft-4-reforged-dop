package thaumcraft.wand.focus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.WardedBlockEntity;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.Wands;

public class WardingFocus extends FocusItem implements Architect {
    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
    private static final int SPARKLE_COLOR = 16556032;
    private static final Map<String, Long> DELAY = new HashMap<>();

    public WardingFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BWA" + super.getSortingHelper(focus);
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_warding");
    }

    @Override
    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_warding_orn");
    }

    @Override
    public @Nullable ResourceLocation getDepthLayerTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_warding_depth");
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 16771535;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    private static boolean isSolid(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.isRedstoneConductor(level, pos) && !state.isSignalSource();
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!(level instanceof ServerLevel serverLevel) || !(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = blockHit.getBlockPos();
        String key = origin.getX() + ":" + origin.getY() + ":" + origin.getZ() + ":" + level.dimension().location();
        Long until = DELAY.get(key);
        if (until != null && until > System.currentTimeMillis()) {
            return InteractionResult.SUCCESS;
        }
        DELAY.put(key, System.currentTimeMillis() + 500L);
        BlockEntity target = level.getBlockEntity(origin);
        WandItem wandItem = (WandItem) wand.getItem();
        if (target == null && isSolid(level, origin)) {
            for (BlockPos pos : getArchitectBlocks(wand, level, origin, blockHit.getDirection(), player)) {
                if (!wandItem.consumeAllVis(wand, player, getVisCost(wand), true, false)) {
                    break;
                }
                if (level.getBlockEntity(pos) == null && isSolid(level, pos)) {
                    BlockState state = level.getBlockState(pos);
                    int light = state.getLightEmission(level, pos);
                    level.setBlock(pos, ModBlocks.WARDED_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(pos) instanceof WardedBlockEntity warded) {
                        warded.ward(state, light, player.getUUID());
                        ModNetwork.sendToNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 32.0, new BlockSparkleFxPayload(pos, SPARKLE_COLOR));
                    }
                }
            }
            level.playSound(null, origin, ModSounds.ZAP.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
        } else if (target instanceof WardedBlockEntity warded && warded.isOwner(player.getUUID())) {
            for (BlockPos pos : getArchitectBlocks(wand, level, origin, blockHit.getDirection(), player)) {
                if (level.getBlockEntity(pos) instanceof WardedBlockEntity other && other.isOwner(player.getUUID())) {
                    level.setBlock(pos, other.getDisguise(), Block.UPDATE_ALL);
                    ModNetwork.sendToNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 32.0, new BlockSparkleFxPayload(pos, SPARKLE_COLOR));
                }
            }
            level.playSound(null, origin, ModSounds.ZAP.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
            case 2 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.ARCHITECT};
            case 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE};
            default -> null;
        };
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.ENLARGE) || isUpgradedWith(focus, FocusUpgradeType.ARCHITECT);
    }

    @Override
    public int getMaxAreaSize(ItemStack focus) {
        return 3 + getUpgradeLevel(focus, FocusUpgradeType.ENLARGE);
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack wand, Level level, BlockPos pos, Direction side, Player player) {
        List<BlockPos> out = new ArrayList<>();
        boolean tiles = level.getBlockEntity(pos) instanceof WardedBlockEntity;
        int sizeX = 0;
        int sizeY = 0;
        int sizeZ = 0;
        if (isUpgradedWith(Wands.getFocusItem(wand), FocusUpgradeType.ARCHITECT)) {
            sizeX = WandManager.getAreaX(wand);
            sizeY = WandManager.getAreaY(wand);
            sizeZ = WandManager.getAreaZ(wand);
        }
        int face = side.get3DDataValue();
        Set<BlockPos> checked = new HashSet<>();
        if (face != 2 && face != 3) {
            checkNeighbours(level, pos, pos, face, sizeX, sizeY, sizeZ, out, checked, player, tiles);
        } else {
            checkNeighbours(level, pos, pos, face, sizeZ, sizeY, sizeX, out, checked, player, tiles);
        }
        return out;
    }

    private static void checkNeighbours(Level level, BlockPos origin, BlockPos pos, int side, int sizeX, int sizeY, int sizeZ, List<BlockPos> list, Set<BlockPos> checked, Player player, boolean tiles) {
        if (!checked.add(pos)) {
            return;
        }
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        switch (side) {
            case 0, 1 -> {
                if (dx > sizeX || dz > sizeZ || dy > sizeY) {
                    return;
                }
            }
            case 2, 3 -> {
                if (dx > sizeX || dy > sizeZ || dz > sizeY) {
                    return;
                }
            }
            default -> {
                if (dy > sizeX || dz > sizeZ || dx > sizeY) {
                    return;
                }
            }
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (tiles && !(blockEntity instanceof WardedBlockEntity)) {
            return;
        }
        if (!tiles && (blockEntity != null || !isSolid(level, pos))) {
            return;
        }
        if (tiles && !((WardedBlockEntity) blockEntity).isOwner(player.getUUID())) {
            return;
        }
        if (level.getBlockState(pos).isAir()) {
            return;
        }
        list.add(pos);
        for (Direction direction : Direction.values()) {
            checkNeighbours(level, origin, pos.relative(direction), side, sizeX, sizeY, sizeZ, list, checked, player, tiles);
        }
    }

    @Override
    public boolean showAxis(ItemStack wand, Level level, Player player, Direction side, Direction.Axis axis) {
        int dim = WandManager.getAreaDim(wand);
        if (dim == 0) {
            return true;
        }
        return switch (side.get3DDataValue()) {
            case 0, 1 -> axis == Direction.Axis.X && dim == 1 || axis == Direction.Axis.Z && dim == 2 || axis == Direction.Axis.Y && dim == 3;
            case 2, 3 -> axis == Direction.Axis.Y && dim == 1 || axis == Direction.Axis.X && dim == 2 || axis == Direction.Axis.Z && dim == 3;
            default -> axis == Direction.Axis.Y && dim == 1 || axis == Direction.Axis.Z && dim == 2 || axis == Direction.Axis.X && dim == 3;
        };
    }
}
