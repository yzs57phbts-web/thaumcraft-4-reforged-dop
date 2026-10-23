package thaumcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public final class Baubles {
    private static final BaubleType[] SLOT_TYPES = {BaubleType.AMULET, BaubleType.RING, BaubleType.RING, BaubleType.BELT};
    private static final String[] CURIO_IDS = {"necklace", "ring", "ring", "belt"};
    private static final int[] CURIO_INDICES = {0, 0, 1, 0};

    private Baubles() {
    }

    public static int slotCount() {
        return SLOT_TYPES.length;
    }

    public static BaubleType slotType(int slot) {
        return SLOT_TYPES[slot];
    }

    public static String curioId(BaubleType type) {
        return switch (type) {
            case AMULET -> "necklace";
            case RING -> "ring";
            case BELT -> "belt";
        };
    }

    private static IDynamicStackHandler handler(LivingEntity entity, int slot) {
        return CuriosApi.getCuriosInventory(entity)
            .flatMap(inventory -> inventory.getStacksHandler(CURIO_IDS[slot]))
            .map(stacks -> stacks.getStacks())
            .filter(stacks -> CURIO_INDICES[slot] < stacks.getSlots())
            .orElse(null);
    }

    public static ItemStack get(Player player, int slot) {
        if (slot < 0 || slot >= SLOT_TYPES.length) {
            return ItemStack.EMPTY;
        }
        IDynamicStackHandler stacks = handler(player, slot);
        return stacks == null ? ItemStack.EMPTY : stacks.getStackInSlot(CURIO_INDICES[slot]);
    }

    public static void set(Player player, int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_TYPES.length) {
            return;
        }
        IDynamicStackHandler stacks = handler(player, slot);
        if (stacks != null) {
            stacks.setStackInSlot(CURIO_INDICES[slot], stack);
        }
    }

    public static List<ItemStack> stacks(Player player) {
        List<ItemStack> result = new ArrayList<>(SLOT_TYPES.length);
        for (int slot = 0; slot < SLOT_TYPES.length; slot++) {
            result.add(get(player, slot));
        }
        return result;
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (entity instanceof Player player) {
            for (ItemStack stack : stacks(player)) {
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isEquipped(Player player, Item item) {
        return !find(player, stack -> stack.is(item)).isEmpty();
    }
}
