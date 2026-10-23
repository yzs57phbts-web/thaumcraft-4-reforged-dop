package thaumcraft.blockentity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.platform.CamouflagedBlockEntity;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class WardedBlockEntity extends CamouflagedBlockEntity {

    private BlockState disguise = Blocks.STONE.defaultBlockState();
    private byte light;
    private @Nullable UUID owner;

    public WardedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDED.get(), pos, state);
    }

    public void ward(BlockState disguise, int light, UUID owner) {
        this.disguise = disguise;
        this.light = (byte) light;
        this.owner = owner;
        sync();
    }

    public BlockState getDisguise() {
        return disguise;
    }

    public int getLight() {
        return light;
    }

    public boolean hasSameOwner(WardedBlockEntity other) {
        return java.util.Objects.equals(owner, other.owner);
    }

    public boolean isOwner(UUID player) {
        return player.equals(owner);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BlockState previous = disguise;
        disguise = input.read("disguise", BlockState.CODEC).orElse(Blocks.STONE.defaultBlockState());
        light = input.getByteOr("ll", (byte) 0);
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        if (level != null && level.isClientSide() && previous != disguise) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("disguise", BlockState.CODEC, disguise);
        output.putByte("ll", light);
        output.storeNullable("owner", UUIDUtil.CODEC, owner);
    }
}
