package thaumcraft.item.tool;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.StackKey;
import thaumcraft.platform.DataKey;

public final class ToolKeys {
    public static final StackKey<Integer> ORIENTATION = StackKey.register("tool_orientation", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final DataKey<Boolean> FOLLOWING = DataKey.builder("following_item", () -> false)
        .syncToAll(ByteBufCodecs.BOOL)
        .build();

    private ToolKeys() {
    }

    public static void init() {
    }

    public static int getOrientation(ItemStack stack) {
        return ORIENTATION.getOrDefault(stack, 0);
    }

    public static void setOrientation(ItemStack stack, int orientation) {
        ORIENTATION.set(stack, Math.floorMod(orientation, 3));
    }
}
