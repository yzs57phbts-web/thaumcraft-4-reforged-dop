package thaumcraft.client.fx;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.client.platform.ParticleSprites;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.device.MachineFx;

public final class MachineClientFx implements MachineFx.Proxy {
    @Override
    public void furnaceLava(Level level, BlockPos pos, Direction facing) {
        RandomSource random = level.getRandom();
        level.addParticle(
            ParticleTypes.LAVA,
            pos.getX() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.3F + facing.getStepX(),
            pos.getY() + 0.3F,
            pos.getZ() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.3F + facing.getStepZ(),
            0.0,
            0.0,
            0.0
        );
    }

    @Override
    public @Nullable Object boreBeam(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, int type, int color, boolean reverse, float endMod, @Nullable Object previous, int impact) {
        BoreBeamFx beam = previous instanceof BoreBeamFx existing && !existing.isDead() ? existing : null;
        if (beam != null) {
            beam.retarget(targetX, targetY, targetZ);
        } else {
            beam = new BoreBeamFx(level, x, y, z, targetX, targetY, targetZ, (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 8);
            beam.setType(type);
            beam.setReverse(reverse);
            WorldFxRenderer.add(beam);
        }
        beam.setEndMod(endMod);
        beam.setImpact(impact);
        return beam;
    }

    @Override
    public void boreDig(Level level, BlockPos block, BlockPos target, BlockState state) {
        RandomSource random = level.getRandom();
        double x = block.getX() + random.nextFloat();
        double y = block.getY() + random.nextFloat();
        double z = block.getZ() + random.nextFloat();
        if (random.nextInt(10) == 0) {
            WorldFxRenderer.add(new BoreSparkleFx(random, x, y, z, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));
            return;
        }
        TextureAtlasSprite sprite = ParticleSprites.block(state);
        if (sprite != null) {
            WorldFxRenderer.add(new BoreParticleFx(random, x, y, z, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, sprite));
        }
    }

    @Override
    public int particleCount(int base) {
        return ClientFx.particleCount(base);
    }
}
