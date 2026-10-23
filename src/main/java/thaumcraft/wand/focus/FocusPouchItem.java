package thaumcraft.wand.focus;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import thaumcraft.item.InteractionResults;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import thaumcraft.menu.FocusPouchMenu;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Menus;

public class FocusPouchItem extends Item implements Bauble {
    public static final int SIZE = 18;

    public FocusPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResults.holder(handleUse(level, player, hand), player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Menus.open(serverPlayer, new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new FocusPouchMenu(containerId, inventory, player.getItemInHand(hand)),
                Component.translatable("item.thaumcraft.focus_pouch")
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public int pouchSize(ItemStack pouch) {
        return SIZE;
    }

    public boolean canStore(ItemStack stack) {
        return stack.getItem() instanceof FocusItem;
    }

    public boolean canRetainStored(ItemStack stack) {
        return canStore(stack);
    }

    public static int size(ItemStack pouch) {
        return pouch.getItem() instanceof FocusPouchItem item ? item.pouchSize(pouch) : SIZE;
    }

    public static List<ItemStack> getInventory(ItemStack pouch) {
        int size = size(pouch);
        NonNullList<ItemStack> stored = NonNullList.withSize(size, ItemStack.EMPTY);
        FocusKeys.POUCH.getOrDefault(pouch, ItemContainerContents.EMPTY).copyInto(stored);
        FocusPouchItem item = pouch.getItem() instanceof FocusPouchItem pouchItem ? pouchItem : null;
        List<ItemStack> contents = new ArrayList<>(size);
        for (ItemStack stack : stored) {
            boolean allowed = item != null ? item.canRetainStored(stack) : stack.getItem() instanceof FocusItem;
            contents.add(allowed ? stack : ItemStack.EMPTY);
        }
        return contents;
    }

    public static void setInventory(ItemStack pouch, List<ItemStack> contents) {
        int size = size(pouch);
        List<ItemStack> stored = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) {
            stored.add(slot < contents.size() ? contents.get(slot) : ItemStack.EMPTY);
        }
        FocusKeys.POUCH.set(pouch, ItemContainerContents.fromItems(stored));
    }
}
