package thaumcraft.client.render.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.device.SinisterCone;
import thaumcraft.registry.ModItems;

public final class SinisterNodes {
    private static final Map<GlobalPos, Long> NODES = new HashMap<>();
    private static boolean active;

    private SinisterNodes() {
    }

    public static void add(ResourceKey<Level> dimension, BlockPos pos) {
        NODES.put(GlobalPos.of(dimension, pos), System.currentTimeMillis());
    }

    public static boolean isActive() {
        return active;
    }

    static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            NODES.clear();
            active = false;
            return;
        }
        if (!player.getInventory().contains(new net.minecraft.world.item.ItemStack(ModItems.SINISTER_LODESTONE.get()))) {
            return;
        }
        active = false;
        List<GlobalPos> expired = new ArrayList<>();
        for (Map.Entry<GlobalPos, Long> entry : NODES.entrySet()) {
            GlobalPos node = entry.getKey();
            if (entry.getValue() < System.currentTimeMillis() - SinisterCone.EXPIRY_MILLIS) {
                expired.add(node);
            }
            if (node.dimension() == minecraft.level.dimension()
                && SinisterCone.isVisibleTo(SinisterCone.APERTURE, player.getEyePosition(), player.getLookAngle(), node.pos().getX() + 0.5, node.pos().getY() + 0.5, node.pos().getZ() + 0.5, SinisterCone.RANGE)) {
                active = true;
                break;
            }
        }
        for (GlobalPos node : expired) {
            NODES.remove(node);
        }
    }
}
