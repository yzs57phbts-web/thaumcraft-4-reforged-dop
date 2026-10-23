package thaumcraft.device;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class ArcaneLevitatorBlockEntity extends TcBlockEntity {
    private int counter;
    private int rangeAbove;
    private boolean requiresUpdate = true;
    private boolean lastPowerState;

    public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_LEVITATOR.get(), pos, state);
    }

    public int getRangeAbove() {
        return rangeAbove;
    }

    public boolean getLastPowerState() {
        return lastPowerState;
    }

    public void requireUpdate() {
        requiresUpdate = true;
    }

    private static boolean blocksLift(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof ArcaneLevitatorBlock || state.isSolidRender(level, pos);
    }

    public static void tick(Level level, BlockPos pos, ArcaneLevitatorBlockEntity levitator) {
        levitator.counter++;
        if (levitator.requiresUpdate || levitator.counter % 100 == 0) {
            levitator.lastPowerState = ArcaneLevitatorBlock.isGettingPower(level, pos);
            levitator.requiresUpdate = false;
            int max = 10;
            for (int count = 1;
                level.getBlockState(pos.below(count)).is(ModBlocks.ARCANE_LEVITATOR.get()) && !level.hasNeighborSignal(pos.below(count));
                max += 10
            ) {
                count++;
            }
            levitator.rangeAbove = 0;
            while (levitator.rangeAbove < max && !blocksLift(level, pos.above(1 + levitator.rangeAbove))) {
                levitator.rangeAbove++;
            }
        }
        if (levitator.rangeAbove > 0 && !ArcaneLevitatorBlock.isGettingPower(level, pos)) {
            List<Entity> targets = level.getEntitiesOfClass(
                Entity.class,
                new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1 + levitator.rangeAbove, pos.getZ() + 1)
            );
            for (Entity entity : targets) {
                if (entity instanceof ItemEntity || entity.isPushable() || entity instanceof AbstractHorse) {
                    Vec3 motion = entity.getDeltaMovement();
                    if (entity.isShiftKeyDown()) {
                        if (motion.y < 0.0) {
                            entity.setDeltaMovement(motion.x, motion.y * 0.9F, motion.z);
                        }
                    } else if (motion.y < 0.35F) {
                        entity.setDeltaMovement(motion.x, motion.y + 0.1F, motion.z);
                    }
                    entity.fallDistance = 0.0F;
                }
            }
        }
    }
}
