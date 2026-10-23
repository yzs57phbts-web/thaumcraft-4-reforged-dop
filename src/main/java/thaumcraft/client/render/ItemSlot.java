package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public final class ItemSlot {
    private ItemStack stack = ItemStack.EMPTY;
    private ItemDisplayContext context = ItemDisplayContext.NONE;
    private int seed;
    @Nullable
    private Level level;

    public void clear() {
        stack = ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void set(ItemStack stack, ItemDisplayContext context, @Nullable Level level, int seed) {
        this.stack = stack;
        this.context = context;
        this.level = level;
        this.seed = seed;
    }

    public void submit(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!stack.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light, overlay, poseStack, buffers, level, seed);
        }
    }
}
