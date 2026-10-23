package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public final class MachineFx {
    public interface Proxy {
        default void furnaceLava(Level level, BlockPos pos, Direction facing) {
        }

        default @Nullable Object boreBeam(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, int type, int color, boolean reverse, float endMod, @Nullable Object previous, int impact) {
            return null;
        }

        default void boreDig(Level level, BlockPos block, BlockPos target, BlockState state) {
        }

        default int particleCount(int base) {
            return 0;
        }
    }

    private static Proxy proxy = new Proxy() {
    };

    private MachineFx() {
    }

    public static Proxy get() {
        return proxy;
    }

    public static void set(Proxy value) {
        proxy = value;
    }
}
