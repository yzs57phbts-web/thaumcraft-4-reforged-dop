package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;

public class InfernalFurnaceNozzleBlockEntity extends TcBlockEntity implements EssentiaTransport {
    private int drawDelay;

    public InfernalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.INFERNAL_FURNACE_NOZZLE.get(), pos, state);
    }

    protected InfernalFurnaceNozzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private @Nullable Direction towardFurnace() {
        if (level == null) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof InfernalFurnaceBlockEntity) {
                return direction;
            }
        }
        return null;
    }

    public @Nullable InfernalFurnaceBlockEntity getFurnace() {
        Direction direction = towardFurnace();
        return direction == null ? null : (InfernalFurnaceBlockEntity) level.getBlockEntity(worldPosition.relative(direction));
    }

    public @Nullable Direction getFacing() {
        Direction direction = towardFurnace();
        return direction == null ? null : direction.getOpposite();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceNozzleBlockEntity nozzle) {
        InfernalFurnaceBlockEntity furnace = nozzle.getFurnace();
        if (furnace != null && furnace.getSpeedyTime() < 60 && nozzle.drawEssentia()) {
            furnace.addSpeedyTime(600);
        }
    }

    private boolean drawEssentia() {
        if (++drawDelay % 5 != 0) {
            return false;
        }
        Direction facing = getFacing();
        if (facing == null) {
            return false;
        }
        EssentiaTransport source = Essentia.getConnectable(level, worldPosition, facing);
        if (source != null) {
            if (!source.canOutputTo(facing.getOpposite())) {
                return false;
            }
            return source.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)
                && source.takeEssentia(Aspect.FIRE, 1, facing.getOpposite()) == 1;
        }
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return getFacing() != null;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return getFacing() != null;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return Aspect.FIRE;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        InfernalFurnaceBlockEntity furnace = getFurnace();
        return furnace != null && furnace.getSpeedyTime() < 40 ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
