package thaumcraft.world.gen;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Feature-based TC ruins must also respect registered structure footprints. */
public final class StructurePlacementGuard {
    private StructurePlacementGuard() {}

    public static boolean intersectsStructure(LevelAccessor level, BoundingBox footprint) {
        if (!(level instanceof WorldGenRegion region)) return false;
        var registry = region.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (int x = footprint.minX() >> 4; x <= footprint.maxX() >> 4; x++) {
            for (int z = footprint.minZ() >> 4; z <= footprint.maxZ() >> 4; z++) {
                if (!region.hasChunk(x, z)) return true;
                var chunk = region.getChunk(x, z, ChunkStatus.STRUCTURE_REFERENCES);
                for (var entry : chunk.getAllReferences().entrySet()) {
                    var id = registry.getKey(entry.getKey());
                    if (id == null || id.getNamespace().equals("thaumcraft")) continue;
                    for (long reference : entry.getValue()) {
                        var origin = new net.minecraft.world.level.ChunkPos(reference);
                        if (!region.hasChunk(origin.x, origin.z)) return true;
                        var start = region.getChunk(origin.x, origin.z, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(entry.getKey());
                        if (start != null && start.isValid() && start.getBoundingBox().intersects(footprint)) return true;
                    }
                }
            }
        }
        return false;
    }
}
