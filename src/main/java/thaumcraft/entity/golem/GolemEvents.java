package thaumcraft.entity.golem;

import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.platform.Teleport;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class GolemEvents {
    private GolemEvents() { }
    @SubscribeEvent
    static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var source = player.level().getServer().getLevel(event.getFrom());
        if (source == null) return;
        var followers = new ArrayList<TravelingTrunk>();
        for (var entity : source.getAllEntities()) if (entity instanceof TravelingTrunk trunk && trunk.isAlive() && !trunk.getStay() && player.getUUID().equals(trunk.getOwnerUUID())) followers.add(trunk);
        for (var trunk : followers) Teleport.to(trunk, player.serverLevel(), player.position().add(0, 0.25, 0), trunk.getYRot(), trunk.getXRot());
    }
}
