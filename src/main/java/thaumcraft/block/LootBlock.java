package thaumcraft.block;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.loot.TcLoot;
import thaumcraft.registry.ModSounds;

public class LootBlock extends Block {
    public enum Tier implements StringRepresentable {
        COMMON,
        UNCOMMON,
        RARE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Kind {
        URN,
        CRATE
    }

    public static final EnumProperty<Tier> TIER = EnumProperty.create("tier", Tier.class);

    private static final VoxelShape URN_SHAPE = Block.box(2.0, 1.0, 2.0, 14.0, 13.0, 14.0);
    private static final VoxelShape CRATE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    private static SoundType urnSounds;

    private final Kind kind;

    public LootBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(TIER, Tier.COMMON));
    }

    public Kind kind() {
        return kind;
    }

    public static ItemStack stackForState(BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (state.getBlock() instanceof LootBlock) {
            stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(TIER, state.getValue(TIER)));
        }
        return stack;
    }

    public static BlockState stateForStack(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        BlockState state = block.defaultBlockState();
        if (block instanceof LootBlock) {
            BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
            Tier tier = properties.get(TIER);
            if (tier != null) {
                state = state.setValue(TIER, tier);
            }
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return kind == Kind.URN ? URN_SHAPE : CRATE_SHAPE;
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        if (kind != Kind.URN) {
            return super.getSoundType(state);
        }
        if (urnSounds == null) {
            urnSounds = new SoundType(1.0F, 0.7F, ModSounds.URNBREAK.get(), SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
        }
        return urnSounds;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        int tier = state.getValue(TIER).ordinal();
        int rolls = 1 + tier + level.getRandom().nextInt(3);
        for (int index = 0; index < rolls; index++) {
            ItemStack loot = TcLoot.generateLoot(level.registryAccess(), tier, level.getRandom());
            if (!loot.isEmpty()) {
                Block.popResource(level, pos, loot);
            }
        }
    }
}
