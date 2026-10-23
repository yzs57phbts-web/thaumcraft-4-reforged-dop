package thaumcraft.arcane;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.vis.VisRelayBlockEntity;
import thaumcraft.wand.Wands;

public class VisChargeRelayBlockEntity extends VisRelayBlockEntity {
    private static final int DRAIN = 5;
    private static final int SYNC_INTERVAL = 20;

    private int chargeCounter;
    private boolean wandChanged;

    public VisChargeRelayBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VIS_CHARGE_RELAY.get(), pos, state);
    }

    protected VisChargeRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VisChargeRelayBlockEntity entity) {
        VisRelayBlockEntity.tick(level, pos, state, entity);
        if (!level.isClientSide()) {
            entity.charge(level);
        }
    }

    public @Nullable ArcaneWorkbenchBlockEntity getWorkbench() {
        return level != null && level.getBlockEntity(worldPosition.below()) instanceof ArcaneWorkbenchBlockEntity workbench ? workbench : null;
    }

    public boolean isCharging() {
        ArcaneWorkbenchBlockEntity workbench = getWorkbench();
        if (workbench == null) {
            return false;
        }
        ItemStack wand = workbench.getWand();
        return Wands.isWand(wand) && Wands.getAspectsWithRoom(wand).size() > 0;
    }

    private void charge(Level tickLevel) {
        ArcaneWorkbenchBlockEntity workbench = getWorkbench();
        if (workbench == null) {
            return;
        }
        ItemStack wand = workbench.getWand();
        if (Wands.isWand(wand)) {
            AspectList room = Wands.getAspectsWithRoom(wand);
            for (Aspect aspect : room.getAspects()) {
                int drain = Math.min(DRAIN, Wands.getMaxVis(wand) - Wands.getVis(wand, aspect));
                if (drain > 0) {
                    int consumed = consumeVis(aspect, drain);
                    if (consumed > 0) {
                        Wands.addRealVis(wand, aspect, consumed, true);
                        wandChanged = true;
                    }
                }
            }
        }
        chargeCounter++;
        if (wandChanged && chargeCounter % SYNC_INTERVAL == 0) {
            wandChanged = false;
            workbench.markWandChanged();
        }
    }
}
