package thaumcraft.research;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class HexGrid {
    private static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final int MAX_BLANK_ATTEMPTS = 10000;

    private HexGrid() {
    }

    public record Cell(int q, int r) {
        public Cell neighbour(int direction) {
            int[] offset = NEIGHBOURS[direction];
            return new Cell(q + offset[0], r + offset[1]);
        }

        public double pixelX(int size) {
            return size * 1.5 * q;
        }

        public double pixelY(int size) {
            return size * Math.sqrt(3.0) * (r + q / 2.0);
        }
    }

    public record Links(List<Cell[]> lines, Set<Cell> highlight) {
    }

    public static int distance(Cell first, Cell second) {
        return (Math.abs(first.q - second.q) + Math.abs(first.r - second.r) + Math.abs(first.q + first.r - second.q - second.r)) / 2;
    }

    public static Cell fromPixel(double x, double y, int size) {
        double q = 0.6666666666666666 * x / size;
        double r = (0.3333333333333333 * Math.sqrt(3.0) * -y - 0.3333333333333333 * x) / size;
        return roundedCell(q, r);
    }

    private static Cell roundedCell(double qq, double rr) {
        double xx = qq;
        double yy = rr;
        double zz = -qq - rr;
        int roundedX = (int) Math.round(xx);
        int roundedY = (int) Math.round(yy);
        int roundedZ = (int) Math.round(zz);
        double diffX = Math.abs(roundedX - xx);
        double diffY = Math.abs(roundedY - yy);
        double diffZ = Math.abs(roundedZ - zz);
        if (diffX > diffY && diffX > diffZ) {
            roundedX = -roundedY - roundedZ;
        } else if (diffY > diffZ) {
            roundedY = -roundedX - roundedZ;
        } else {
            roundedZ = -roundedX - roundedY;
        }
        return new Cell(roundedX, roundedZ);
    }

    public static List<Cell> ring(int radius) {
        Cell cell = new Cell(0, 0);
        for (int step = 0; step < radius; step++) {
            cell = cell.neighbour(4);
        }
        List<Cell> ring = new ArrayList<>();
        for (int direction = 0; direction < 6; direction++) {
            for (int step = 0; step < radius; step++) {
                ring.add(cell);
                cell = cell.neighbour(direction);
            }
        }
        return ring;
    }

    public static List<Cell> distributeRing(int radius, int entries) {
        List<Cell> ring = ring(radius);
        List<Cell> results = new ArrayList<>();
        float spacing = (float) ring.size() / entries;
        float position = 0.0F;
        for (int index = 0; index < entries; index++) {
            results.add(ring.get(Math.min(ring.size() - 1, Math.round(position))));
            position += spacing;
        }
        return results;
    }

    public static Set<Cell> generate(int radius) {
        Set<Cell> results = new LinkedHashSet<>();
        Cell cell = new Cell(0, 0);
        results.add(cell);
        for (int ringIndex = 0; ringIndex < radius; ringIndex++) {
            cell = cell.neighbour(4);
            Cell walker = cell;
            for (int direction = 0; direction < 6; direction++) {
                for (int step = 0; step <= ringIndex; step++) {
                    results.add(walker);
                    walker = walker.neighbour(direction);
                }
            }
        }
        return results;
    }

    public static Map<Cell, ResearchNote.Hex> index(List<ResearchNote.Hex> hexes) {
        Map<Cell, ResearchNote.Hex> map = new LinkedHashMap<>();
        for (ResearchNote.Hex hex : hexes) {
            map.put(new Cell(hex.q(), hex.r()), hex);
        }
        return map;
    }

    public static List<ResearchNote.Hex> generateHexes(ResearchItem item, RandomSource random) {
        int complexity = item.complexity();
        int radius = 1 + Math.min(3, complexity);
        Map<Cell, ResearchNote.Hex> hexes = new LinkedHashMap<>();
        for (Cell cell : generate(radius)) {
            hexes.put(cell, new ResearchNote.Hex(cell.q(), cell.r(), ResearchNote.Hex.EMPTY, Optional.empty()));
        }
        List<Aspect> tags = item.tags().getAspects();
        List<Cell> outerRing = distributeRing(radius, tags.size());
        for (int index = 0; index < outerRing.size(); index++) {
            Cell cell = outerRing.get(index);
            hexes.put(cell, new ResearchNote.Hex(cell.q(), cell.r(), ResearchNote.Hex.GIVEN, Optional.of(tags.get(index))));
        }
        if (complexity > 1) {
            int blanks = complexity * 2;
            int attempts = 0;
            while (blanks > 0 && attempts++ < MAX_BLANK_ATTEMPTS) {
                List<Cell> cells = new ArrayList<>(hexes.keySet());
                Cell candidate = cells.get(random.nextInt(cells.size()));
                if (hexes.get(candidate).type() != ResearchNote.Hex.EMPTY) {
                    continue;
                }
                boolean removable = true;
                for (int direction = 0; direction < 6; direction++) {
                    Cell neighbour = candidate.neighbour(direction);
                    ResearchNote.Hex neighbourHex = hexes.get(neighbour);
                    if (neighbourHex != null && neighbourHex.type() == ResearchNote.Hex.GIVEN) {
                        int count = 0;
                        for (int inner = 0; inner < 6; inner++) {
                            if (hexes.containsKey(neighbour.neighbour(inner))) {
                                count++;
                            }
                            if (count >= 2) {
                                break;
                            }
                        }
                        if (count < 2) {
                            removable = false;
                            break;
                        }
                    }
                }
                if (removable) {
                    hexes.remove(candidate);
                    blanks--;
                }
            }
        }
        return new ArrayList<>(hexes.values());
    }

    public static boolean linked(Aspect first, Aspect second) {
        Aspect[] firstComponents = first.getComponents();
        Aspect[] secondComponents = second.getComponents();
        return !first.isPrimal() && firstComponents != null && (firstComponents[0] == second || firstComponents[1] == second)
            || !second.isPrimal() && secondComponents != null && (secondComponents[0] == first || secondComponents[1] == first);
    }

    private static boolean connects(Map<Cell, ResearchNote.Hex> hexes, Cell from, Cell to, Predicate<Aspect> discovered) {
        ResearchNote.Hex fromHex = hexes.get(from);
        ResearchNote.Hex toHex = hexes.get(to);
        if (fromHex == null || toHex == null || toHex.type() < ResearchNote.Hex.GIVEN || fromHex.aspect().isEmpty() || toHex.aspect().isEmpty()) {
            return false;
        }
        Aspect first = fromHex.aspect().get();
        Aspect second = toHex.aspect().get();
        return discovered.test(first) && discovered.test(second) && linked(first, second);
    }

    private static void flood(Map<Cell, ResearchNote.Hex> hexes, Cell cell, Set<Cell> checked, Set<Cell> main, Set<Cell> remains, Predicate<Aspect> discovered) {
        checked.add(cell);
        for (int direction = 0; direction < 6; direction++) {
            Cell target = cell.neighbour(direction);
            if (!checked.contains(target) && connects(hexes, cell, target, discovered)) {
                remains.add(target);
                if (hexes.get(target).type() == ResearchNote.Hex.GIVEN) {
                    main.remove(target);
                }
                flood(hexes, target, checked, main, remains, discovered);
            }
        }
    }

    public static ResearchNote complete(ResearchNote note, Predicate<Aspect> discovered) {
        Map<Cell, ResearchNote.Hex> hexes = index(note.hexes());
        Set<Cell> checked = new HashSet<>();
        Set<Cell> main = new LinkedHashSet<>();
        Set<Cell> remains = new HashSet<>();
        Cell start = null;
        for (Map.Entry<Cell, ResearchNote.Hex> entry : hexes.entrySet()) {
            if (entry.getValue().type() == ResearchNote.Hex.GIVEN) {
                if (start == null) {
                    start = entry.getKey();
                } else {
                    main.add(entry.getKey());
                }
            }
        }
        if (start != null) {
            flood(hexes, start, checked, main, remains, discovered);
        }
        if (!main.isEmpty()) {
            return null;
        }
        List<ResearchNote.Hex> kept = new ArrayList<>();
        for (Map.Entry<Cell, ResearchNote.Hex> entry : hexes.entrySet()) {
            if (entry.getValue().type() == ResearchNote.Hex.GIVEN || remains.contains(entry.getKey())) {
                kept.add(entry.getValue());
            }
        }
        return new ResearchNote(note.key(), note.color(), true, note.copies(), kept);
    }

    public static Links links(List<ResearchNote.Hex> hexList, Predicate<Aspect> discovered) {
        Map<Cell, ResearchNote.Hex> hexes = index(hexList);
        Set<Cell> checked = new HashSet<>();
        Set<String> seen = new HashSet<>();
        List<Cell[]> lines = new ArrayList<>();
        Set<Cell> highlight = new HashSet<>();
        for (Map.Entry<Cell, ResearchNote.Hex> entry : hexes.entrySet()) {
            ResearchNote.Hex hex = entry.getValue();
            if (hex.type() == ResearchNote.Hex.GIVEN && hex.aspect().isPresent() && discovered.test(hex.aspect().get())) {
                trace(hexes, entry.getKey(), checked, seen, lines, highlight, discovered);
            }
        }
        return new Links(lines, highlight);
    }

    private static void trace(Map<Cell, ResearchNote.Hex> hexes, Cell cell, Set<Cell> checked, Set<String> seen, List<Cell[]> lines, Set<Cell> highlight, Predicate<Aspect> discovered) {
        checked.add(cell);
        for (int direction = 0; direction < 6; direction++) {
            Cell target = cell.neighbour(direction);
            if (!checked.contains(target) && connects(hexes, cell, target, discovered)) {
                String forward = cell.q() + ":" + cell.r() + ":" + target.q() + ":" + target.r();
                String backward = target.q() + ":" + target.r() + ":" + cell.q() + ":" + cell.r();
                if (!seen.contains(forward) && !seen.contains(backward)) {
                    seen.add(forward);
                    lines.add(new Cell[]{cell, target});
                    highlight.add(target);
                }
                trace(hexes, target, checked, seen, lines, highlight, discovered);
            }
        }
    }

    public static AspectList requiredTags(ResearchItem item, int copies) {
        AspectList cost = item.tags().copy();
        for (Aspect aspect : cost.getAspects()) {
            cost.add(aspect, copies);
        }
        return cost;
    }
}
