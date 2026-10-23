package thaumcraft.world.village;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class VillageStructures {
    private static final Map<String, Integer> HOUSES = Map.of("wizard_tower", 1, "banker_home", 1);
    private static final Map<String, String> BIOMES = Map.of(
        "plains", "mossify_10_percent",
        "taiga", "mossify_10_percent",
        "desert", "empty",
        "savanna", "empty",
        "snowy", "empty"
    );

    private VillageStructures() {
    }

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        BIOMES.forEach((biome, processorName) -> {
            StructureTemplatePool pool = pools.get(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace("village/" + biome + "/houses")));
            Holder<StructureProcessorList> processors = processorLists.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace(processorName)));
            HOUSES.forEach((house, weight) -> inject(pool, Thaumcraft.MODID + ":village/" + biome + "/" + house, processors, weight));
        });
    }

    private static void inject(StructureTemplatePool pool, String location, Holder<StructureProcessorList> processors, int weight) {
        for (Pair<StructurePoolElement, Integer> entry : pool.rawTemplates) {
            if (entry.getFirst() instanceof LegacySinglePoolElement single && single.toString().contains(location)) {
                return;
            }
        }
        StructurePoolElement element = StructurePoolElement.legacy(location, processors).apply(StructureTemplatePool.Projection.RIGID);
        List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(pool.rawTemplates);
        raw.add(Pair.of(element, weight));
        pool.rawTemplates = raw;
        for (int index = 0; index < weight; index++) {
            pool.templates.add(element);
        }
    }
}
