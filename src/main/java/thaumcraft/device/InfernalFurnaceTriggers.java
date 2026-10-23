package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class InfernalFurnaceTriggers {
    private static final int OBSIDIAN = 1;
    private static final int BRICK = 2;
    private static final int LAVA = 3;
    private static final int AIR = 4;
    private static final int[][][] BLUEPRINT = {
        {{BRICK, OBSIDIAN, BRICK}, {OBSIDIAN, AIR, OBSIDIAN}, {BRICK, OBSIDIAN, BRICK}},
        {{BRICK, OBSIDIAN, BRICK}, {OBSIDIAN, LAVA, OBSIDIAN}, {BRICK, OBSIDIAN, BRICK}},
        {{BRICK, OBSIDIAN, BRICK}, {OBSIDIAN, OBSIDIAN, OBSIDIAN}, {BRICK, OBSIDIAN, BRICK}}
    };

    private InfernalFurnaceTriggers() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(
            state -> state.is(Blocks.OBSIDIAN) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.IRON_BARS),
            InfernalFurnaceTriggers::create
        ));
    }

    private static boolean create(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!ResearchGate.isComplete(player, "INFERNALFURNACE")) {
            return false;
        }
        for (int x = pos.getX() - 2; x <= pos.getX(); x++) {
            for (int y = pos.getY() - 2; y <= pos.getY(); y++) {
                for (int z = pos.getZ() - 2; z <= pos.getZ(); z++) {
                    BlockPos origin = new BlockPos(x, y, z);
                    if (fits(level, origin) && consume(player, wand)) {
                        if (level instanceof ServerLevel serverLevel) {
                            form(serverLevel, origin);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean consume(Player player, ItemStack wand) {
        AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50);
        return wand.getItem() instanceof WandItem item && item.consumeAllVisCrafting(wand, player, cost, true);
    }

    private static int classify(BlockState state) {
        if (state.isAir()) {
            return AIR;
        }
        if (state.is(Blocks.OBSIDIAN)) {
            return OBSIDIAN;
        }
        if (state.is(Blocks.NETHER_BRICKS)) {
            return BRICK;
        }
        if (state.is(Blocks.LAVA) && state.getFluidState().isSource() && state.getFluidState().getType() == Fluids.LAVA) {
            return LAVA;
        }
        return state.is(Blocks.IRON_BARS) ? 5 : 0;
    }

    public static boolean fits(Level level, BlockPos origin) {
        boolean barsFound = false;
        for (int layer = 0; layer < 3; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    int found = classify(level.getBlockState(origin.offset(x, 2 - layer, z)));
                    if (found != BLUEPRINT[layer][x][z]) {
                        if (layer != 1 || barsFound || found != 5 || x == z || x != 1 && z != 1) {
                            return false;
                        }
                        barsFound = true;
                    }
                }
            }
        }
        return barsFound;
    }

    public static void form(ServerLevel level, BlockPos origin) {
        InfernalFurnaceBlock block = ModBlocks.INFERNAL_FURNACE.get();
        Direction facing = Direction.NORTH;
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (level.getBlockState(origin.offset(x, 1, z)).is(Blocks.IRON_BARS)) {
                    facing = x == 0 ? Direction.WEST : x == 2 ? Direction.EAST : z == 0 ? Direction.NORTH : Direction.SOUTH;
                }
            }
        }
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockPos target = origin.offset(x, y, z);
                    BlockState old = level.getBlockState(target);
                    if (old.isAir()) {
                        continue;
                    }
                    int tile;
                    if (old.is(Blocks.LAVA)) {
                        tile = InfernalTiles.CORE;
                    } else if (old.is(Blocks.IRON_BARS)) {
                        tile = InfernalTiles.BARS;
                    } else {
                        int[] canonical = InfernalTiles.toCanonical(x - 1, z - 1, facing);
                        tile = InfernalTiles.wall(y, canonical[0] + 1, canonical[1] + 1);
                    }
                    level.setBlock(target, block.tileState(tile, facing), Block.UPDATE_ALL);
                    level.blockEvent(target, block, InfernalFurnaceBlock.EVENT_SPARKLE, 4);
                }
            }
        }
        level.playSound(null, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
