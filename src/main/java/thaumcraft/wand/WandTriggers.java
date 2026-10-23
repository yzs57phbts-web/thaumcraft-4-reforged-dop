package thaumcraft.wand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class WandTriggers {
    private record Entry(Predicate<BlockState> target, WandTrigger trigger) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private WandTriggers() {
    }

    public static void register(Predicate<BlockState> target, WandTrigger trigger) {
        ENTRIES.add(new Entry(target, trigger));
    }

    public static boolean hasTrigger(BlockState state) {
        for (Entry entry : ENTRIES) {
            if (entry.target().test(state)) {
                return true;
            }
        }
        return false;
    }

    public static boolean perform(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        for (Entry entry : ENTRIES) {
            if (entry.target().test(state) && entry.trigger().perform(level, wand, player, pos, side)) {
                return true;
            }
        }
        return false;
    }
}
