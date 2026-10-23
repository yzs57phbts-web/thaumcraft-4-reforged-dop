package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;

public class ThaumatoriumTopBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport {
    public ThaumatoriumTopBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.THAUMATORIUM_TOP.get(), pos, state);
    }

    protected ThaumatoriumTopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable ThaumatoriumBlockEntity getThaumatorium() {
        return level != null && level.getBlockEntity(worldPosition.below()) instanceof ThaumatoriumBlockEntity bottom ? bottom : null;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? amount : bottom.addToContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom != null && bottom.takeFromContainer(aspect, amount);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom != null && bottom.doesContainerContainAmount(aspect, amount);
    }

    @Override
    public int containerContains(Aspect aspect) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? 0 : bottom.containerContains(aspect);
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom != null && bottom.isConnectable(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom != null && bottom.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        if (bottom != null) {
            bottom.setSuction(aspect, amount);
        }
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? null : bottom.getSuctionType(face);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? 0 : bottom.getSuctionAmount(face);
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
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? 0 : bottom.takeEssentia(aspect, amount, face);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? 0 : bottom.addEssentia(aspect, amount, face);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public AspectList getAspects() {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        return bottom == null ? new AspectList() : bottom.getAspects();
    }

    @Override
    public void setAspects(AspectList aspects) {
        ThaumatoriumBlockEntity bottom = getThaumatorium();
        if (bottom != null) {
            bottom.setAspects(aspects);
        }
    }

    public @Nullable Container exposedItems(@Nullable Direction side) {
        ThaumatoriumBlockEntity thaumatorium = getThaumatorium();
        return thaumatorium == null ? null : thaumatorium.exposedItems(side);
    }
}
