package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class NodeRenderTypes extends RenderType {
    private static ShaderInstance shader;
    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");
    private static final ShaderStateShard NODE_SHADER = new ShaderStateShard(
        () -> IRIS_LOADED && IrisCompat.isShaderPackInUse() ? GameRenderer.getParticleShader() : shader
    );
    private static final Map<Key, RenderType> TYPES = new HashMap<>();

    private record Key(ResourceLocation texture, boolean translucent, boolean depthIgnore) {
    }

    private NodeRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                            Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    @SubscribeEvent
    static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Thaumcraft.id("node"), DefaultVertexFormat.PARTICLE), loaded -> shader = loaded);
    }

    public static RenderType of(ResourceLocation texture, boolean translucent, boolean depthIgnore) {
        return TYPES.computeIfAbsent(new Key(texture, translucent, depthIgnore), key -> RenderType.create(
            "thaumcraft_node_" + (key.translucent() ? "translucent" : "additive") + (key.depthIgnore() ? "_no_depth" : ""),
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            CompositeState.builder()
                .setShaderState(NODE_SHADER)
                .setTextureState(new TextureStateShard(key.texture(), false, false))
                .setTransparencyState(key.translucent() ? TRANSLUCENT_TRANSPARENCY : LIGHTNING_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .setDepthTestState(key.depthIgnore() ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        ));
    }
}
