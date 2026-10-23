package thaumcraft.research;

import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;

public class ResearchItem {
    private final String key;
    private final String category;
    private final AspectList tags;
    private final int displayColumn;
    private final int displayRow;
    private final @Nullable TcStack iconItem;
    private final @Nullable ResourceLocation iconTexture;
    private List<String> parents = List.of();
    private List<String> parentsHidden = List.of();
    private List<String> siblings = List.of();
    private int complexity;
    private boolean special;
    private boolean secondary;
    private boolean round;
    private boolean stub;
    private boolean virtual;
    private boolean concealed;
    private boolean hidden;
    private boolean lost;
    private boolean autoUnlock;
    private int warp;
    private List<ResourceLocation> itemTriggers = List.of();
    private List<ResourceLocation> entityTriggers = List.of();
    private List<Aspect> aspectTriggers = List.of();
    private List<ResearchPage> pages = List.of();

    public ResearchItem(String key, String category) {
        this.key = key;
        this.category = category;
        this.tags = new AspectList();
        this.displayColumn = 0;
        this.displayRow = 0;
        this.iconItem = null;
        this.iconTexture = null;
        this.virtual = true;
    }

    public ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon) {
        this(key, category, tags, column, row, complexity, icon, null);
    }

    public ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        this(key, category, tags, column, row, complexity, null, icon);
    }

    private ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, @Nullable TcStack iconItem, @Nullable ResourceLocation iconTexture) {
        this.key = key;
        this.category = category;
        this.tags = tags;
        this.displayColumn = column;
        this.displayRow = row;
        this.iconItem = iconItem;
        this.iconTexture = iconTexture;
        this.complexity = Math.max(1, Math.min(3, complexity));
    }

    public ResearchItem setSpecial() {
        special = true;
        return this;
    }

    public ResearchItem setSecondary() {
        secondary = true;
        return this;
    }

    public ResearchItem setRound() {
        round = true;
        return this;
    }

    public ResearchItem setStub() {
        stub = true;
        return this;
    }

    public ResearchItem setVirtual() {
        virtual = true;
        return this;
    }

    public ResearchItem setConcealed() {
        concealed = true;
        return this;
    }

    public ResearchItem setHidden() {
        hidden = true;
        return this;
    }

    public ResearchItem setLost() {
        lost = true;
        return this;
    }

    public ResearchItem setAutoUnlock() {
        autoUnlock = true;
        return this;
    }

    public ResearchItem setComplexity(int complexity) {
        this.complexity = complexity;
        return this;
    }

    public ResearchItem setWarp(int warp) {
        this.warp = warp;
        return this;
    }

    public ResearchItem setParents(String... parents) {
        this.parents = List.of(parents);
        return this;
    }

    public ResearchItem setParentsHidden(String... parentsHidden) {
        this.parentsHidden = List.of(parentsHidden);
        return this;
    }

    public ResearchItem setSiblings(String... siblings) {
        this.siblings = List.of(siblings);
        return this;
    }

    public ResearchItem setItemTriggers(String... items) {
        this.itemTriggers = ids(items);
        return this;
    }

    public ResearchItem setEntityTriggers(String... entities) {
        this.entityTriggers = ids(entities);
        return this;
    }

    public ResearchItem setAspectTriggers(Aspect... aspects) {
        this.aspectTriggers = List.of(aspects);
        return this;
    }

    public ResearchItem setPages(ResearchPage... pages) {
        this.pages = List.of(pages);
        return this;
    }

    public ResearchItem registerResearchItem() {
        ResearchCategories.addResearch(this);
        return this;
    }

    public boolean isAvailable() {
        return thaumcraft.modern.MetalIntegration.researchAvailable(key);
    }

    public String key() {
        return key;
    }

    public String category() {
        return category;
    }

    public AspectList tags() {
        return tags;
    }

    public int column() {
        return displayColumn;
    }

    public int row() {
        return displayRow;
    }

    public @Nullable TcStack iconItem() {
        return iconItem;
    }

    public @Nullable ResourceLocation iconTexture() {
        return iconTexture;
    }

    public List<String> parents() {
        return parents;
    }

    public List<String> parentsHidden() {
        return parentsHidden;
    }

    public List<String> siblings() {
        return siblings;
    }

    public int complexity() {
        return complexity;
    }

    public int warp() {
        return warp;
    }

    public boolean isSpecial() {
        return special;
    }

    public boolean isSecondary() {
        return secondary;
    }

    public boolean isRound() {
        return round;
    }

    public boolean isStub() {
        return stub;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isConcealed() {
        return concealed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLost() {
        return lost;
    }

    public boolean isAutoUnlock() {
        return autoUnlock;
    }

    public List<ResourceLocation> itemTriggers() {
        return itemTriggers;
    }

    public List<ResourceLocation> entityTriggers() {
        return entityTriggers;
    }

    public List<Aspect> aspectTriggers() {
        return aspectTriggers;
    }

    public List<ResearchPage> pages() {
        return pages;
    }

    public String nameKey() {
        return "tc.research_name." + key;
    }

    public String textKey() {
        return "tc.research_text." + key;
    }

    public net.minecraft.network.chat.Component description() {
        return net.minecraft.network.chat.Component.translatable(textKey());
    }

    public @Nullable Aspect primaryTag() {
        Aspect primary = null;
        int highest = 0;
        for (Aspect aspect : tags.getAspects()) {
            if (tags.getAmount(aspect) > highest) {
                primary = aspect;
                highest = tags.getAmount(aspect);
            }
        }
        return primary;
    }

    private static List<ResourceLocation> ids(String[] values) {
        return Arrays.stream(values).map(ResourceLocation::parse).toList();
    }
}
