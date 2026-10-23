package thaumcraft.modern;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.*;
import thaumcraft.registry.*;

/** Vanilla sign editing, dye, glow ink, wax and front/back text, with TC wood skins. */
public final class ModernSigns {
    public static final Map<String, WoodType> TYPES = new LinkedHashMap<>();

    public static void register(String wood, BlockSetType set) {
        WoodType type = WoodType.register(new WoodType("thaumcraft:" + wood, set));
        TYPES.put(wood, type);
        var standing = ModBlocks.BLOCKS.register(wood + "_sign", () -> new StandingSignBlock(type, properties()) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Entity(pos, state); }
            @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> blockType) {
                return createTickerHelper(blockType, ModernContent.SIGN.get(), SignBlockEntity::tick);
            }
        });
        var wall = ModBlocks.BLOCKS.register(wood + "_wall_sign", () -> new WallSignBlock(type, properties()) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Entity(pos, state); }
            @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> blockType) {
                return createTickerHelper(blockType, ModernContent.SIGN.get(), SignBlockEntity::tick);
            }
        });
        ModernContent.WOOD.put(wood + "_sign", standing);
        ModernContent.WOOD.put(wood + "_wall_sign", wall);
        ModItems.ITEMS.registerItem(wood + "_sign", p -> new SignItem(p.stacksTo(16), standing.get(), wall.get()));

        var hanging = ModBlocks.BLOCKS.register(wood + "_hanging_sign", () -> new CeilingHangingSignBlock(type, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)));
        var wallHanging = ModBlocks.BLOCKS.register(wood + "_wall_hanging_sign", () -> new WallHangingSignBlock(type, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)));
        ModernContent.WOOD.put(wood + "_hanging_sign", hanging);
        ModernContent.WOOD.put(wood + "_wall_hanging_sign", wallHanging);
        ModItems.ITEMS.registerItem(wood + "_hanging_sign", p -> new HangingSignItem(hanging.get(), wallHanging.get(), p.stacksTo(16)));
    }

    private static BlockBehaviour.Properties properties() { return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN); }

    public static final class Entity extends SignBlockEntity {
        public Entity(BlockPos pos, BlockState state) { super(ModernContent.SIGN.get(), pos, state); }
    }
}
