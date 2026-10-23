package thaumcraft.wand.focus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.fx.Fx;
import thaumcraft.item.tool.SpecialMining;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class ExcavationFocus extends FocusItem {
    public static final FocusUpgradeType DOWSING = new FocusUpgradeType(20, "dowsing", new AspectList().add(Aspect.MINE, 1));

    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 15);
    private static AspectList cost2;
    private static final double RANGE = 10.0;
    private static final Map<String, State> STATES = new HashMap<>();

    public ExcavationFocus(Properties properties) {
        super(properties);
    }

    private static final class State {
        private long soundDelay;
        private float breakCount;
        private @Nullable BlockPos last;
    }

    private static State state(Player player) {
        UUID id = player.getUUID();
        return STATES.computeIfAbsent((player.level().isClientSide() ? "R" : "S") + id, key -> new State());
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BE" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 409606;
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_excavation");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        if (isUpgradedWith(focus, FocusUpgradeType.SILKTOUCH)) {
            if (cost2 == null) {
                cost2 = new AspectList()
                    .add(Aspect.AIR, 1)
                    .add(Aspect.FIRE, 1)
                    .add(Aspect.EARTH, 1)
                    .add(Aspect.WATER, 1)
                    .add(Aspect.ORDER, 1)
                    .add(Aspect.ENTROPY, 1)
                    .add(COST);
            }
            return cost2;
        }
        if (isUpgradedWith(focus, DOWSING)) {
            if (cost2 == null) {
                cost2 = new AspectList().add(Aspect.FIRE, 2).add(Aspect.ORDER, 2).add(COST);
            }
            return cost2;
        }
        return COST;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focus) {
        return WandFocusAnimation.CHARGE;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack stack, Player player, int count) {
        WandItem wand = (WandItem)stack.getItem();
        AspectList cost = getVisCost(stack);
        if (!wand.consumeAllVis(stack, player, cost, false, false)) {
            player.stopUsingItem();
            return;
        }
        Level level = player.level();
        State state = state(player);
        BlockHitResult hit = FocusUtils.getTargetBlock(level, player, true, RANGE);
        Vec3 look = player.getLookAngle();
        double targetX = player.getX() + look.x * RANGE;
        double targetY = player.getEyeY() + look.y * RANGE;
        double targetZ = player.getZ() + look.z * RANGE;
        int impact = 0;
        if (hit != null) {
            targetX = hit.getLocation().x;
            targetY = hit.getLocation().y;
            targetZ = hit.getLocation().z;
            impact = 5;
            if (!level.isClientSide() && state.soundDelay < System.currentTimeMillis()) {
                level.playSound(null, targetX, targetY, targetZ, ModSounds.RUMBLE.value(), SoundSource.PLAYERS, 0.3F, 1.0F);
                state.soundDelay = System.currentTimeMillis() + 1200L;
            }
        } else {
            state.soundDelay = 0L;
        }
        if (level.isClientSide()) {
            Fx.get().excavationBeam(level, player, targetX, targetY, targetZ, impact);
        }
        if (hit == null || !level.mayInteract(player, hit.getBlockPos())) {
            reset(state);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        float hardness = blockState.getDestroySpeed(level, pos);
        if (hardness < 0.0F) {
            return;
        }
        int potency = Wands.getFocusPotency(stack);
        float speed = 0.05F + potency * 0.1F;
        if (FocusUtils.isDiggableMaterial(blockState)) {
            speed = 0.25F + potency * 0.25F;
        }
        if (blockState.is(Blocks.OBSIDIAN)) {
            speed *= 3.0F;
        }
        if (!pos.equals(state.last)) {
            state.last = pos.immutable();
            state.breakCount = 0.0F;
            return;
        }
        float breakCount = state.breakCount;
        if (level.isClientSide() && breakCount > 0.0F && !blockState.isAir() && hardness > 0.0F) {
            Fx.get().excavationCrack(level, player, pos, blockState, (int)(breakCount / hardness * 9.0F));
        }
        if (level.isClientSide()) {
            state.breakCount = breakCount >= hardness ? 0.0F : breakCount + speed;
        } else if (breakCount >= hardness && wand.consumeAllVis(stack, player, cost, true, false)) {
            if (excavate(level, stack, player, pos, blockState)) {
                int enlarge = Wands.getFocusEnlarge(stack);
                for (int index = 0; index < enlarge; index++) {
                    if (wand.consumeAllVis(stack, player, cost, false, false) && breakNeighbour(player, pos, blockState, stack)) {
                        wand.consumeAllVis(stack, player, cost, true, false);
                    }
                }
            }
            reset(state);
        } else {
            state.breakCount = breakCount + speed;
        }
    }

    private static void reset(State state) {
        state.last = null;
        state.breakCount = 0.0F;
    }

    private boolean excavate(Level level, ItemStack wandStack, Player player, BlockPos pos, BlockState blockState) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (CommonHooks.fireBlockBreak(serverLevel, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, blockState).isCanceled()) {
            return false;
        }
        ItemStack focusStack = Wands.getFocusItem(wandStack);
        int fortune = Wands.getFocusTreasure(wandStack);
        boolean silk = isUpgradedWith(focusStack, FocusUpgradeType.SILKTOUCH);
        ItemStack tool = FocusUtils.harvestTool(serverLevel, fortune, silk);
        BlockEntity blockEntity = blockState.hasBlockEntity() ? serverLevel.getBlockEntity(pos) : null;
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(blockState, serverLevel, pos, blockEntity, serverPlayer, tool));
        if (isUpgradedWith(focusStack, DOWSING)) {
            float chance = 0.2F + fortune * 0.075F;
            for (int index = 0; index < drops.size(); index++) {
                ItemStack drop = drops.get(index);
                ItemStack replaced = SpecialMining.find(blockState, drop, chance, serverLevel.getRandom());
                if (!ItemStack.isSameItemSameComponents(drop, replaced)) {
                    drops.set(index, replaced);
                    serverLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 0.7F + serverLevel.getRandom().nextFloat() * 0.2F);
                }
            }
        }
        for (ItemStack drop : drops) {
            Block.popResource(serverLevel, pos, drop);
        }
        blockState.spawnAfterBreak(serverLevel, pos, tool, true);
        serverLevel.destroyBlock(pos, false, serverPlayer);
        return true;
    }

    private boolean breakNeighbour(Player player, BlockPos pos, BlockState blockState, ItemStack wandStack) {
        List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(directions, new java.util.Random(player.level().getRandom().nextLong()));
        for (Direction direction : directions) {
            BlockPos neighbour = pos.relative(direction);
            if (player.level().getBlockState(neighbour).equals(blockState) && excavate(player.level(), wandStack, player, neighbour, blockState)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPlayerStoppedUsingFocus(ItemStack wand, Level level, Player player, int count) {
        State state = state(player);
        reset(state);
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE};
            case 2 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, DOWSING};
            case 4 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, FocusUpgradeType.SILKTOUCH};
            default -> null;
        };
    }
}
