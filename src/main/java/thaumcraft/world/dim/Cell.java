package thaumcraft.world.dim;

public final class Cell {
    public boolean north;
    public boolean south;
    public boolean east;
    public boolean west;
    public boolean above;
    public boolean below;
    public byte feature;

    public Cell() {
    }

    public Cell(short data) {
        north = (data & 1) != 0;
        south = (data & 2) != 0;
        east = (data & 4) != 0;
        west = (data & 8) != 0;
        above = (data & 16) != 0;
        below = (data & 32) != 0;
        feature = (byte) (data >> 8);
    }

    public short pack() {
        int data = (north ? 1 : 0) | (south ? 2 : 0) | (east ? 4 : 0) | (west ? 8 : 0);
        data |= (above ? 16 : 0) | (below ? 32 : 0) | (feature << 8);
        return (short) data;
    }
}
