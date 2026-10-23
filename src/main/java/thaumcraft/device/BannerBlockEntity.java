package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class BannerBlockEntity extends TcBlockEntity {
    private int facing;
    private int color = -1;
    private @Nullable Aspect aspect;
    private boolean onWall;

    public BannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANNER.get(), pos, state);
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
        setChanged();
    }

    public boolean isOnWall() {
        return onWall;
    }

    public void setOnWall(boolean onWall) {
        this.onWall = onWall;
        setChanged();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public @Nullable Aspect getAspect() {
        return aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        facing = input.getByteOr("facing", (byte) 0);
        color = input.getByteOr("color", (byte) 0);
        aspect = input.read("aspect", Aspect.CODEC).orElse(null);
        onWall = input.getBooleanOr("wall", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("facing", (byte) facing);
        output.putByte("color", (byte) color);
        output.storeNullable("aspect", Aspect.CODEC, aspect);
        output.putBoolean("wall", onWall);
    }
}
