package thaumcraft.device;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModSounds;

public class MirrorItem extends BlockItem {
    public MirrorItem(MirrorBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        if (player == null || level.isClientSide() || state.getBlock() != getBlock()) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity mirror) {
            if (!mirror.isLinkValid(true)) {
                ItemStack linkedStack = stack.copyWithCount(1);
                MirrorKeys.LINK.set(linkedStack, GlobalPos.of(level.dimension(), pos));
                level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
                if (!player.getInventory().add(linkedStack)) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), linkedStack));
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.inventoryMenu.broadcastChanges();
            } else {
                player.sendSystemMessage(MirrorLinks.ALREADY_LINKED);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        GlobalPos link = MirrorKeys.LINK.get(stack);
        if (link != null) {
            MirrorLinks.appendLink("Linked to ", link, builder::add);
        }
        super.appendHoverText(stack, context, builder, flag);
    }
}
