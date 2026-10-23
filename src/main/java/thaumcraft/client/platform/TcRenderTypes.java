package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class TcRenderTypes extends RenderType {
    private static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(GameRenderer::getParticleShader);
    private static final RenderType BLOCK_HIGHLIGHT = RenderType.create(
        "thaumcraft_block_highlight",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LINES_SHADER)
            .setLineState(new LineStateShard(OptionalDouble.empty()))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
    private static final Map<ResourceLocation, RenderType> ELDRITCH = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ELDRITCH_PORTAL = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE_NO_DEPTH = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_NO_DEPTH = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH = new HashMap<>();

    private TcRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public enum Kind {
        ADDITIVE,
        TRANSLUCENT,
        ADDITIVE_NO_DEPTH,
        TRANSLUCENT_NO_DEPTH,
        ENTITY_CUTOUT,
        ENTITY_TRANSLUCENT
    }

    public static RenderType of(Kind kind, ResourceLocation texture) {
        return switch (kind) {
            case ADDITIVE -> additive(texture);
            case TRANSLUCENT -> translucent(texture);
            case ADDITIVE_NO_DEPTH -> additiveNoDepth(texture);
            case TRANSLUCENT_NO_DEPTH -> translucentNoDepth(texture);
            case ENTITY_CUTOUT -> entityCutout(texture);
            case ENTITY_TRANSLUCENT -> entityTranslucent(texture);
        };
    }

    private static RenderType particleLike(String name, ResourceLocation texture, TransparencyStateShard transparency, DepthTestStateShard depthTest) {
        return RenderType.create(
            name,
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(transparency)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .setDepthTestState(depthTest)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        );
    }

    public static RenderType eldritchPortal(ResourceLocation texture) {
        return ELDRITCH_PORTAL.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_eldritch_portal",
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(new TextureStateShard(key, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false)
        ));
    }

    public static RenderType eldritchSurface(ResourceLocation texture) {
        return ELDRITCH.computeIfAbsent(texture, key -> RenderType.create("thaumcraft_eldritch_surface", DefaultVertexFormat.PARTICLE, VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder().setShaderState(PARTICLE_SHADER).setTextureState(new TextureStateShard(key, false, false))
                .setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false)));
    }

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> particleLike("thaumcraft_additive", key, LIGHTNING_TRANSPARENCY, LEQUAL_DEPTH_TEST));
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> particleLike("thaumcraft_translucent", key, TRANSLUCENT_TRANSPARENCY, LEQUAL_DEPTH_TEST));
    }

    public static RenderType additiveNoDepth(ResourceLocation texture) {
        return ADDITIVE_NO_DEPTH.computeIfAbsent(texture, key -> particleLike("thaumcraft_additive_no_depth", key, LIGHTNING_TRANSPARENCY, NO_DEPTH_TEST));
    }

    public static RenderType translucentNoDepth(ResourceLocation texture) {
        return TRANSLUCENT_NO_DEPTH.computeIfAbsent(texture, key -> particleLike("thaumcraft_translucent_no_depth", key, TRANSLUCENT_TRANSPARENCY, NO_DEPTH_TEST));
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode, boolean depth) {
        if (depth) {
            return byBlend(texture, blendMode);
        }
        return blendMode == 771 ? translucentNoDepth(texture) : additiveNoDepth(texture);
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }

    public static RenderType eyes(ResourceLocation texture) {
        return RenderType.eyes(texture);
    }

    public static RenderType translucentMovingBlock() {
        return RenderType.translucentMovingBlock();
    }

    public static RenderType entityTranslucentNoDepth(ResourceLocation texture) {
        return ENTITY_TRANSLUCENT_NO_DEPTH.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_entity_translucent_no_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(key, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(true)
        ));
    }

    public static RenderType outline(ResourceLocation texture) {
        return RenderType.outline(texture);
    }

    public static RenderType blockHighlight() {
        return BLOCK_HIGHLIGHT;
    }
}
