package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class ArcaneLampBlockEntity extends TcBlockEntity {
    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_LAMP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, ArcaneLampBlockEntity lamp) {
        RandomSource random = level.getRandom();
        int x = pos.getX() + random.nextInt(16) - random.nextInt(16);
        int y = pos.getY() + random.nextInt(16) - random.nextInt(16);
        int z = pos.getZ() + random.nextInt(16) - random.nextInt(16);
        if (!level.hasChunkAt(new BlockPos(x, pos.getY(), z))) {
            return;
        }
        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (y > height + 4) {
            y = height + 4;
        }
        if (y < 5) {
            y = 5;
        }
        BlockPos target = new BlockPos(x, y, z);
        if (level.isInWorldBounds(target)
            && level.isEmptyBlock(target)
            && !level.getBlockState(target).is(ModBlocks.ARCANE_LAMP_LIGHT.get())
            && Math.max(level.getBrightness(LightLayer.BLOCK, target), level.getBrightness(LightLayer.SKY, target)) < 9) {
            level.setBlock(target, ModBlocks.ARCANE_LAMP_LIGHT.get().defaultBlockState(), 3);
        }
    }

    public void removeLights() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        for (int x = -15; x <= 15; x++) {
            for (int y = -15; y <= 15; y++) {
                for (int z = -15; z <= 15; z++) {
                    target.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
                    if (level.isInWorldBounds(target) && level.hasChunkAt(target) && level.getBlockState(target).is(ModBlocks.ARCANE_LAMP_LIGHT.get())) {
                        level.removeBlock(target, false);
                    }
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        removeLights();
        super.preRemoveSideEffects(pos, state);
    }
}
