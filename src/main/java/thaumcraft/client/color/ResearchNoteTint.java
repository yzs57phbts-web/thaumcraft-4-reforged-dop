package thaumcraft.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import thaumcraft.research.ResearchNote;
import thaumcraft.research.ResearchNoteKeys;

public final class ResearchNoteTint implements ItemColor {
    public static final ResearchNoteTint INSTANCE = new ResearchNoteTint();
    private static final int DEFAULT_COLOR = 10066329;

    private ResearchNoteTint() {
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        ResearchNote note = ResearchNoteKeys.NOTE.get(stack);
        return FastColor.ARGB32.opaque(note == null ? DEFAULT_COLOR : note.color());
    }
}
