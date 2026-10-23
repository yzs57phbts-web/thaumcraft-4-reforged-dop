package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;

public class AdvancedAlchemicalFurnaceNozzleBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport {
    private @Nullable Direction facing;
    private @Nullable AdvancedAlchemicalFurnaceBlockEntity furnace;

    public AdvancedAlchemicalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get(), pos, state);
    }

    protected AdvancedAlchemicalFurnaceNozzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable AdvancedAlchemicalFurnaceBlockEntity getFurnace() {
        if (level == null) {
            return null;
        }
        if (furnace != null && !furnace.isRemoved() && level.getBlockEntity(furnace.getBlockPos()) == furnace) {
            return furnace;
        }
        furnace = null;
        facing = null;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof AdvancedAlchemicalFurnaceBlockEntity found) {
                furnace = found;
                facing = direction.getOpposite();
                break;
            }
        }
        return furnace;
    }

    public @Nullable Direction getFacing() {
        getFurnace();
        return facing;
    }

    @Override
    public AspectList getAspects() {
        AdvancedAlchemicalFurnaceBlockEntity source = getFurnace();
        return source != null ? source.getAspects() : new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        AdvancedAlchemicalFurnaceBlockEntity source = getFurnace();
        return source != null && source.takeFromContainer(aspect, amount);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        AdvancedAlchemicalFurnaceBlockEntity source = getFurnace();
        return source != null && source.getAspects().getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        AdvancedAlchemicalFurnaceBlockEntity source = getFurnace();
        return source == null ? 0 : source.getAspects().getAmount(aspect);
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == getFacing();
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        AdvancedAlchemicalFurnaceBlockEntity source = getFurnace();
        return source != null && !source.getAspects().isEmpty() ? source.getAspects().getAspects().get(0) : null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        Aspect type = getEssentiaType(face);
        return type == null ? 0 : getFurnace().getAspects().getAmount(type);
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
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
