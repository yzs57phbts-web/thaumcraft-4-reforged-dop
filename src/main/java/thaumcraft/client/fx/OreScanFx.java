package thaumcraft.client.fx;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.client.geom.FacingQuads;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFx;
import thaumcraft.client.platform.WorldFxRenderer;

public class OreScanFx extends WorldFx {
    private static final ResourceLocation NODES = Thaumcraft.id("textures/misc/nodes.png");
    private static final ResourceLocation WARDED_GLASS = Thaumcraft.id("textures/block/wardedglass.png");
    private static final int SIZE = 17;
    private static final int HALF = 8;
    private static final int WATER_COLOR = 3986684;
    private static final int LAVA_COLOR = 16734721;
    private static @Nullable Scan current;

    private final Scan scan;
    private final boolean fluids;

    private record Scan(BlockPos center, int[] values, long until) {
    }

    private OreScanFx(Scan scan, boolean fluids) {
        super(scan.center().getX(), scan.center().getY(), scan.center().getZ(), Integer.MAX_VALUE);
        this.scan = scan;
        this.fluids = fluids;
    }

    public static void start(Level level, BlockPos center, int range, long until) {
        int[] values = new int[SIZE * SIZE * SIZE];
        Arrays.fill(values, -1);
        int clamped = Math.min(range, HALF);
        for (int dx = -clamped; dx <= clamped; dx++) {
            for (int dy = -clamped; dy <= clamped; dy++) {
                for (int dz = -clamped; dz <= clamped; dz++) {
                    values[index(dx, dy, dz)] = value(level, center.offset(dx, dy, dz));
                }
            }
        }
        Scan scan = new Scan(center.immutable(), values, until);
        current = scan;
        WorldFxRenderer.add(new OreScanFx(scan, false));
        WorldFxRenderer.add(new OreScanFx(scan, true));
    }

    private static int index(int dx, int dy, int dz) {
        return ((dx + HALF) * SIZE + dy + HALF) * SIZE + dz + HALF;
    }

    private static int value(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return -1;
        }
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return -10;
        }
        if (state.getFluidState().is(FluidTags.WATER)) {
            return -5;
        }
        if (!state.is(Tags.Blocks.ORES)) {
            return -1;
        }
        ItemStack stack = new ItemStack(state.getBlock());
        if (stack.isEmpty()) {
            return 0;
        }
        return AspectHelper.getBonusTags(stack).visSize();
    }

    @Override
    public boolean isDead() {
        return current != scan || System.currentTimeMillis() >= scan.until();
    }

    @Override
    public ResourceLocation texture() {
        return fluids ? WARDED_GLASS : NODES;
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public boolean depthTest() {
        return false;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        long time = System.currentTimeMillis();
        long dif = scan.until() - time;
        int frame = (int) (time / 50L % 32L);
        FacingQuads.Camera facing = new FacingQuads.Camera(cameraYaw, cameraPitch, 0.0F, 0.0F, 0.0F);
        BlockPos center = scan.center();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dy = -HALF; dy <= HALF; dy++) {
                for (int dz = -HALF; dz <= HALF; dz++) {
                    int value = scan.values()[index(dx, dy, dz)];
                    if (value == -1 || fluids != (value < -1)) {
                        continue;
                    }
                    float alpha = 1.0F;
                    if (dif > 4750L) {
                        alpha = 1.0F - (dif - 4750L) / 5.0F;
                    }
                    if (dif < 1500L) {
                        alpha = dif / 1500.0F;
                    }
                    alpha *= 1.0F - (dx * dx + dy * dy + dz * dz) / 64.0F;
                    if (alpha <= 0.0F) {
                        continue;
                    }
                    float px = (float) (center.getX() + dx + 0.5 - camera.x);
                    float py = (float) (center.getY() + dy + 0.5 - camera.y);
                    float pz = (float) (center.getZ() + dz + 0.5 - camera.z);
                    if (fluids) {
                        cube(sink, px, py, pz, value == -5 ? WATER_COLOR : LAVA_COLOR, alpha);
                    } else {
                        float size = value / 7.0F;
                        int a = Math.clamp((int) (alpha * 255.0F), 0, 255) << 24;
                        FacingQuads.strip(sink, facing, px, py, pz, 0.0F, 0.2F * size, 32, 0, frame, a | 11184657, 220);
                        FacingQuads.strip(sink, facing, px, py, pz, 0.0F, 0.5F * size, 32, 0, frame, a | 11145506, 220);
                    }
                }
            }
        }
    }

    private static void cube(QuadSink sink, float px, float py, float pz, int color, float alpha) {
        int argb = Math.clamp((int) (alpha * 255.0F), 0, 255) << 24 | color & 0xFFFFFF;
        for (Direction direction : Direction.values()) {
            Vector3f normal = new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            Vector3f u = Math.abs(normal.y) > 0.5F ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
            Vector3f v = new Vector3f(normal).cross(u);
            float cx = px + normal.x * 0.5F;
            float cy = py + normal.y * 0.5F;
            float cz = pz + normal.z * 0.5F;
            sink.vertex(cx - u.x * 0.5F + v.x * 0.5F, cy - u.y * 0.5F + v.y * 0.5F, cz - u.z * 0.5F + v.z * 0.5F, 0.0F, 1.0F, argb, 200);
            sink.vertex(cx + u.x * 0.5F + v.x * 0.5F, cy + u.y * 0.5F + v.y * 0.5F, cz + u.z * 0.5F + v.z * 0.5F, 1.0F, 1.0F, argb, 200);
            sink.vertex(cx + u.x * 0.5F - v.x * 0.5F, cy + u.y * 0.5F - v.y * 0.5F, cz + u.z * 0.5F - v.z * 0.5F, 1.0F, 0.0F, argb, 200);
            sink.vertex(cx - u.x * 0.5F - v.x * 0.5F, cy - u.y * 0.5F - v.y * 0.5F, cz - u.z * 0.5F - v.z * 0.5F, 0.0F, 0.0F, argb, 200);
        }
    }
}
