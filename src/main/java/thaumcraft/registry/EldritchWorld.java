package thaumcraft.registry;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.world.dim.OuterLandsChunkGenerator;

public final class EldritchWorld {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS = DeferredRegister.create(
        Registries.CHUNK_GENERATOR,
        Thaumcraft.MODID
    );
    public static final Supplier<MapCodec<? extends ChunkGenerator>> OUTER_LANDS_GENERATOR = GENERATORS.register(
        "outer_lands",
        () -> OuterLandsChunkGenerator.CODEC
    );

    private EldritchWorld() {
    }

    public static void register(IEventBus bus) {
        GENERATORS.register(bus);
    }
}
