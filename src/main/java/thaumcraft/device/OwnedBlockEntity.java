package thaumcraft.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class OwnedBlockEntity extends TcBlockEntity {
    public static final int IRON = 0;
    public static final int GOLD = 1;

    public record Access(int type, UUID player) {
        public static final Codec<Access> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("type").forGetter(Access::type),
            UUIDUtil.CODEC.fieldOf("player").forGetter(Access::player)
        ).apply(instance, Access::new));
    }

    private @Nullable UUID owner;
    private final List<Access> accessList = new ArrayList<>();

    public OwnedBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.OWNED.get(), pos, state);
    }

    protected OwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static @Nullable OwnedBlockEntity at(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof OwnedBlockEntity owned ? owned : null;
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public boolean isOwner(UUID player) {
        return player.equals(owner);
    }

    public List<Access> getAccessList() {
        return List.copyOf(accessList);
    }

    public boolean hasAccess(UUID player, int type) {
        return accessList.contains(new Access(type, player));
    }

    public boolean isOwnerOrHasAccess(UUID player) {
        return isOwner(player) || hasAccess(player, IRON) || hasAccess(player, GOLD);
    }

    public void addAccess(int type, UUID player) {
        accessList.add(new Access(type, player));
        sync();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        accessList.clear();
        for (Access access : input.listOrEmpty("access", Access.CODEC)) {
            accessList.add(access);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("owner", UUIDUtil.CODEC, owner);
        ValueOutput.TypedOutputList<Access> list = output.list("access", Access.CODEC);
        for (Access access : accessList) {
            list.add(access);
        }
    }
}
