package thaumcraft.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
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
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class InfusionTriggers {
    private static final int[][][] REPLACEMENT = {
        {{0, 0, 0}, {0, 9, 0}, {0, 0, 0}},
        {{1, 0, 1}, {0, 0, 0}, {1, 0, 1}},
        {{2, 0, 3}, {0, 0, 0}, {4, 0, 5}}
    };

    private InfusionTriggers() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(state -> state.is(ModBlocks.RUNIC_MATRIX.get()), InfusionTriggers::perform));
    }

    public static boolean perform(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!ResearchGate.isComplete(player, "INFUSION")) {
            return false;
        }
        for (int x = pos.getX() - 2; x <= pos.getX(); x++) {
            for (int y = pos.getY() - 2; y <= pos.getY(); y++) {
                for (int z = pos.getZ() - 2; z <= pos.getZ(); z++) {
                    BlockPos origin = new BlockPos(x, y, z);
                    if (fits(level, origin) && consume(player, wand)) {
                        if (!level.isClientSide()) {
                            replace(level, origin);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private static boolean consume(Player player, ItemStack wand) {
        AspectList cost = new AspectList();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            cost.add(aspect, 25);
        }
        return wand.getItem() instanceof WandItem item && item.consumeAllVisCrafting(wand, player, cost, true);
    }

    private static BlockPos cell(BlockPos origin, int layer, int x, int z) {
        return origin.offset(x, 2 - layer, z);
    }

    public static boolean fits(Level level, BlockPos origin) {
        for (int layer = 0; layer < 3; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = cell(origin, layer, x, z);
                    BlockState state = level.getBlockState(pos);
                    boolean corner = x != 1 && z != 1;
                    if (layer == 0 && x == 1 && z == 1) {
                        if (!state.is(ModBlocks.RUNIC_MATRIX.get())) {
                            return false;
                        }
                    } else if (layer == 1 && corner) {
                        if (!state.is(ModBlocks.ARCANE_STONE.get())) {
                            return false;
                        }
                    } else if (layer == 2 && corner) {
                        if (!state.is(ModBlocks.ARCANE_STONE_BRICKS.get())) {
                            return false;
                        }
                    } else if (layer == 2 && x == 1 && z == 1) {
                        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity)) {
                            return false;
                        }
                    } else if (!state.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void replace(Level level, BlockPos origin) {
        for (int layer = 0; layer < 3; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    int kind = REPLACEMENT[layer][x][z];
                    BlockPos pos = cell(origin, layer, x, z);
                    if (kind == 1) {
                        level.setBlock(pos, ModBlocks.INFUSION_PILLAR_TOP.get().defaultBlockState(), Block.UPDATE_ALL);
                        level.blockEvent(pos, ModBlocks.INFUSION_PILLAR_TOP.get(), 1, 0);
                    } else if (kind > 1 && kind < 9) {
                        level.setBlock(pos, ModBlocks.INFUSION_PILLAR.get().defaultBlockState(), Block.UPDATE_ALL);
                        if (level.getBlockEntity(pos) instanceof InfusionPillarBlockEntity pillar) {
                            pillar.setOrientation(kind);
                        }
                        level.blockEvent(pos, ModBlocks.INFUSION_PILLAR.get(), 1, 0);
                    } else if (kind == 9 && level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix) {
                        matrix.setActive(true);
                    }
                }
            }
        }
        level.playSound(null, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
