package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModVillagers {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Thaumcraft.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD = profession("wizard");
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BANKER = profession("banker");

    private ModVillagers() {
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> profession(String name) {
        return PROFESSIONS.register(name, () -> new VillagerProfession(
            name,
            holder -> false,
            holder -> false,
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN
        ));
    }

    public static void register(IEventBus bus) {
        PROFESSIONS.register(bus);
    }
}
