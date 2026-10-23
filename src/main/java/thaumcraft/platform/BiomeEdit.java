package thaumcraft.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class BiomeEdit {
    private static final Map<ServerLevel, Map<ChunkPos, ChunkAccess>> DIRTY = new HashMap<>();

    private BiomeEdit() {
    }

    public static boolean setColumn(ServerLevel level, BlockPos pos, Holder<Biome> biome) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        }
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartZ = QuartPos.fromBlock(pos.getZ());
        boolean[] changed = {false};
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> {
            Holder<Biome> current = chunk.getNoiseBiome(x, y, z);
            if (x == quartX && z == quartZ && !current.is(biome)) {
                changed[0] = true;
                return biome;
            }
            return current;
        }, Climate.empty());
        if (!changed[0]) {
            return false;
        }
        chunk.setUnsaved(true);
        DIRTY.computeIfAbsent(level, key -> new LinkedHashMap<>()).put(chunk.getPos(), chunk);
        return true;
    }

    public static Holder<Biome> getColumnBiome(LevelReader level, BlockPos pos) {
        return level.getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()));
    }

    public static boolean restoreColumn(
        ServerLevel level,
        BlockPos pos,
        Holder<Biome> taintedBiome,
        Holder<Biome> taintFallback,
        Holder<Biome>... biomes
    ) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        }
        BiomeResolver resolver = level.getChunkSource().getGenerator().getBiomeSource();
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartZ = QuartPos.fromBlock(pos.getZ());
        boolean[] changed = {false};
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> {
            Holder<Biome> current = chunk.getNoiseBiome(x, y, z);
            if (x != quartX || z != quartZ) {
                return current;
            }
            boolean restore = false;
            for (Holder<Biome> biome : biomes) {
                if (current.is(biome)) {
                    restore = true;
                    break;
                }
            }
            if (!restore) {
                return current;
            }
            Holder<Biome> original = resolver.getNoiseBiome(x, y, z, level.getChunkSource().randomState().sampler());
            if (original.is(taintedBiome)) {
                original = taintFallback;
            }
            if (!current.equals(original)) {
                changed[0] = true;
            }
            return original;
        }, Climate.empty());
        if (!changed[0]) {
            return false;
        }
        chunk.setUnsaved(true);
        DIRTY.computeIfAbsent(level, key -> new LinkedHashMap<>()).put(chunk.getPos(), chunk);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (DIRTY.isEmpty()) {
            return;
        }
        Map<ServerLevel, Map<ChunkPos, ChunkAccess>> pending = new HashMap<>(DIRTY);
        DIRTY.clear();
        pending.forEach((level, chunks) -> level.getChunkSource().chunkMap.resendBiomesForChunks(new ArrayList<>(chunks.values())));
    }
}
