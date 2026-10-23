package thaumcraft.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface WandInteractable {
    default InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        return InteractionResult.PASS;
    }

    default InteractionResult onWandUse(Level level, BlockPos pos, Player player, ItemStack wand) {
        return InteractionResult.PASS;
    }

    default void onUsingWandTick(ItemStack wand, Player player, int count) {
    }

    default void onWandStoppedUsing(ItemStack wand, Level level, Player player, int count) {
    }
}
