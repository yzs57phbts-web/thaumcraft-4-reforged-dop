package thaumcraft.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.ManaPodBlockEntity;
import thaumcraft.registry.ModBlocks;

public class ManaPodBlock extends Block implements EntityBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final int[] BOTTOM = {12, 10, 8, 6, 5, 4, 3, 2};
    private static final VoxelShape[] SHAPES = new VoxelShape[8];

    static {
        for (int age = 0; age < 8; age++) {
            SHAPES[age] = Block.box(4.0, BOTTOM[age], 4.0, 12.0, 16.0, 12.0);
        }
    }

    public ManaPodBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    public static boolean canHangFrom(LevelReader level, BlockPos logPos) {
        return level.getBiome(logPos).is(Tags.Biomes.IS_MAGICAL) && level.getBlockState(logPos).is(BlockTags.LOGS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canHangFrom(level, pos.above());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (!canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        } else if (random.nextInt(30) == 0 && level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod) {
            pod.checkGrowth();
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return super.getDestroyProgress(state, player, level, pos) * (8 - state.getValue(AGE));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        int age = state.getValue(AGE);
        if (age < 2) {
            return drops;
        }
        int count = 1;
        if (age == 7 && params.getLevel().getRandom().nextFloat() > 0.33F) {
            count = 2;
        }
        Aspect aspect = Aspect.PLANT;
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof ManaPodBlockEntity pod && pod.getAspect() != null) {
            aspect = pod.getAspect();
        }
        for (int index = 0; index < count; index++) {
            ItemStack bean = new ItemStack(ModBlocks.MANA_BEAN.get());
            AspectKeys.ASPECTS.set(bean, new AspectList().add(aspect, 1));
            drops.add(bean);
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.MANA_BEAN.get());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaPodBlockEntity(pos, state);
    }
}
