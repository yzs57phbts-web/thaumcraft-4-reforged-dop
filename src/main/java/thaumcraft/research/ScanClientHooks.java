package thaumcraft.research;

import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public final class ScanClientHooks {
    public interface Scanner {
        void useTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining);

        void stopUsing(LivingEntity entity);
    }

    private static @Nullable Scanner scanner;
    private static @Nullable Supplier<Player> localPlayer;

    private ScanClientHooks() {
    }

    public static void setScanner(Scanner value) {
        scanner = value;
    }

    public static void setLocalPlayer(Supplier<Player> value) {
        localPlayer = value;
    }

    public static @Nullable Player localPlayer() {
        return localPlayer == null ? null : localPlayer.get();
    }

    public static void useTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (scanner != null) {
            scanner.useTick(level, entity, stack, ticksRemaining);
        }
    }

    public static void stopUsing(LivingEntity entity) {
        if (scanner != null) {
            scanner.stopUsing(entity);
        }
    }
}
