package thaumcraft.world.dim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public final class MazeRooms {
    private MazeRooms() {
    }

    public static void generateCell(ServerLevel level, RandomSource random, int chunkX, int chunkZ, Cell cell) {
        GenCommon generator = switch (cell.feature) {
            case 1 -> new GenPortal();
            case 2, 3, 4, 5 -> new GenBossRoom();
            case 6 -> new GenKeyRoom();
            case 7 -> new GenNestRoom();
            case 8 -> new GenLibraryRoom();
            default -> new GenPassage();
        };
        int y = 50;
        switch (cell.feature) {
            case 1:
                ((GenPortal) generator).generatePortal(level, random, chunkX, chunkZ, y, cell);
                break;
            case 2, 3, 4, 5:
                ((GenBossRoom) generator).generateRoom(level, random, chunkX, chunkZ, y, cell);
                break;
            case 6:
                ((GenKeyRoom) generator).generateRoom(level, random, chunkX, chunkZ, y, cell);
                break;
            case 7:
                ((GenNestRoom) generator).generateRoom(level, random, chunkX, chunkZ, y, cell);
                break;
            case 8:
                ((GenLibraryRoom) generator).generateRoom(level, random, chunkX, chunkZ, y, cell);
                break;
            default:
                ((GenPassage) generator).generateDefaultPassage(level, random, chunkX, chunkZ, y, cell);
                break;
        }
        generator.processDecorations(level);
    }
}
