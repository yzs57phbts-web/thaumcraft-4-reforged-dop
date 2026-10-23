package thaumcraft.client.fx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.fx.Fx;
import thaumcraft.infusion.InfusionMatrixBlockEntity;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class EssentiaSourceTracker {
    private static final Map<String, Source> SOURCES = new LinkedHashMap<>();
    private static int tickCount;

    private EssentiaSourceTracker() {
    }

    public static void add(BlockPos start, BlockPos end, int color) {
        String key = start.getX() + ":" + start.getY() + ":" + start.getZ() + ":" + end.getX() + ":" + end.getY() + ":" + end.getZ() + ":" + color;
        Source existing = SOURCES.remove(key);
        if (existing != null) {
            existing.ticks = 15;
            SOURCES.put(key, existing);
        } else {
            SOURCES.put(key, new Source(start, end, 15, color));
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            SOURCES.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        tickCount++;
        for (String key : new ArrayList<>(SOURCES.keySet())) {
            Source source = SOURCES.get(key);
            if (source.ticks <= 0) {
                SOURCES.remove(key);
                continue;
            }
            BlockPos target = source.start;
            if (level.getBlockEntity(source.start) instanceof InfusionMatrixBlockEntity) {
                target = source.start.below();
            }
            if (source.ticks > 5) {
                Fx.get().essentiaTrail(level, source.end, target, tickCount, source.color, 1.0F);
            } else {
                Fx.get().essentiaTrail(level, source.end, target, tickCount - (5 - source.ticks), source.color, source.ticks * source.ticks / 25.0F);
            }
            source.ticks--;
        }
    }

    private static final class Source {
        private final BlockPos start;
        private final BlockPos end;
        private final int color;
        private int ticks;

        private Source(BlockPos start, BlockPos end, int ticks, int color) {
            this.start = start;
            this.end = end;
            this.ticks = ticks;
            this.color = color;
        }
    }
}
