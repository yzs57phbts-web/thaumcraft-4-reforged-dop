package thaumcraft.item.golem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

public final class GolemItems {
    public static final String[] CORES = {"fill", "empty", "gather", "harvest", "guard", "decanting", "alchemy", "chop", "use", "butcher", "sorting", "fishing"};
    public static final String[] UPGRADES = {"air", "earth", "fire", "water", "order", "entropy"};
    public static final String[] DECORATIONS = {"top_hat", "spectacles", "bowtie", "fez", "dart_launcher", "visor", "iron_plating", "mace_arm"};
    private GolemItems() { }
    public static ItemStack core(int index) { return stack("golem_core_" + CORES[index]); }
    public static ItemStack upgrade(int index) { return stack("golem_upgrade_" + UPGRADES[index]); }
    private static ItemStack stack(String id) { return new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id(id))); }
}
