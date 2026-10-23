package thaumcraft.aspect;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.network.AspectTagsPayload;
import thaumcraft.platform.ModNetwork;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AspectSync {
    private static final List<Consumer<AspectRegistry>> REGISTRARS = new CopyOnWriteArrayList<>();

    private AspectSync() {
    }

    public static void addRegistrar(Consumer<AspectRegistry> registrar) {
        REGISTRARS.add(registrar);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        rebuild(event.getServer());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            rebuild(event.getPlayerList().getServer());
        }
        AspectRegistry registry = AspectRegistry.active();
        AspectTagsPayload payload = new AspectTagsPayload(registry.exportTags(), registry.exportEntities());
        event.getRelevantPlayers().forEach(player -> ModNetwork.sendToPlayer(player, payload));
    }

    public static void rebuild(MinecraftServer server) {
        long start = System.currentTimeMillis();
        AspectRegistry registry = new AspectRegistry(server);
        ModernAspects modern = new ModernAspects(registry);
        modern.registerBase();
        new ConfigAspects(registry).registerAll();
        REGISTRARS.forEach(registrar -> registrar.accept(registry));
        modern.registerDerived();
        int items = registry.buildAll().size();
        AspectRegistry.setActive(registry);
        Thaumcraft.LOGGER.info(
            "Aspect tags: {} items, {} entities ({} ms)",
            items,
            registry.exportEntities().size(),
            System.currentTimeMillis() - start
        );
    }
}
