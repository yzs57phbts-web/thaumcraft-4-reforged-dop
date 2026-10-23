package thaumcraft.wand.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public final class FocusUtils {
    public static final TagKey<Block> PORTABLE_HOLE_BLACKLIST = TagKey.create(Registries.BLOCK, Thaumcraft.id("portable_hole_blacklist"));
    public static final TagKey<Block> TAINT = TagKey.create(Registries.BLOCK, Thaumcraft.id("taint"));

    private FocusUtils() {
    }

    public static @Nullable BlockHitResult getTargetBlock(Level level, Player player, boolean collidersOnly, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(range));
        ClipContext.Block block = collidersOnly ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;
        BlockHitResult hit = level.clip(new ClipContext(start, end, block, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    public static boolean isBlockExposed(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!level.getBlockState(pos.relative(direction)).isSolidRender(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack harvestTool(Level level, int fortune, boolean silkTouch) {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        if (silkTouch) {
            enchant(level, tool, Enchantments.SILK_TOUCH, 1);
        } else if (fortune > 0) {
            enchant(level, tool, Enchantments.FORTUNE, fortune);
        }
        return tool;
    }

    private static void enchant(Level level, ItemStack tool, ResourceKey<Enchantment> key, int enchantLevel) {
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
            .ifPresent(holder -> EnchantmentHelper.updateEnchantments(tool, enchantments -> enchantments.set(holder, enchantLevel)));
    }

    public static boolean isDiggableMaterial(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }
}
