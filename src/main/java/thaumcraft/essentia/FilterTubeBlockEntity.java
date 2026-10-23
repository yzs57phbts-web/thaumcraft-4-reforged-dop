package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;

public class FilterTubeBlockEntity extends TubeBlockEntity implements AspectContainer {
    private @Nullable Aspect aspectFilter;

    public FilterTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FILTER_TUBE.get(), pos, state);
    }

    protected FilterTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable Aspect getAspectFilter() {
        return aspectFilter;
    }

    public void setAspectFilter(@Nullable Aspect aspectFilter) {
        this.aspectFilter = aspectFilter;
    }

    @Override
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(aspectFilter, restrict, directional);
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (aspectFilter != null) {
            aspects.add(aspectFilter, -1);
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && aspectFilter != null) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, new ItemStack(ModItems.JAR_LABEL.get())));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspectFilter != null) {
            output.store("AspectFilter", Aspect.CODEC, aspectFilter);
        }
    }
}
