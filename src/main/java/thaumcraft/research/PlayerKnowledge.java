package thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class PlayerKnowledge {
    private static final Codec<Set<String>> STRING_SET = Codec.STRING.listOf().xmap(LinkedHashSet::new, List::copyOf);
    private static final StreamCodec<ByteBuf, Set<String>> STRING_SET_STREAM = ByteBufCodecs.<ByteBuf, String, Set<String>>collection(LinkedHashSet::new, ByteBufCodecs.STRING_UTF8);

    public static final Codec<PlayerKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        STRING_SET.optionalFieldOf("research", Set.of()).forGetter(knowledge -> knowledge.research),
        AspectList.CODEC.optionalFieldOf("aspects", AspectList.EMPTY).forGetter(knowledge -> knowledge.aspects),
        STRING_SET.optionalFieldOf("objects", Set.of()).forGetter(knowledge -> knowledge.objects),
        STRING_SET.optionalFieldOf("entities", Set.of()).forGetter(knowledge -> knowledge.entities),
        STRING_SET.optionalFieldOf("phenomena", Set.of()).forGetter(knowledge -> knowledge.phenomena),
        Codec.INT.optionalFieldOf("warp", 0).forGetter(knowledge -> knowledge.warp),
        Codec.INT.optionalFieldOf("warp_sticky", 0).forGetter(knowledge -> knowledge.warpSticky),
        Codec.INT.optionalFieldOf("warp_temp", 0).forGetter(knowledge -> knowledge.warpTemp),
        Codec.INT.optionalFieldOf("warp_counter", 0).forGetter(knowledge -> knowledge.warpCounter),
        Codec.BOOL.optionalFieldOf("started", false).forGetter(knowledge -> knowledge.started)
    ).apply(instance, PlayerKnowledge::new));

    public static final StreamCodec<ByteBuf, PlayerKnowledge> STREAM_CODEC = StreamCodec.of(
        (buffer, knowledge) -> {
            STRING_SET_STREAM.encode(buffer, knowledge.research);
            AspectList.STREAM_CODEC.encode(buffer, knowledge.aspects);
            STRING_SET_STREAM.encode(buffer, knowledge.objects);
            STRING_SET_STREAM.encode(buffer, knowledge.entities);
            STRING_SET_STREAM.encode(buffer, knowledge.phenomena);
            ByteBufCodecs.VAR_INT.encode(buffer, knowledge.warp);
            ByteBufCodecs.VAR_INT.encode(buffer, knowledge.warpSticky);
            ByteBufCodecs.VAR_INT.encode(buffer, knowledge.warpTemp);
            ByteBufCodecs.VAR_INT.encode(buffer, knowledge.warpCounter);
            ByteBufCodecs.BOOL.encode(buffer, knowledge.started);
        },
        buffer -> new PlayerKnowledge(
            STRING_SET_STREAM.decode(buffer),
            AspectList.STREAM_CODEC.decode(buffer),
            STRING_SET_STREAM.decode(buffer),
            STRING_SET_STREAM.decode(buffer),
            STRING_SET_STREAM.decode(buffer),
            ByteBufCodecs.VAR_INT.decode(buffer),
            ByteBufCodecs.VAR_INT.decode(buffer),
            ByteBufCodecs.VAR_INT.decode(buffer),
            ByteBufCodecs.VAR_INT.decode(buffer),
            ByteBufCodecs.BOOL.decode(buffer)
        )
    );

    private final Set<String> research;
    private final AspectList aspects;
    private final Set<String> objects;
    private final Set<String> entities;
    private final Set<String> phenomena;
    private int warp;
    private int warpSticky;
    private int warpTemp;
    private int warpCounter;
    private boolean started;

    public PlayerKnowledge() {
        this(Set.of(), AspectList.EMPTY, Set.of(), Set.of(), Set.of(), 0, 0, 0, 0, false);
    }

    private PlayerKnowledge(
        Set<String> research,
        AspectList aspects,
        Set<String> objects,
        Set<String> entities,
        Set<String> phenomena,
        int warp,
        int warpSticky,
        int warpTemp,
        int warpCounter,
        boolean started
    ) {
        this.research = new LinkedHashSet<>(research);
        this.aspects = aspects.copy();
        this.objects = new LinkedHashSet<>(objects);
        this.entities = new LinkedHashSet<>(entities);
        this.phenomena = new LinkedHashSet<>(phenomena);
        this.warp = warp;
        this.warpSticky = warpSticky;
        this.warpTemp = warpTemp;
        this.warpCounter = warpCounter;
        this.started = started;
        addPrimals();
    }

    public PlayerKnowledge copy() {
        return new PlayerKnowledge(research, aspects, objects, entities, phenomena, warp, warpSticky, warpTemp, warpCounter, started);
    }

    public boolean isResearchComplete(String key) {
        return research.contains(key);
    }

    public boolean addResearch(String key) {
        return research.add(key);
    }

    public boolean removeResearch(String key) {
        return research.remove(key);
    }

    public void clearResearch() {
        research.clear();
    }

    public Set<String> research() {
        return Collections.unmodifiableSet(research);
    }

    public AspectList aspects() {
        return aspects;
    }

    public AspectList visibleAspects() {
        AspectList visible = new AspectList();
        for (Aspect aspect : aspects.getAspects()) {
            if (aspect.isEnabled()) visible.add(aspect, aspects.getAmount(aspect));
        }
        return visible;
    }

    public boolean hasDiscoveredAspect(Aspect aspect) {
        return aspect.isEnabled() && aspects.getAspects().contains(aspect);
    }

    public boolean hasDiscoveredParentAspects(Aspect aspect) {
        if (!aspect.isEnabled()) return false;
        Aspect[] components = aspect.getComponents();
        if (components == null) {
            return true;
        }
        for (Aspect component : components) {
            if (!hasDiscoveredAspect(component)) {
                return false;
            }
        }
        return true;
    }

    public int getAspectPool(Aspect aspect) {
        return aspect.isEnabled() ? aspects.getAmount(aspect) : 0;
    }

    public boolean discoverAspect(Aspect aspect) {
        if (!aspect.isEnabled() || hasDiscoveredAspect(aspect)) {
            return false;
        }
        aspects.add(aspect, 0);
        return true;
    }

    public void addAspectPool(Aspect aspect, int amount) {
        if (aspect.isEnabled()) aspects.add(aspect, amount);
    }

    public void setAspectPool(Aspect aspect, int amount) {
        if (!aspect.isEnabled()) return;
        aspects.remove(aspect);
        aspects.add(aspect, amount);
    }

    public boolean isScanned(ScanKind kind, String key) {
        return scanned(kind).contains(key);
    }

    public boolean addScanned(ScanKind kind, String key) {
        return scanned(kind).add(key);
    }

    public Set<String> scannedSet(ScanKind kind) {
        return Collections.unmodifiableSet(scanned(kind));
    }

    public int getWarp(WarpType type) {
        return switch (type) {
            case PERMANENT -> warp;
            case STICKY -> warpSticky;
            case TEMPORARY -> warpTemp;
        };
    }

    public void setWarp(WarpType type, int amount) {
        int value = Math.max(0, amount);
        switch (type) {
            case PERMANENT -> warp = value;
            case STICKY -> warpSticky = value;
            case TEMPORARY -> warpTemp = value;
        }
    }

    public int getTotalWarp() {
        return warp + warpSticky + warpTemp;
    }

    public int getWarpCounter() {
        return warpCounter;
    }

    public void setWarpCounter(int warpCounter) {
        this.warpCounter = warpCounter;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    private Set<String> scanned(ScanKind kind) {
        return switch (kind) {
            case OBJECT -> objects;
            case ENTITY -> entities;
            case PHENOMENON -> phenomena;
        };
    }

    private void addPrimals() {
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (!hasDiscoveredAspect(aspect)) {
                aspects.add(aspect, 0);
            }
        }
    }
}
