package thaumcraft.wand.focus;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class FocusUpgradeType {
    private static FocusUpgradeType[] types = new FocusUpgradeType[20];

    public static final FocusUpgradeType POTENCY = new FocusUpgradeType(0, "potency", new AspectList().add(Aspect.WEAPON, 1));
    public static final FocusUpgradeType FRUGAL = new FocusUpgradeType(1, "frugal", new AspectList().add(Aspect.HUNGER, 1));
    public static final FocusUpgradeType TREASURE = new FocusUpgradeType(2, "treasure", new AspectList().add(Aspect.GREED, 1));
    public static final FocusUpgradeType ENLARGE = new FocusUpgradeType(3, "enlarge", new AspectList().add(Aspect.TRAVEL, 1));
    public static final FocusUpgradeType ALCHEMISTS_FIRE = new FocusUpgradeType(4, "alchemistsfire", new AspectList().add(Aspect.ENERGY, 1).add(Aspect.SLIME, 1));
    public static final FocusUpgradeType ALCHEMISTS_FROST = new FocusUpgradeType(5, "alchemistsfrost", new AspectList().add(Aspect.COLD, 1).add(Aspect.TRAP, 1));
    public static final FocusUpgradeType ARCHITECT = new FocusUpgradeType(6, "architect", new AspectList().add(Aspect.CRAFT, 1));
    public static final FocusUpgradeType EXTEND = new FocusUpgradeType(7, "extend", new AspectList().add(Aspect.EXCHANGE, 1));
    public static final FocusUpgradeType SILKTOUCH = new FocusUpgradeType(8, "silktouch", new AspectList().add(Aspect.GREED, 1));

    private final int id;
    private final String name;
    private final ResourceLocation icon;
    private final AspectList aspects;

    public FocusUpgradeType(int id, String name, AspectList aspects) {
        this(id, name, Thaumcraft.id("textures/foci/" + name + ".png"), aspects);
    }

    public FocusUpgradeType(int id, String name, ResourceLocation icon, AspectList aspects) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.aspects = aspects;
        if (id < types.length && types[id] != null) {
            Thaumcraft.LOGGER.error("Focus Upgrade id {} already occupied. Ignoring.", id);
            return;
        }
        if (id >= types.length) {
            FocusUpgradeType[] grown = new FocusUpgradeType[id + 1];
            System.arraycopy(types, 0, grown, 0, types.length);
            types = grown;
        }
        types[id] = this;
    }

    public static @Nullable FocusUpgradeType byId(int id) {
        return id >= 0 && id < types.length ? types[id] : null;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public ResourceLocation icon() {
        return icon;
    }

    public AspectList aspects() {
        return aspects;
    }

    public Component getLocalizedName() {
        return Component.translatable("focus.upgrade." + name + ".name");
    }

    public Component getLocalizedText() {
        return Component.translatable("focus.upgrade." + name + ".text");
    }
}
