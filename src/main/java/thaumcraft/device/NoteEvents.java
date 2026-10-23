package thaumcraft.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class NoteEvents {
    private static final Map<Level, List<Event>> EVENTS = new WeakHashMap<>();

    private NoteEvents() {
    }

    public record Event(BlockPos pos, NoteBlockInstrument instrument, int note) {
    }

    public static List<Event> get(Level level) {
        List<Event> events = EVENTS.get(level);
        return events == null ? List.of() : events;
    }

    @SubscribeEvent
    static void onNotePlayed(NoteBlockEvent.Play event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            EVENTS.computeIfAbsent(level, key -> new ArrayList<>()).add(new Event(event.getPos().immutable(), event.getInstrument(), event.getVanillaNoteId()));
        }
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        List<Event> events = EVENTS.get(event.getLevel());
        if (events != null) {
            events.clear();
        }
    }
}
