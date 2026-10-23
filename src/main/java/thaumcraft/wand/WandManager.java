package thaumcraft.wand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import net.minecraft.Util;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.platform.Baubles;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusPouchItem;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumcraft.wand.focus.TradeFocus;

public final class WandManager {
    public static final String REMOVE_FOCUS = "REMOVE";

    private static final int POUCH_STRIDE = 1000;
    private static final int UNSET_AREA = -1;
    private static final Map<LivingEntity, Map<FocusItem, Long>> COOLDOWN_SERVER = new WeakHashMap<>();
    private static final Map<LivingEntity, Map<FocusItem, Long>> COOLDOWN_CLIENT = new WeakHashMap<>();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private WandManager() {
    }

    public static float getTotalVisDiscount(@Nullable Player player, @Nullable Aspect aspect) {
        if (player == null) {
            return 0.0F;
        }
        int total = 0;
        for (ItemStack stack : Baubles.stacks(player)) {
            if (stack.getItem() instanceof VisDiscountGear gear) {
                total += gear.getVisDiscount(stack, player, aspect);
            }
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof VisDiscountGear gear) {
                total += gear.getVisDiscount(stack, player, aspect);
            }
        }
        total -= getVisExhaustPenalty(player);
        return total / 100.0F;
    }

    private static int getVisExhaustPenalty(Player player) {
        MobEffectInstance normal = player.getEffect(ModEffects.VIS_EXHAUST);
        MobEffectInstance infectious = player.getEffect(ModEffects.INFECTIOUS_VIS_EXHAUST);
        int normalAmplifier = normal == null ? -1 : normal.getAmplifier();
        int infectiousAmplifier = infectious == null ? -1 : infectious.getAmplifier();
        return (Math.max(normalAmplifier, infectiousAmplifier) + 1) * 10;
    }

    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        Inventory inventory = player.getInventory();
        for (int slot = Inventory.INVENTORY_SIZE - 1; slot >= 0; slot--) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof WandItem wand && wand.consumeAllVis(stack, player, cost, true, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        return isOnCooldown(entity, activeWand(entity));
    }

    public static boolean isOnCooldown(LivingEntity entity, ItemStack wand) {
        return getCooldown(entity, wand) > 0.0F;
    }

    public static float getCooldown(LivingEntity entity) {
        return getCooldown(entity, activeWand(entity));
    }

    public static float getCooldown(LivingEntity entity, ItemStack wand) {
        FocusItem focus = Wands.getFocus(wand);
        Map<LivingEntity, Map<FocusItem, Long>> cooldowns = entity.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER;
        Map<FocusItem, Long> entries = cooldowns.get(entity);
        if (focus == null || entries == null) {
            return 0.0F;
        }
        Long until = entries.get(focus);
        long remaining = until == null ? 0L : until - Util.getMillis();
        if (remaining <= 0L) {
            entries.remove(focus);
            if (entries.isEmpty()) {
                cooldowns.remove(entity);
            }
            return 0.0F;
        }
        return remaining / 1000.0F;
    }

    public static void setCooldown(LivingEntity entity, int milliseconds) {
        setCooldown(entity, activeWand(entity), milliseconds);
    }

    public static void setCooldown(LivingEntity entity, ItemStack wand, int milliseconds) {
        FocusItem focus = Wands.getFocus(wand);
        if (focus == null) {
            return;
        }
        Map<LivingEntity, Map<FocusItem, Long>> cooldowns = entity.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER;
        if (milliseconds <= 0) {
            Map<FocusItem, Long> entries = cooldowns.get(entity);
            if (entries != null) {
                entries.remove(focus);
                if (entries.isEmpty()) {
                    cooldowns.remove(entity);
                }
            }
        } else {
            cooldowns.computeIfAbsent(entity, ignored -> new HashMap<>()).put(focus, Util.getMillis() + milliseconds);
        }
    }

    private static ItemStack activeWand(LivingEntity entity) {
        if (entity.isUsingItem() && Wands.isWand(entity.getUseItem())) {
            return entity.getUseItem();
        }
        return Wands.isWand(entity.getMainHandItem()) ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    public static void changeFocus(ItemStack wand, Level level, Player player, String focusKey) {
        if (!Wands.isWand(wand)) {
            return;
        }
        TreeMap<String, Integer> foci = new TreeMap<>();
        Map<Integer, Integer> pouches = new TreeMap<>();
        int pouchCount = 0;
        for (int slot = 0; slot < Baubles.slotCount(); slot++) {
            ItemStack bauble = Baubles.get(player, slot);
            if (bauble.getItem() instanceof FocusPouchItem) {
                pouchCount++;
                pouches.put(pouchCount, slot - Baubles.slotCount());
                collectPouchFoci(foci, bauble, pouchCount);
            }
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof FocusItem focusItem) {
                foci.put(focusItem.getSortingHelper(stack), slot);
            }
            if (stack.getItem() instanceof FocusPouchItem) {
                pouchCount++;
                pouches.put(pouchCount, slot);
                collectPouchFoci(foci, stack, pouchCount);
            }
        }
        boolean hasFocus = Wands.getFocus(wand) != null;
        if (!focusKey.equals(REMOVE_FOCUS) && !foci.isEmpty()) {
            String newKey = focusKey;
            if (foci.get(newKey) == null) {
                newKey = foci.higherKey(newKey);
            }
            if (newKey == null || foci.get(newKey) == null) {
                newKey = foci.firstKey();
            }
            int location = foci.get(newKey);
            ItemStack selected = ItemStack.EMPTY;
            if (location < POUCH_STRIDE) {
                selected = inventory.getItem(location).copy();
            } else {
                int pouchId = location / POUCH_STRIDE;
                Integer pouchSlot = pouches.get(pouchId);
                if (pouchSlot != null) {
                    selected = fetchFocusFromPouch(player, location - pouchId * POUCH_STRIDE, getPouchStack(player, pouchSlot), pouchSlot);
                }
            }
            if (selected.isEmpty()) {
                return;
            }
            if (location < POUCH_STRIDE) {
                inventory.setItem(location, ItemStack.EMPTY);
            }
            playFocusSound(level, player, 1.0F);
            if (hasFocus) {
                ItemStack old = Wands.getFocusItem(wand).copy();
                if (addFocusToPouch(player, old, pouches) || inventory.add(old)) {
                    Wands.setFocus(wand, ItemStack.EMPTY);
                }
            }
            if (Wands.getFocus(wand) == null) {
                Wands.setFocus(wand, selected);
            } else if (!addFocusToPouch(player, selected, pouches)) {
                inventory.add(selected);
            }
        } else if (hasFocus) {
            ItemStack old = Wands.getFocusItem(wand).copy();
            if (addFocusToPouch(player, old, pouches) || inventory.add(old)) {
                Wands.setFocus(wand, ItemStack.EMPTY);
                playFocusSound(level, player, 0.9F);
            }
        }
    }

    private static void playFocusSound(Level level, Player player, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CAMERATICKS.value(), SoundSource.PLAYERS, 0.3F, pitch);
    }

    private static void collectPouchFoci(Map<String, Integer> foci, ItemStack pouch, int pouchId) {
        List<ItemStack> contents = FocusPouchItem.getInventory(pouch);
        for (int index = 0; index < contents.size(); index++) {
            ItemStack stack = contents.get(index);
            if (stack.getItem() instanceof FocusItem focusItem) {
                foci.put(focusItem.getSortingHelper(stack), index + pouchId * POUCH_STRIDE);
            }
        }
    }

    private static ItemStack getPouchStack(Player player, int pouchSlot) {
        return pouchSlot >= 0 ? player.getInventory().getItem(pouchSlot) : Baubles.get(player, pouchSlot + Baubles.slotCount());
    }

    private static void storePouchStack(Player player, int pouchSlot, ItemStack pouch) {
        if (pouchSlot >= 0) {
            player.getInventory().setItem(pouchSlot, pouch);
            player.getInventory().setChanged();
        } else {
            Baubles.set(player, pouchSlot + Baubles.slotCount(), pouch);
        }
    }

    private static ItemStack fetchFocusFromPouch(Player player, int focusSlot, ItemStack pouch, int pouchSlot) {
        List<ItemStack> contents = FocusPouchItem.getInventory(pouch);
        ItemStack stack = contents.get(focusSlot);
        if (!(stack.getItem() instanceof FocusItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack focus = stack.copy();
        contents.set(focusSlot, ItemStack.EMPTY);
        FocusPouchItem.setInventory(pouch, contents);
        storePouchStack(player, pouchSlot, pouch);
        return focus;
    }

    private static boolean addFocusToPouch(Player player, ItemStack focus, Map<Integer, Integer> pouches) {
        for (int pouchSlot : pouches.values()) {
            ItemStack pouch = getPouchStack(player, pouchSlot);
            if (!(pouch.getItem() instanceof FocusPouchItem)) {
                continue;
            }
            List<ItemStack> contents = FocusPouchItem.getInventory(pouch);
            for (int index = 0; index < contents.size(); index++) {
                if (contents.get(index).isEmpty()) {
                    contents.set(index, focus.copy());
                    FocusPouchItem.setInventory(pouch, contents);
                    storePouchStack(player, pouchSlot, pouch);
                    return true;
                }
            }
        }
        return false;
    }

    public static void toggleMisc(ItemStack wand, Level level, Player player) {
        toggleMisc(wand, level, player, player.isShiftKeyDown());
    }

    public static void toggleMisc(ItemStack wand, Level level, Player player, boolean sneak) {
        if (!Wands.isWand(wand)) {
            return;
        }
        FocusItem focus = Wands.getFocus(wand);
        if (!(focus instanceof thaumcraft.wand.focus.Architect) || !focus.isUpgradedWith(Wands.getFocusItem(wand), FocusUpgradeType.ARCHITECT)) {
            return;
        }
        int dim = getAreaDim(wand);
        if (sneak) {
            dim++;
            if (dim > (focus instanceof TradeFocus ? 2 : 3)) {
                dim = 0;
            }
            setAreaDim(wand, dim);
            return;
        }
        int areaX = getAreaX(wand);
        int areaY = getAreaY(wand);
        int areaZ = getAreaZ(wand);
        if (dim == 0) {
            areaX++;
            areaZ++;
            areaY++;
        } else if (dim == 1) {
            areaX++;
        } else if (dim == 2) {
            areaZ++;
        } else if (dim == 3) {
            areaY++;
        }
        int max = focus.getMaxAreaSize(Wands.getFocusItem(wand));
        if (areaX > max) {
            areaX = 0;
        }
        if (areaZ > max) {
            areaZ = 0;
        }
        if (areaY > max) {
            areaY = 0;
        }
        setAreaX(wand, areaX);
        setAreaY(wand, areaY);
        setAreaZ(wand, areaZ);
    }

    private static WandArea area(ItemStack wand) {
        WandArea area = WandKeys.AREA.get(wand);
        return area == null ? new WandArea(UNSET_AREA, UNSET_AREA, UNSET_AREA, 0) : area;
    }

    private static int clampArea(ItemStack wand, int value) {
        FocusItem focus = Wands.getFocus(wand);
        int max = focus == null ? 0 : focus.getMaxAreaSize(Wands.getFocusItem(wand));
        return value < 0 || value > max ? max : value;
    }

    public static int getAreaDim(ItemStack wand) {
        return area(wand).dim();
    }

    public static int getAreaX(ItemStack wand) {
        return clampArea(wand, area(wand).x());
    }

    public static int getAreaY(ItemStack wand) {
        return clampArea(wand, area(wand).y());
    }

    public static int getAreaZ(ItemStack wand) {
        return clampArea(wand, area(wand).z());
    }

    public static void setAreaX(ItemStack wand, int value) {
        WandArea area = area(wand);
        WandKeys.AREA.set(wand, new WandArea(value, area.y(), area.z(), area.dim()));
    }

    public static void setAreaY(ItemStack wand, int value) {
        WandArea area = area(wand);
        WandKeys.AREA.set(wand, new WandArea(area.x(), value, area.z(), area.dim()));
    }

    public static void setAreaZ(ItemStack wand, int value) {
        WandArea area = area(wand);
        WandKeys.AREA.set(wand, new WandArea(area.x(), area.y(), value, area.dim()));
    }

    public static void setAreaDim(ItemStack wand, int dim) {
        WandArea area = area(wand);
        WandKeys.AREA.set(wand, new WandArea(area.x(), area.y(), area.z(), dim));
    }
}
