package thaumcraft.item.golem;

import net.minecraft.world.item.Item;

public class GolemPartItem extends Item {
    public enum Kind { CORE, UPGRADE, DECORATION }
    private final Kind kind;
    private final int index;
    public GolemPartItem(Properties properties, Kind kind, int index) { super(properties); this.kind = kind; this.index = index; }
    @Override
    public net.minecraft.network.chat.Component getName(net.minecraft.world.item.ItemStack stack) {
        if (kind == Kind.CORE) return net.minecraft.network.chat.Component.translatable("item.thaumcraft.golem_core").append(": ").append(super.getName(stack));
        return super.getName(stack);
    }
    public Kind kind() { return kind; }
    public int index() { return index; }
    public String decoration() { return String.valueOf("HGBFRVPM".charAt(index)); }
}
