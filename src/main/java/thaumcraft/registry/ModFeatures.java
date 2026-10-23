package thaumcraft.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.world.biome.MagicalForestDecorationFeature;
import thaumcraft.world.biome.TaintDecorationFeature;
import thaumcraft.world.gen.ThaumcraftWorldGenFeature;

public final class ModFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Thaumcraft.MODID);

    public static final Supplier<ThaumcraftWorldGenFeature> WORLD_GENERATION = FEATURES.register("world_generation", ThaumcraftWorldGenFeature::new);
    public static final Supplier<MagicalForestDecorationFeature> MAGICAL_FOREST_DECORATION = FEATURES.register("magical_forest_decoration", MagicalForestDecorationFeature::new);
    public static final Supplier<Feature<?>> TAINT_DECORATION = FEATURES.register("taint_decoration", TaintDecorationFeature::new);

    private ModFeatures() {
    }

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
