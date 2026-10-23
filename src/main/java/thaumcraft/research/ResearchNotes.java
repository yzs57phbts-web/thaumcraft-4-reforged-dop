package thaumcraft.research;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.registry.ModItems;

public final class ResearchNotes {
    private ResearchNotes() {
    }

    public static ItemStack createNote(ResearchItem item, RandomSource random) {
        Aspect primary = item.primaryTag();
        if (primary == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.RESEARCH_NOTES.get());
        ResearchNoteKeys.NOTE.set(stack, new ResearchNote(item.key(), primary.getColor(), false, 0, HexGrid.generateHexes(item, random)));
        return stack;
    }

    public static boolean createNoteForPlayer(ServerPlayer player, String key) {
        if (findNoteSlot(player, key) >= 0) {
            return true;
        }
        ResearchItem item = ResearchCategories.getResearch(key);
        if (item == null || !consumeInk(player, false)) {
            return false;
        }
        int paper = findItem(player, Items.PAPER);
        if (paper < 0) {
            return false;
        }
        ItemStack note = createNote(item, player.getRandom());
        if (note.isEmpty()) {
            return false;
        }
        player.getInventory().getItem(paper).shrink(1);
        consumeInk(player, true);
        if (!player.getInventory().add(note)) {
            Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), note);
        }
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static boolean consumeInk(Player player, boolean doit) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ScribingToolsItem && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doit) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        return false;
    }

    public static int findNoteSlot(Player player, String key) {
        List<ItemStack> items = player.getInventory().items;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.is(ModItems.RESEARCH_NOTES.get()) || stack.is(ModItems.DISCOVERY.get())) {
                ResearchNote note = ResearchNoteKeys.NOTE.get(stack);
                if (note != null && note.key().equals(key)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    public static int findItem(Player player, net.minecraft.world.item.Item item) {
        List<ItemStack> items = player.getInventory().items;
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }
}
