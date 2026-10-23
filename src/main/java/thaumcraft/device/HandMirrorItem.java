package thaumcraft.device;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import thaumcraft.item.InteractionResults;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModSounds;

public class HandMirrorItem extends Item {
    public HandMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MirrorKeys.LINK.has(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockState(pos).getBlock() instanceof MirrorBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MirrorBlockEntity) {
            MirrorKeys.LINK.set(stack, GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.inventoryMenu.broadcastChanges();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResults.holder(handleUse(level, player, hand), player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GlobalPos link = MirrorKeys.LINK.get(stack);
        if (link == null || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel targetLevel = MirrorLinks.levelOf(serverLevel, link);
        if (targetLevel == null) {
            return InteractionResult.PASS;
        }
        if (!(MirrorLinks.fetch(targetLevel, link.pos()) instanceof MirrorBlockEntity)) {
            breakLink(stack, player, level);
            return InteractionResult.PASS;
        }
        Menus.open(serverPlayer, new SimpleMenuProvider(
            (containerId, inventory, ignored) -> new HandMirrorMenu(containerId, inventory, stack),
            stack.getHoverName()
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        GlobalPos link = MirrorKeys.LINK.get(stack);
        if (link != null) {
            builder.add(Component.translatable("tc.handmirrorlinkedto").append(" " + MirrorLinks.describe(link)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, builder, flag);
    }

    private static void breakLink(ItemStack mirror, Player player, Level level) {
        MirrorKeys.LINK.remove(mirror);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.translatable("tc.handmirrorerror").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
        GlobalPos link = MirrorKeys.LINK.get(mirror);
        if (link == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        ServerLevel targetLevel = MirrorLinks.levelOf(serverLevel, link);
        if (targetLevel == null) {
            return false;
        }
        BlockEntity entity = MirrorLinks.fetch(targetLevel, link.pos());
        if (entity instanceof MirrorBlockEntity target) {
            targetLevel.addFreshEntity(MirrorLinks.ejected(targetLevel, link.pos(), target.getFacing(), items.copy()));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1F, 1.0F);
            targetLevel.blockEvent(link.pos(), target.getBlockState().getBlock(), 1, 0);
            return true;
        }
        breakLink(mirror, player, level);
        return false;
    }
}
