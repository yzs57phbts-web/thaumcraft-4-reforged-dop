package thaumcraft.client.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemModelResolver {
    public void updateForLiving(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context, LivingEntity entity) {
        updateForTopItem(state, stack, context, entity.level(), entity, entity.getId());
    }

    public void updateForTopItem(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context, Level level, LivingEntity entity, int seed) {
        state.stack = stack.copy();
        state.context = context;
        state.level = level;
        state.seed = seed;
    }
}
