package thaumcraft.platform;

import net.neoforged.bus.api.IEventBus;

public final class Platform {
    private Platform() {
    }

    public static void register(IEventBus bus) {
        StackData.register(bus);
        DataAttachments.register(bus);
        Registration.register(bus);
        Transfer.register(bus);
    }
}
