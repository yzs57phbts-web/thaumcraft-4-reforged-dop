package thaumcraft.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class InteractionResults {
    private InteractionResults() {
    }

    public static InteractionResultHolder<ItemStack> holder(InteractionResult result, ItemStack stack, Level level) {
        if (result == InteractionResult.SUCCESS) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (result == InteractionResult.CONSUME) {
            return InteractionResultHolder.consume(stack);
        }
        if (result == InteractionResult.FAIL) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
