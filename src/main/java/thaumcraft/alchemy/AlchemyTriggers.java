package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AlchemyTriggers {
    private static final int[][][] FURNACE_BLUEPRINT = {
        {{4, 4, 4}, {4, 3, 4}, {4, 4, 4}},
        {{1, 2, 1}, {2, 0, 2}, {1, 2, 1}}
    };

    private AlchemyTriggers() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WandTriggers.register(state -> state.is(BlockTags.CAULDRONS), AlchemyTriggers::createCrucible);
            WandTriggers.register(state -> state.is(ModBlocks.ALCHEMICAL_CONSTRUCT.get()), AlchemyTriggers::createThaumatorium);
            WandTriggers.register(
                state -> state.is(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()) || state.is(ModBlocks.ALCHEMICAL_CONSTRUCT.get()),
                AlchemyTriggers::createAdvancedAlchemicalFurnace
            );
        });
    }

    private static boolean consume(Player player, ItemStack wand, AspectList cost) {
        return wand.getItem() instanceof WandItem item && item.consumeAllVisCrafting(wand, player, cost, true);
    }

    private static void playWand(Level level, BlockPos pos) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void sparkle(ServerLevel level, BlockPos pos, BlockPos center) {
        ModNetwork.sendToNear(level, null, center.getX(), center.getY(), center.getZ(), 32.0, new BlockSparkleFxPayload(pos, -9999));
    }

    private static boolean createCrucible(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (level instanceof ServerLevel serverLevel) {
            playWand(serverLevel, pos);
            serverLevel.setBlock(pos, ModBlocks.CRUCIBLE.get().defaultBlockState(), Block.UPDATE_ALL);
            serverLevel.updateNeighborsAt(pos, ModBlocks.CRUCIBLE.get());
            serverLevel.blockEvent(pos, ModBlocks.CRUCIBLE.get(), CrucibleBlockEntity.EVENT_SPARKLE, 1);
        }
        return true;
    }

    private static boolean createThaumatorium(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!ResearchGate.isComplete(player, "THAUMATORIUM")) {
            return false;
        }
        BlockPos bottom = pos;
        if (!isBlock(level, bottom.above(), ModBlocks.ALCHEMICAL_CONSTRUCT.get()) || !isBlock(level, bottom.below(), ModBlocks.CRUCIBLE.get())) {
            if (!isBlock(level, bottom.below(), ModBlocks.ALCHEMICAL_CONSTRUCT.get()) || !isBlock(level, bottom.below(2), ModBlocks.CRUCIBLE.get())) {
                return false;
            }
            bottom = bottom.below();
        }
        AspectList cost = new AspectList().add(Aspect.FIRE, 15).add(Aspect.ORDER, 30).add(Aspect.WATER, 30);
        if (!consume(player, wand, cost)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            Direction facing = side.getAxis().isVertical() ? player.getDirection().getOpposite() : side;
            BlockPos top = bottom.above();
            BlockState bottomState = ModBlocks.THAUMATORIUM.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING, facing);
            BlockState topState = ModBlocks.THAUMATORIUM_TOP.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING, facing);
            serverLevel.setBlock(bottom, bottomState, Block.UPDATE_ALL);
            serverLevel.setBlock(top, topState, Block.UPDATE_ALL);
            sparkle(serverLevel, bottom, bottom);
            sparkle(serverLevel, top, bottom);
            playWand(serverLevel, bottom);
        }
        return true;
    }

    private static boolean isBlock(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).is(block);
    }

    private static boolean matchesBlueprint(Level level, BlockPos furnace, int cell, BlockPos target) {
        return switch (cell) {
            case 1 -> isBlock(level, target, ModBlocks.ALEMBIC.get());
            case 2 -> isBlock(level, target, ModBlocks.ALCHEMICAL_CONSTRUCT.get());
            case 4 -> isBlock(level, target, ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
            case 3 -> isBlock(level, target, ModBlocks.ALCHEMICAL_FURNACE.get());
            default -> true;
        };
    }

    private static boolean createAdvancedAlchemicalFurnace(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!ResearchGate.isComplete(player, "ADVALCHEMYFURNACE")) {
            return false;
        }
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                for (int c = -1; c <= 1; c++) {
                    BlockPos core = pos.offset(a, b, c);
                    if (!isBlock(level, core, ModBlocks.ALCHEMICAL_FURNACE.get())) {
                        continue;
                    }
                    for (int layer = 0; layer <= 1; layer++) {
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                if (!matchesBlueprint(level, core, FURNACE_BLUEPRINT[layer][x + 1][z + 1], core.offset(x, layer, z))) {
                                    return false;
                                }
                            }
                        }
                    }
                    AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.WATER, 50).add(Aspect.ORDER, 50);
                    if (!consume(player, wand, cost)) {
                        return false;
                    }
                    if (level instanceof ServerLevel serverLevel) {
                        formFurnace(serverLevel, core);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void formFurnace(ServerLevel level, BlockPos core) {
        AdvancedAlchemicalFurnaceBlock block = ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get();
        level.setBlock(core, block.partState(AdvancedAlchemicalFurnaceBlock.Part.CORE), Block.UPDATE_ALL);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                boolean corner = x != 0 && z != 0;
                level.setBlock(core.offset(x, 0, z), block.partState(corner ? AdvancedAlchemicalFurnaceBlock.Part.FRAME_CORNER : AdvancedAlchemicalFurnaceBlock.Part.NOZZLE), Block.UPDATE_ALL);
                level.setBlock(core.offset(x, 1, z), block.partState(corner ? AdvancedAlchemicalFurnaceBlock.Part.FRAME_ALEMBIC : AdvancedAlchemicalFurnaceBlock.Part.FRAME_EDGE), Block.UPDATE_ALL);
            }
        }
        playWand(level, core);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    sparkle(level, core.offset(x, y, z), core);
                }
            }
        }
    }
}
