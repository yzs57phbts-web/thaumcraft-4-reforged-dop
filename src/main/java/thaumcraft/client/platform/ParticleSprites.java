package thaumcraft.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public final class ParticleSprites {
    private ParticleSprites() {
    }

    public static @Nullable TextureAtlasSprite block(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);
    }

    public static @Nullable TextureAtlasSprite item(Level level, ItemStack stack, RandomSource random) {
        return Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0).getParticleIcon();
    }

    public static float u(TextureAtlasSprite sprite, float fraction) {
        return sprite.getU(fraction);
    }

    public static float v(TextureAtlasSprite sprite, float fraction) {
        return sprite.getV(fraction);
    }

    public static ResourceLocation atlas(TextureAtlasSprite sprite) {
        return sprite.atlasLocation();
    }
}
