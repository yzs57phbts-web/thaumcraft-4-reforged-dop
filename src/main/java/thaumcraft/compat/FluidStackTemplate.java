package thaumcraft.compat;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidStackTemplate {
    public static final Codec<FluidStackTemplate> CODEC = FluidStack.CODEC.xmap(FluidStackTemplate::fromNonEmptyStack, FluidStackTemplate::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = FluidStack.STREAM_CODEC.map(FluidStackTemplate::fromNonEmptyStack, FluidStackTemplate::create);
    private final FluidStack stack;
    private FluidStackTemplate(FluidStack stack) {
        this.stack = stack.copy();
    }
    public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
        return new FluidStackTemplate(stack);
    }
    public FluidStack create() {
        return stack.copy();
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof FluidStackTemplate template && FluidStack.matches(stack, template.stack);
    }
    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack) * 31 + stack.getAmount();
    }
}
