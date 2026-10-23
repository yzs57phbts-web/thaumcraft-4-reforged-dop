package thaumcraft.client.render.block;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import thaumcraft.block.WardedBlock;
import thaumcraft.blockentity.WardedBlockEntity;

public class WardedBlockModel extends BakedModelWrapper<BakedModel> {
    public WardedBlockModel(BakedModel fallback) {
        super(fallback);
    }

    private static @Nullable BlockState disguise(ModelData data) {
        BlockState state = data.get(WardedBlockEntity.DISGUISE);
        return state == null || state.getBlock() instanceof WardedBlock ? null : state;
    }

    private BakedModel model(@Nullable BlockState state) {
        return state == null ? originalModel : Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        BlockState disguise = disguise(data);
        return model(disguise).getQuads(disguise == null ? state : disguise, side, random, data, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return model(disguise(data)).getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        BlockState disguise = disguise(data);
        return model(disguise).getRenderTypes(disguise == null ? state : disguise, random, data);
    }

    public static final class Unbaked implements IUnbakedGeometry<Unbaked> {
        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ItemOverrides overrides) {
            return new WardedBlockModel(baker.bake(ResourceLocation.withDefaultNamespace("block/stone"), state));
        }
    }
}
