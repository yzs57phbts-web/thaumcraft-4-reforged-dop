package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class WardingAuraBlockEntity extends TcBlockEntity {
    private int count;

    public WardingAuraBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDING_AURA.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, WardingAuraBlockEntity aura) {
        if (aura.count == 0) {
            aura.count = level.getRandom().nextInt(100);
        }
        aura.count++;
        if (aura.count % 100 == 0
            && !level.getBlockState(pos.below()).is(ModBlocks.WARDING_PAVING_STONE.get())
            && !level.getBlockState(pos.below(2)).is(ModBlocks.WARDING_PAVING_STONE.get())) {
            level.removeBlock(pos, false);
        }
    }
}
