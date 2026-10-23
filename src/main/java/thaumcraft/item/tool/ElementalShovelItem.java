package thaumcraft.item.tool;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Item;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.fx.Fx;
import thaumcraft.item.gear.Repairable;
import thaumcraft.wand.focus.Architect;

public class ElementalShovelItem extends ShovelItem implements Repairable, Architect {
    private static final Block[] EFFECTIVE = {
        Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK,
        Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM
    };
    private static final int PLACE_COLOR = 8401408;
    private static final int GRASS_COLOR = 3;

    public ElementalShovelItem(Properties properties) {
        super(Registration.tier(TcMaterials.ELEMENTAL_TOOL), properties);
    }

    public static void cycleOrientation(Player player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() instanceof ElementalShovelItem) {
            ToolKeys.setOrientation(held, ToolKeys.getOrientation(held) + 1);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (level.getBlockEntity(pos) != null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        Item blockItem = state.getBlock().asItem();
        boolean placed = false;
        for (BlockPos target : targets(stack, level, pos, side, player)) {
            if (level.isClientSide()) {
                if (player.isCreative() || has(player, blockItem) || state.is(Blocks.GRASS_BLOCK) && has(player, Items.DIRT)) {
                    Fx.get().blockSparkle(level, target, PLACE_COLOR, 4);
                    placed = true;
                }
                continue;
            }
            if (blockItem != Items.AIR && ToolHarvest.consumeInventoryItem(player, blockItem)) {
                place(level, target, state, stack, player, context.getHand());
                placed = true;
            } else if (state.is(Blocks.GRASS_BLOCK) && ToolHarvest.consumeInventoryItem(player, Items.DIRT)) {
                place(level, target, Blocks.DIRT.defaultBlockState(), stack, player, context.getHand());
                placed = true;
            }
        }
        return placed ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean has(Player player, Item item) {
        return item != Items.AIR && player.getInventory().hasAnyMatching(stack -> stack.is(item));
    }

    private static void place(Level level, BlockPos target, BlockState state, ItemStack stack, Player player, InteractionHand hand) {
        level.playSound(null, target, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.setBlock(target, state, Block.UPDATE_ALL);
        stack.hurtAndBreak(1, player, ((hand) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
    }

    private static List<BlockPos> targets(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        List<BlockPos> result = new ArrayList<>();
        int face = side.get3DDataValue();
        int orientation = ToolKeys.getOrientation(stack);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                int x = 0;
                int y = 0;
                int z = 0;
                if (orientation == 1) {
                    y = b;
                    if (face <= 1) {
                        if (facing(player) % 2 != 0) {
                            z = a;
                        } else {
                            x = a;
                        }
                    } else if (face <= 3) {
                        z = a;
                    } else {
                        x = a;
                    }
                } else if (orientation == 2) {
                    if (face <= 1) {
                        y = b;
                        if (facing(player) % 2 != 0) {
                            z = a;
                        } else {
                            x = a;
                        }
                    } else {
                        z = b;
                        x = a;
                    }
                } else if (face <= 1) {
                    x = a;
                    z = b;
                } else if (face <= 3) {
                    x = a;
                    y = b;
                } else {
                    z = a;
                    y = b;
                }
                BlockPos target = pos.offset(x + side.getStepX(), y + side.getStepY(), z + side.getStepZ());
                if (isOpen(level, target)) {
                    result.add(target);
                }
            }
        }
        return result;
    }

    private static int facing(Player player) {
        return Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5) & 3;
    }

    private static boolean isOpen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
            || state.is(Blocks.VINE)
            || state.is(Blocks.SHORT_GRASS)
            || state.is(Blocks.FERN)
            || state.is(Blocks.DEAD_BUSH)
            || state.getFluidState().is(FluidTags.WATER)
            || state.canBeReplaced();
    }

    private static boolean isEffective(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL) || stack.isCorrectToolForDrops(state)) {
            return true;
        }
        for (Block block : EFFECTIVE) {
            if (state.is(block)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (ToolHarvest.isHarvesting()) {
            return true;
        }
        if (owner.isShiftKeyDown()) {
            return super.mineBlock(stack, level, state, pos, owner);
        }
        if (!(level instanceof ServerLevel serverLevel) || !(owner instanceof ServerPlayer player) || !isEffective(stack, state)) {
            return true;
        }
        ToolHarvest.breakArea(stack, serverLevel, player, pos, target -> isEffective(stack, target), GRASS_COLOR);
        return true;
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        if (!player.isShiftKeyDown()) {
            return List.of();
        }
        return targets(stack, level, pos, side, player);
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return false;
    }
}
