package thaumcraft.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class InfusionPillarBlockEntity extends TcBlockEntity {
    private int orientation;

    public InfusionPillarBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.INFUSION_PILLAR.get(), pos, state);
    }

    protected InfusionPillarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int value) {
        orientation = value;
        sync();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        orientation = input.getIntOr("orientation", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("orientation", orientation);
    }
}
