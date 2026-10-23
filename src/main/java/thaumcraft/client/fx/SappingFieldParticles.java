package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;

/** A visible potion trail over the Warden's weakness fields. */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class SappingFieldParticles {
    private SappingFieldParticles() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || minecraft.player == null || minecraft.isPaused() || level.getGameTime() % 5 != 0) return;
        BlockPos center = minecraft.player.blockPosition();
        var spiral = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFF0CE37);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-12, -3, -12), center.offset(12, 3, 12))) {
            if (!level.getBlockState(pos).is(ModBlocks.SAPPING_FIELD.get())) continue;
            double x = pos.getX() + 0.2 + level.random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.2 + level.random.nextDouble() * 0.6;
            level.addParticle(spiral, x, pos.getY() + 0.25, z, 0.0, 0.025, 0.0);
        }
    }
}
