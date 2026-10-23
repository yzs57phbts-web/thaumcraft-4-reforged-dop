package thaumcraft.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class StackData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Thaumcraft.MODID);

    private StackData() {
    }

    public static <T> StackKey<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new StackKey<>(COMPONENTS.registerComponentType(name, builder -> builder.persistent(codec).networkSynchronized(streamCodec)));
    }

    public static <T> StackKey<T> registerTransient(String name) {
        return new StackKey<>(COMPONENTS.<T>registerComponentType(name, builder -> builder));
    }

    static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
