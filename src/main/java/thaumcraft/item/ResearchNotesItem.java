package thaumcraft.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNote;
import thaumcraft.research.ResearchNoteKeys;
import thaumcraft.research.ResearchNotes;

public class ResearchNotesItem extends Item {
    private final boolean discovery;

    public ResearchNotesItem(Properties properties, boolean discovery) {
        super(discovery ? properties : properties.stacksTo(1));
        this.discovery = discovery;
    }

    public boolean isDiscovery() {
        return discovery;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = handleUse(level, player, hand);
        return InteractionResults.holder(result, player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNote note = ResearchNoteKeys.NOTE.get(stack);
        boolean completable = note != null && note.complete() && !ResearchManager.isComplete(player, note.key());
        boolean unknown = note == null && !discovery;
        if (!completable && !unknown) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (completable) {
            if (ResearchManager.hasRequisites(player, note.key())) {
                ResearchManager.completeWithSiblings(serverPlayer, note.key());
                stack.shrink(1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("tc.researcherror"));
            }
            return InteractionResult.SUCCESS;
        }
        String key = ResearchManager.findHiddenResearch(serverPlayer);
        if (key == null) {
            stack.shrink(1);
            level.addFreshEntity(new ItemEntity(
                level,
                player.getX(),
                player.getY() + player.getEyeHeight() / 2.0F,
                player.getZ(),
                new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get(), 7 + level.getRandom().nextInt(3))
            ));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ERASE.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
        } else {
            ResearchItem research = ResearchCategories.getResearch(key);
            ItemStack created = research == null ? ItemStack.EMPTY : ResearchNotes.createNote(research, level.getRandom());
            if (!created.isEmpty()) {
                player.setItemInHand(hand, created);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WRITE.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ResearchNote note = ResearchNoteKeys.NOTE.get(stack);
        if (note == null) {
            if (!discovery) {
                tooltip.add(Component.translatable("item.researchnotes.unknown.1").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("item.researchnotes.unknown.2").withStyle(ChatFormatting.BLUE));
            }
            return;
        }
        ResearchItem research = ResearchCategories.getResearch(note.key());
        if (research == null) {
            return;
        }
        tooltip.add(Component.translatable(research.nameKey()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(research.textKey()).withStyle(ChatFormatting.ITALIC));
        int warp = Math.min(5, research.warp());
        if (warp > 0) {
            String level = Component.translatable("tc.forbidden.level." + warp).getString();
            tooltip.add(Component.literal(Component.translatable("tc.forbidden").getString().replace("%n", level)).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (note.copies() > 0) {
            tooltip.add(Component.literal("x" + (note.copies() + 1)).withStyle(ChatFormatting.GRAY));
        }
    }
}
