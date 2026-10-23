import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import thaumcraft.client.platform.Billboards;

public final class BillboardVertexTest {
    public static void main(String[] args) {
        try (ByteBufferBuilder memory = new ByteBufferBuilder(1024)) {
            BufferBuilder buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            PoseStack poses = new PoseStack();
            Billboards.submit(poses, type -> buffer, new Camera(), null, 0, 1, 0, 1, 0.5F, 0xFFFFFFFF, 0xF000F0);
            try (var mesh = buffer.buildOrThrow()) {
                if (mesh.drawState().vertexCount() != 4) throw new AssertionError("incomplete billboard");
            }
        }
        System.out.println("Billboard entity vertex format passed, including overlay and normal");
    }
}
