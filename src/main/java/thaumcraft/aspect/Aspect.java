package thaumcraft.aspect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import net.neoforged.fml.ModList;

public enum Aspect implements StringRepresentable {
    AIR("aer", 0xFFFF7E, "e", 1),
    EARTH("terra", 0x56C000, "2", 1),
    FIRE("ignis", 0xFF5A01, "c", 1),
    WATER("aqua", 0x3CD4FC, "3", 1),
    ORDER("ordo", 0xD5D4EC, "7", 1),
    ENTROPY("perditio", 0x404040, "8", 771),
    VOID("vacuos", 0x888888, AIR, ENTROPY, 771),
    LIGHT("lux", 0xFFF663, AIR, FIRE, 1),
    WEATHER("tempestas", 0xFFFFFF, AIR, WATER, 1),
    MOTION("motus", 0xCDCCF4, AIR, ORDER, 1),
    COLD("gelum", 0xE1FFFF, FIRE, ENTROPY, 1),
    CRYSTAL("vitreus", 0x80FFFF, EARTH, ORDER, 1),
    LIFE("victus", 0xDE0005, WATER, EARTH, 1),
    POISON("venenum", 0x89F000, WATER, ENTROPY, 1),
    ENERGY("potentia", 0xC0FFFF, ORDER, FIRE, 1),
    EXCHANGE("permutatio", 0x578357, ENTROPY, ORDER, 1),
    METAL("metallum", 0xB5B5CD, EARTH, CRYSTAL, 1),
    DEATH("mortuus", 0x887788, LIFE, ENTROPY, 1),
    FLIGHT("volatus", 0xE7E7D7, AIR, MOTION, 1),
    DARKNESS("tenebrae", 0x222222, VOID, LIGHT, 1),
    SOUL("spiritus", 0xEBEBFB, LIFE, DEATH, 1),
    HEAL("sano", 0xFF2F34, LIFE, ORDER, 1),
    TRAVEL("iter", 0xE0585B, MOTION, EARTH, 1),
    ELDRITCH("alienis", 0x805080, VOID, DARKNESS, 1),
    MAGIC("praecantatio", 0x9700C0, VOID, ENERGY, 1),
    AURA("auram", 0xFFC0FF, MAGIC, AIR, 1),
    TAINT("vitium", 0x800080, MAGIC, ENTROPY, 1),
    SLIME("limus", 0x01F800, LIFE, WATER, 1),
    PLANT("herba", 0x01AC00, LIFE, EARTH, 1),
    TREE("arbor", 0x876531, AIR, PLANT, 1),
    BEAST("bestia", 0x9F6409, MOTION, LIFE, 1),
    FLESH("corpus", 0xEE478D, DEATH, BEAST, 1),
    UNDEAD("exanimis", 0x3A4000, MOTION, DEATH, 1),
    MIND("cognitio", 0xFFC2B3, FIRE, SOUL, 1),
    SENSES("sensus", 0x0FD9FF, AIR, SOUL, 1),
    MAN("humanus", 0xFFD7C0, BEAST, MIND, 1),
    CROP("messis", 0xE1B371, PLANT, MAN, 1),
    MINE("perfodio", 0xDCD2D8, MAN, EARTH, 1),
    TOOL("instrumentum", 0x4040EE, MAN, ORDER, 1),
    HARVEST("meto", 0xEEAD82, CROP, TOOL, 1),
    WEAPON("telum", 0xC05050, TOOL, FIRE, 1),
    ARMOR("tutamen", 0x00C0C0, TOOL, EARTH, 1),
    HUNGER("fames", 0x9A0305, LIFE, VOID, 1),
    GREED("lucrum", 0xE6BE44, MAN, HUNGER, 1),
    CRAFT("fabrico", 0x809D80, MAN, TOOL, 1),
    CLOTH("pannus", 0xEAEAC2, TOOL, BEAST, 1),
    MECHANISM("machina", 0x8080A0, MOTION, TOOL, 1),
    TRAP("vinculum", 0x9A8080, MOTION, ENTROPY, 1),
    // Append addon aspects: existing ordinal-based particles/menu data retain their IDs.
    // Definitions and artwork are from Forbidden Magic's original DarkAspects registry.
    INFERNUS("infernus", 0xFF0000, FIRE, MAGIC, 771),
    IRA("ira", 0x870404, WEAPON, FIRE, 771),
    INVIDIA("invidia", 0x00BA00, SENSES, HUNGER, 1),
    GULA("gula", 0xD59C46, HUNGER, VOID, 1),
    SUPERBIA("superbia", 0x9639FF, FLIGHT, VOID, 1),
    DESIDIA("desidia", 0x6E6E6E, TRAP, SOUL, 771),
    LUXURIA("luxuria", 0xFFC1CE, FLESH, HUNGER, 1);

    private static final Map<String, Aspect> BY_TAG = new HashMap<>();
    public static final String FORBIDDEN_MAGIC_MOD_ID = "forbiddenmagic";
    public static final Codec<Aspect> CODEC = StringRepresentable.fromEnum(Aspect::values);
    public static final StreamCodec<ByteBuf, Aspect> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Aspect::byTagOrThrow, Aspect::getTag);

    static {
        for (Aspect aspect : values()) {
            BY_TAG.put(aspect.tag, aspect);
        }
    }

    private final String tag;
    private final int color;
    private final @Nullable String chatcolor;
    private final @Nullable Aspect[] components;
    private final int blend;
    private final ResourceLocation image;

    Aspect(String tag, int color, String chatcolor, int blend) {
        this(tag, color, chatcolor, null, blend);
    }

    Aspect(String tag, int color, Aspect first, Aspect second, int blend) {
        this(tag, color, null, new Aspect[]{first, second}, blend);
    }

    Aspect(String tag, int color, @Nullable String chatcolor, @Nullable Aspect[] components, int blend) {
        this.tag = tag;
        this.color = color;
        this.chatcolor = chatcolor;
        this.components = components;
        this.blend = blend;
        this.image = Thaumcraft.id("textures/aspects/" + tag + ".png");
    }

    public String getTag() {
        return tag;
    }

    public int getColor() {
        return color;
    }

    public @Nullable String getChatcolor() {
        return chatcolor;
    }

    public @Nullable Aspect[] getComponents() {
        return components;
    }

    public int getBlend() {
        return blend;
    }

    public ResourceLocation getImage() {
        return image;
    }

    public boolean isPrimal() {
        return components == null || components.length != 2;
    }

    /** Dormant definitions remain decodable so temporarily removing an addon cannot erase saves. */
    public boolean isEnabled() {
        if (ordinal() <= TRAP.ordinal()) return true;
        ModList mods = ModList.get();
        return mods != null && mods.isLoaded(FORBIDDEN_MAGIC_MOD_ID);
    }

    /** Gameplay/creative enumeration; values() is reserved for stable IDs and serialization. */
    public static Aspect[] getEnabledAspects() {
        return java.util.Arrays.stream(values()).filter(Aspect::isEnabled).toArray(Aspect[]::new);
    }

    public String getName() {
        return Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
    }

    public Component getDescription() {
        return Component.translatable("tc.aspect." + tag);
    }

    @Override
    public String getSerializedName() {
        return tag;
    }

    public static @Nullable Aspect getAspect(String tag) {
        return BY_TAG.get(tag);
    }

    private static Aspect byTagOrThrow(String tag) {
        Aspect aspect = BY_TAG.get(tag);
        if (aspect == null) {
            throw new IllegalArgumentException("Unknown aspect " + tag);
        }
        return aspect;
    }

    public static List<Aspect> getPrimalAspects() {
        List<Aspect> primals = new ArrayList<>();
        for (Aspect aspect : values()) {
            if (aspect.isEnabled() && aspect.isPrimal()) {
                primals.add(aspect);
            }
        }
        return primals;
    }

    public static List<Aspect> getCompoundAspects() {
        List<Aspect> compounds = new ArrayList<>();
        for (Aspect aspect : values()) {
            if (aspect.isEnabled() && !aspect.isPrimal()) {
                compounds.add(aspect);
            }
        }
        return compounds;
    }

    public static @Nullable Aspect combine(Aspect first, Aspect second) {
        if (!first.isEnabled() || !second.isEnabled()) return null;
        for (Aspect aspect : values()) {
            if (aspect.isEnabled() && aspect.components != null
                && (aspect.components[0] == first && aspect.components[1] == second
                    || aspect.components[0] == second && aspect.components[1] == first)) {
                return aspect;
            }
        }
        return null;
    }
}
