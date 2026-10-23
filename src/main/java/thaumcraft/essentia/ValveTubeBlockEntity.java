package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class ValveTubeBlockEntity extends TubeBlockEntity {
    private boolean allowFlow = true;
    private boolean wasPoweredLastTick;
    private float rotation;

    public ValveTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VALVE_TUBE.get(), pos, state);
    }

    protected ValveTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isAllowFlow() {
        return allowFlow;
    }

    public void setAllowFlow(boolean allowFlow) {
        this.allowFlow = allowFlow;
        sync();
    }

    public float getRotation() {
        return rotation;
    }

    public boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public void playSqueek(Level soundLevel) {
        soundLevel.playSound(
            null,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            ModSounds.SQUEEK.get(),
            SoundSource.BLOCKS,
            0.7F,
            0.9F + soundLevel.getRandom().nextFloat() * 0.2F
        );
    }

    @Override
    protected void tick() {
        if (level == null) {
            return;
        }
        if (!level.isClientSide() && count % 5 == 0) {
            boolean powered = gettingPower();
            if (wasPoweredLastTick && !powered && !allowFlow) {
                allowFlow = true;
                playSqueek(level);
                sync();
            }
            if (!wasPoweredLastTick && powered && allowFlow) {
                allowFlow = false;
                playSqueek(level);
                sync();
            }
            wasPoweredLastTick = powered;
        }
        if (level.isClientSide()) {
            if (!allowFlow && rotation < 360.0F) {
                rotation += 20.0F;
            } else if (allowFlow && rotation > 0.0F) {
                rotation -= 20.0F;
            }
        }
        super.tick();
    }

    @Override
    protected boolean canFaceTowards(Direction candidate) {
        return !canConnectSide(candidate);
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face != getFacing() && super.isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        if (allowFlow) {
            super.setSuction(aspect, amount);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        allowFlow = input.getBooleanOr("flow", true);
        wasPoweredLastTick = input.getBooleanOr("hadpower", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("flow", allowFlow);
        output.putBoolean("hadpower", wasPoweredLastTick);
    }
}
