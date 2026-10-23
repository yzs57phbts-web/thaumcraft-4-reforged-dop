package thaumcraft.client.render;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModFluids;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class FluidRendering {
    private FluidRendering() {
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        ResourceLocation purifying = Thaumcraft.id("block/fluidpure");
        ResourceLocation death = Thaumcraft.id("block/fluiddeath");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return purifying;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return purifying;
            }
        }, ModFluids.PURIFYING_FLUID_TYPE);
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return death;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return death;
            }
        }, ModFluids.LIQUID_DEATH_TYPE);
    }
}
