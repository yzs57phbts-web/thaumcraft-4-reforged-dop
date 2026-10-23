package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BlockHighlightRenderer {
    private BlockHighlightRenderer() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || event.getCamera().getEntity() == null) {
            return;
        }
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (state.isAir() || !minecraft.level.getWorldBorder().isWithinBounds(pos)
            || !BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(Thaumcraft.MODID)) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        double offsetX = pos.getX() - camera.x;
        double offsetY = pos.getY() - camera.y;
        double offsetZ = pos.getZ() - camera.z;
        PoseStack.Pose pose = event.getPoseStack().last();
        VertexConsumer buffer = event.getMultiBufferSource().getBuffer(TcRenderTypes.blockHighlight());
        state.getShape(minecraft.level, pos, CollisionContext.of(event.getCamera().getEntity())).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float normalX = (float) (x2 - x1);
            float normalY = (float) (y2 - y1);
            float normalZ = (float) (z2 - z1);
            float length = Mth.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            normalX /= length;
            normalY /= length;
            normalZ /= length;
            buffer.addVertex(pose, (float) (x1 + offsetX), (float) (y1 + offsetY), (float) (z1 + offsetZ))
                .setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, normalX, normalY, normalZ);
            buffer.addVertex(pose, (float) (x2 + offsetX), (float) (y2 + offsetY), (float) (z2 + offsetZ))
                .setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, normalX, normalY, normalZ);
        });
        event.setCanceled(true);
    }
}
