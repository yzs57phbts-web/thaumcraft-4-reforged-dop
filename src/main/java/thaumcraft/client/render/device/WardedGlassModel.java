package thaumcraft.client.render.device;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import thaumcraft.client.render.ArchitectOverlay;
import thaumcraft.registry.ModBlocks;

public class WardedGlassModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<int[]> FACES = new ModelProperty<>();
    private final List<BakedModel> models;

    public WardedGlassModel(List<BakedModel> models) {
        super(models.get(0));
        this.models = models;
    }

    private static boolean glass(BlockAndTintGetter level, BlockPos pos, int x, int y, int z) {
        return level.getBlockState(pos.offset(x, y, z)).is(ModBlocks.WARDED_GLASS.get());
    }

    static int spriteIndex(BlockAndTintGetter level, BlockPos pos, Direction direction) {
        int side = direction.get3DDataValue();
        boolean[] bits = new boolean[8];
        if (side == 0 || side == 1) {
            bits[0] = glass(level, pos, -1, 0, -1);
            bits[1] = glass(level, pos, 0, 0, -1);
            bits[2] = glass(level, pos, 1, 0, -1);
            bits[3] = glass(level, pos, -1, 0, 0);
            bits[4] = glass(level, pos, 1, 0, 0);
            bits[5] = glass(level, pos, -1, 0, 1);
            bits[6] = glass(level, pos, 0, 0, 1);
            bits[7] = glass(level, pos, 1, 0, 1);
        } else if (side == 2 || side == 3) {
            int before = side == 2 ? 1 : -1;
            int after = side == 3 ? 1 : -1;
            bits[0] = glass(level, pos, before, 1, 0);
            bits[1] = glass(level, pos, 0, 1, 0);
            bits[2] = glass(level, pos, after, 1, 0);
            bits[3] = glass(level, pos, before, 0, 0);
            bits[4] = glass(level, pos, after, 0, 0);
            bits[5] = glass(level, pos, before, -1, 0);
            bits[6] = glass(level, pos, 0, -1, 0);
            bits[7] = glass(level, pos, after, -1, 0);
        } else {
            int before = side == 5 ? 1 : -1;
            int after = side == 4 ? 1 : -1;
            bits[0] = glass(level, pos, 0, 1, before);
            bits[1] = glass(level, pos, 0, 1, 0);
            bits[2] = glass(level, pos, 0, 1, after);
            bits[3] = glass(level, pos, 0, 0, before);
            bits[4] = glass(level, pos, 0, 0, after);
            bits[5] = glass(level, pos, 0, -1, before);
            bits[6] = glass(level, pos, 0, -1, 0);
            bits[7] = glass(level, pos, 0, -1, after);
        }
        int id = 0;
        for (int index = 0; index < 8; index++) {
            if (bits[index]) {
                id |= 1 << index;
            }
        }
        return ArchitectOverlay.CONNECTED[id];
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        int[] faces = new int[6];
        for (Direction direction : Direction.values()) {
            faces[direction.get3DDataValue()] = spriteIndex(level, pos, direction);
        }
        return data.derive().with(FACES, faces).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        if (side == null) {
            return List.of();
        }
        int[] faces = data.get(FACES);
        int index = faces == null ? 0 : faces[side.get3DDataValue()];
        return models.get(index).getQuads(state, side, random, data, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.translucent());
    }

    public record Unbaked(List<ResourceLocation> models) implements IUnbakedGeometry<Unbaked> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> getter, IGeometryBakingContext context) {
            for (ResourceLocation model : models) {
                getter.apply(model).resolveParents(getter);
            }
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ItemOverrides overrides) {
            return new WardedGlassModel(models.stream().map(model -> baker.bake(model, state)).toList());
        }
    }
}
