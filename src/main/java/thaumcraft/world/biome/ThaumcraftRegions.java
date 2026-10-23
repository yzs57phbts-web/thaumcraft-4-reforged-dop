package thaumcraft.world.biome;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ThaumcraftRegions {
    private static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
    private static final ResourceKey<Biome> TAINTED_LAND = key("tainted_land");

    private ThaumcraftRegions() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, name));
    }

    private static final class ReplacingRegion extends Region {
        private final ResourceKey<Biome>[] originals;
        private final ResourceKey<Biome> replacement;

        @SafeVarargs
        private ReplacingRegion(String name, int weight, ResourceKey<Biome> replacement, ResourceKey<Biome>... originals) {
            super(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, name), RegionType.OVERWORLD, weight);
            this.originals = originals;
            this.replacement = replacement;
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            addModifiedVanillaOverworldBiomes(mapper, builder -> {
                for (ResourceKey<Biome> original : originals) {
                    builder.replaceBiome(original, replacement);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Regions.register(new ReplacingRegion("magical_forest", 3, MAGICAL_FOREST, Biomes.FOREST, Biomes.BIRCH_FOREST));
            Regions.register(new ReplacingRegion("tainted_land", 1, TAINTED_LAND, Biomes.PLAINS));
        });
    }
}
