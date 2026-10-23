package thaumcraft.client.render;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

public final class WandItemModel extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    public WandItemModel(BakedModel model) {
        super(model);
        overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                WandSpecialRenderer.setOwner(entity);
                BakedModel resolved = model.getOverrides().resolve(model, stack, level, entity, seed);
                return resolved == model ? WandItemModel.this : resolved;
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }
}
