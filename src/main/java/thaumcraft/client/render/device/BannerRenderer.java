package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.device.BannerBlockEntity;

public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        int color = -1;
        @Nullable Aspect aspect;
        int facing;
        boolean onWall;
        float swing;
    }

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public AABB getRenderBoundingBox(BannerBlockEntity banner) {
        return new AABB(banner.getBlockPos()).expandTowards(0.0, 1.0, 0.0);
    }

    private State extract(BannerBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        state.color = entity.getColor();
        state.aspect = entity.getAspect();
        state.facing = entity.getFacing();
        state.onWall = entity.isOnWall();
        long time = entity.getLevel() == null ? 0L : entity.getLevel().getGameTime();
        state.swing = WardedRendering.bannerSwing(state.blockPos.getX(), state.blockPos.getY(), state.blockPos.getZ(), time, partialTicks);
        return state;
    }

    @Override
    public void render(BannerBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        WardedRendering.banner(collector, poseStack, state.lightCoords, state.color, state.aspect, true, state.facing, state.onWall, state.swing);
    }
}
