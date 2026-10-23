package thaumcraft.modern;

import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/** Integration placeholders remain registered for saves, but never advertise empty recipes. */
public final class MetalIntegration {
    public static boolean has(String tag) {
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM,ResourceLocation.parse("c:"+tag)))
            .map(s->s.size()>0).orElse(false);
    }
    public static String metal(String key) {
        if(key.contains("tin")||key.endsWith("TIN"))return "tin";
        if(key.contains("silver")||key.endsWith("SILVER"))return "silver";
        if(key.contains("lead")||key.endsWith("LEAD"))return "lead";
        return null;
    }
    public static boolean researchAvailable(String key) {
        String metal=metal(key);if(metal==null)return true;
        if(key.startsWith("PURE"))return has("raw_materials/"+metal)||has("ores/"+metal);
        if(key.startsWith("TRANS"))return has("ingots/"+metal);
        if(key.equals("CAP_silver"))return has("ingots/silver");
        return true;
    }
    public static boolean itemAvailable(String path) {
        String metal=metal(path);
        if(metal==null)return true;
        if(path.startsWith("native_"))return has("raw_materials/"+metal)||has("ores/"+metal);
        if(path.endsWith("_nugget")||path.endsWith("_wand_cap"))return has("ingots/"+metal);
        return true;
    }
}
