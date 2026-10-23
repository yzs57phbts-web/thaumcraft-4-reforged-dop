package thaumcraft.block;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Keeps the host-rock appearance on the existing ore id, without changing its loot. */
public class HostRockOreBlock extends DropExperienceBlock {
    public static final BooleanProperty DEEPSLATE = BooleanProperty.create("deepslate");

    public HostRockOreBlock(IntProvider experience, Properties properties) {
        this(experience, properties, false);
    }

    public HostRockOreBlock(IntProvider experience, Properties properties, boolean deepslate) {
        super(experience, properties);
        registerDefaultState(stateDefinition.any().setValue(DEEPSLATE, deepslate));
    }

    @Override
    public net.minecraft.world.item.ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, BlockState state) {
        return new net.minecraft.world.item.ItemStack(thaumcraft.registry.DeepOreVariants.select(state, state.getValue(DEEPSLATE)).getBlock());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DEEPSLATE);
    }
}
