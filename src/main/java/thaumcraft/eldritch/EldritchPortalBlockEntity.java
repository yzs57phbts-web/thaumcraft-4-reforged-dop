package thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.EldritchContent;

public final class EldritchPortalBlockEntity extends BlockEntity {
    private int openCount = -1;
    private int counter;

    public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
        super(EldritchContent.ELDRITCH_PORTAL_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchPortalBlockEntity entity) {
        entity.counter++;
        if (level.isClientSide() && entity.openCount < 30) {
            entity.openCount++;
        }
        if (level.isClientSide() && entity.counter % 250 == 0) {
            level.playLocalSound(pos, thaumcraft.registry.ModSounds.EVIL_PORTAL.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        if (level instanceof net.minecraft.server.level.ServerLevel server && entity.counter % 5 == 0) {
            for (net.minecraft.server.level.ServerPlayer player : server.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, new net.minecraft.world.phys.AABB(pos).inflate(0.5, 1.0, 0.5))) {
                EldritchPortalTravel.tryTravel(server, pos, player);
            }
        }
    }

    public int openCount() {
        return this.openCount;
    }
}
