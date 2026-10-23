package thaumcraft.wand.focus;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Architect {
    List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player);

    boolean showAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis);
}
