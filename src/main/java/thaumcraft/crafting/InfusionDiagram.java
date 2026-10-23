package thaumcraft.crafting;
import java.util.*;
/** Hand-authored, symmetric recipe layouts shared by Thaumonomicon and JEI. */
public final class InfusionDiagram {
    public static List<int[]> coordinates(String key,int count) {
        int[][] manual=switch(key){
        case "HelmGoggles" -> new int[][]{{40,0},{0,-40},{-40,0}};
        case "WandPedFocus" -> new int[][]{{-40,0},{-28,-28},{40,0},{28,-28},{0,-40},{-28,28},{0,40},{28,28}};
        case "FocusHellbat" -> new int[][]{{-28,-28},{40,0},{28,-28},{0,-40},{0,40},{-40,0},{-28,28},{28,28}};

        case "RunicAmulet" -> new int[][]{{0,-40},{28,-28},{28,28},{-28,28},{-28,-28},{0,40},{-40,0},{40,0}};

        case "RunicGirdle" -> new int[][]{{0,-40},{28,-28},{28,28},{-28,28},{-40,0},{40,0},{-28,-28},{0,40}};
        case "ElementalAxe" -> new int[][]{{-40,0},{40,0},{0,40},{0,-40}};
        case "ElementalSword" -> new int[][]{{-40,0},{40,0},{0,40},{0,-40}};
        case "ElementalHoe" -> new int[][]{{-40,0},{40,0},{0,40},{0,-40}};
        case "ElementalShovel" -> new int[][]{{-40,0},{40,0},{0,40},{0,-40},{-28,-28},{28,-28},{-28,28},{28,28}};
        case "ElementalPick" -> new int[][]{{-40,0},{40,0},{0,40},{0,-40},{-28,-28},{28,-28},{-28,28},{28,28}};
        case "BootsTraveller" -> new int[][]{{-28,-28},{28,-28},{-28,28},{28,28},{0,40},{0,-40},{-40,0},{40,0}};

        case "MaskGrinningDevil" -> new int[][]{{0,-40},{-40,0},{-28,28},{28,28},{0,40},{40,0},{-28,-28},{28,-28}};




        case "VoidRobeHelm" -> new int[][]{{0,-40},{-28,-28},{28,-28},{0,40},{-28,28},{28,28},{-40,0},{40,0}};

        case "VoidRobeLegs" -> new int[][]{{0,-40},{-28,-28},{28,-28},{0,40},{-28,28},{28,28},{-40,0},{40,0}};
        default -> null;
        };
        if(key.startsWith(InfusionRunicAugmentRecipe.DISPLAY_PREFIX)){
            int side=Math.max(1,(int)Math.round(count/4.0));
            var order=new ArrayList<Integer>();order.add(side);order.add(count-side);order.add(0);
            for(int i=1;i<count;i++)if(i!=side&&i!=count-side)order.add(i);
            return polygon(count,order.stream().mapToInt(Integer::intValue).toArray());
        }
        int[] selected=switch(key){
            case "ThaumiumFortressLegs" -> new int[]{5,1,3,4,2,0};
            case "ThaumiumFortressChest" -> new int[]{6,1,5,2,4,3,0};
            case "ThaumiumFortressHelm" -> new int[]{2,3,1,4,0};
            case "MaskAngryGhost" -> new int[]{6,5,1,4,0,2,3};
            case "MaskSippingFiend" -> new int[]{6,5,1,0,4,2,3};
            case "VoidRobeChest" -> new int[]{0,7,1,4,5,3,6,2};
            case "EldritchEye" -> new int[]{0,3,4,2,5,1};
            case "SanityCheck" -> new int[]{0,5,1,3,4,2};
            case "ArcaneBore" -> new int[]{8,1,6,3,7,2,5,4,0};
            case "LampGrowth" -> new int[]{0,4,5,3,2,1};
            case "LampFertility" -> new int[]{0,4,5,3,2,1};
            case "JarBrain" -> new int[]{0,5,3,1,2,4};
            case "RunicRing" -> new int[]{0,1,2,4,5,3};
            case "HoverHarness" -> new int[]{7,1,0,3,5,6,2,4};
            default -> null;
        };
        if(selected!=null)return polygon(count,selected);
        if(manual!=null){if(manual.length!=count)throw new IllegalArgumentException("Infusion diagram ingredient count: "+key+" "+count);return Arrays.asList(manual);}
        var points=new ArrayList<int[]>();for(int i=0;i<count;i++){double a=-Math.PI/2+2*Math.PI*i/count;points.add(new int[]{(int)Math.round(Math.cos(a)*40),(int)Math.round(Math.sin(a)*40)});}return points;
    }
    private static List<int[]> polygon(int count,int[] order){
        if(order.length!=count)throw new IllegalArgumentException("Infusion diagram count mismatch");
        var points=new ArrayList<int[]>();var used=new HashSet<Integer>();
        for(int slot:order){
            if(slot<0||slot>=count||!used.add(slot))throw new IllegalArgumentException("Invalid infusion ring slot");
            double angle=-Math.PI/2+2*Math.PI*slot/count;
            points.add(new int[]{(int)Math.round(Math.cos(angle)*40),(int)Math.round(Math.sin(angle)*40)});
        }
        return points;
    }

}
