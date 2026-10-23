package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.node.Node;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class NodeJarTrigger {
    private static final int[][][] BLUEPRINT = {
        {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
        {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
        {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
        {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}
    };

    private NodeJarTrigger() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(state -> state.is(Blocks.GLASS), NodeJarTrigger::perform));
    }

    private static boolean perform(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!ResearchGate.isComplete(player, "NODEJAR")) {
            return false;
        }
        for (int x = pos.getX() - 2; x <= pos.getX(); x++) {
            for (int y = pos.getY() - 3; y <= pos.getY(); y++) {
                for (int z = pos.getZ() - 2; z <= pos.getZ(); z++) {
                    BlockPos origin = new BlockPos(x, y, z);
                    if (fits(level, origin) && consume(player, wand)) {
                        if (!level.isClientSide()) {
                            replace(level, origin);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean consume(Player player, ItemStack wand) {
        AspectList cost = new AspectList();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            cost.add(aspect, 70);
        }
        return wand.getItem() instanceof WandItem item && item.consumeAllVisCrafting(wand, player, cost, true);
    }

    private static BlockPos cell(BlockPos origin, int layer, int x, int z) {
        return origin.offset(x, 2 - layer, z);
    }

    private static boolean fits(Level level, BlockPos origin) {
        for (int layer = 0; layer < 4; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = cell(origin, layer, x, z);
                    int expected = BLUEPRINT[layer][x][z];
                    if (expected == 1 && !level.getBlockState(pos).is(BlockTags.WOODEN_SLABS)) {
                        return false;
                    }
                    if (expected == 2 && !level.getBlockState(pos).is(Blocks.GLASS)) {
                        return false;
                    }
                    if (expected == 3) {
                        BlockEntity entity = level.getBlockEntity(pos);
                        if (!(entity instanceof Node) || entity instanceof NodeJarBlockEntity) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void replace(Level level, BlockPos origin) {
        for (int layer = 0; layer < 4; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = cell(origin, layer, x, z);
                    if (BLUEPRINT[layer][x][z] == 3) {
                        Node node = (Node) level.getBlockEntity(pos);
                        AspectList aspects = node.getAspects().copy();
                        NodeType type = node.getNodeType();
                        NodeModifier modifier = node.getNodeModifier();
                        if (level.getRandom().nextFloat() < 0.75F) {
                            if (modifier == null) {
                                modifier = NodeModifier.PALE;
                            } else if (modifier == NodeModifier.BRIGHT) {
                                modifier = null;
                            } else if (modifier == NodeModifier.PALE) {
                                modifier = NodeModifier.FADING;
                            }
                        }
                        String id = node.getId();
                        node.setAspects(new AspectList());
                        level.setBlock(pos, ModBlocks.NODE_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
                        if (level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
                            jar.setAspects(aspects);
                            jar.setNodeModifier(modifier);
                            jar.setNodeType(type);
                            jar.setId(id);
                            jar.sync();
                        }
                        level.blockEvent(pos, ModBlocks.NODE_JAR.get(), 9, 0);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        level.playSound(null, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
