package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemStackRenderState {
    ItemStack stack = ItemStack.EMPTY;
    ItemDisplayContext context = ItemDisplayContext.NONE;
    Level level;
    int seed;

    public void clear() {
        stack = ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean usesBlockLight() {
        return Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, seed).usesBlockLight();
    }

    public void submit(PoseStack poses, ImmediateGeometry geometry, int light, int overlay, int outline) {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light, overlay, poses, geometry.buffers, level, seed);
    }
}
