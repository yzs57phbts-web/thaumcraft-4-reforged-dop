package thaumcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import thaumcraft.blockentity.TcBlockEntity;

public abstract class CamouflagedBlockEntity extends TcBlockEntity {
    public static final ModelProperty<BlockState> DISGUISE = new ModelProperty<>();

    protected CamouflagedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract BlockState getDisguise();

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(DISGUISE, getDisguise()).build();
    }
}
