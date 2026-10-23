package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.block.ResearchTableBlock;
import thaumcraft.block.ResearchTablePartBlock;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.registry.ModBlocks;

public class ScribingToolsItem extends Item {
    private static final Direction[] SEARCH = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public ScribingToolsItem(Properties properties) {
        super(properties);
    }

    public static Properties properties(Properties properties) {
        return properties.durability(100);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(ModBlocks.TABLE.get())) {
            return InteractionResult.PASS;
        }
        for (Direction direction : SEARCH) {
            BlockPos partnerPos = pos.relative(direction);
            if (!level.getBlockState(partnerPos).is(ModBlocks.TABLE.get())) {
                continue;
            }
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            level.setBlock(partnerPos, ModBlocks.RESEARCH_TABLE_PART.get().defaultBlockState().setValue(ResearchTablePartBlock.FACING, direction.getOpposite()), Block.UPDATE_CLIENTS);
            level.setBlock(pos, ModBlocks.RESEARCH_TABLE.get().defaultBlockState().setValue(ResearchTableBlock.FACING, direction), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
                table.setItem(ResearchTableBlockEntity.SCRIBE_SLOT, context.getItemInHand().copyWithCount(1));
                Player player = context.getPlayer();
                if (player == null || !player.hasInfiniteMaterials()) {
                    context.getItemInHand().shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
