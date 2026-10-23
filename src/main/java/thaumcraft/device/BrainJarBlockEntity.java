package thaumcraft.device;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class BrainJarBlockEntity extends TcBlockEntity {
    public static final int XP_MAX = 2000;

    private int xp;
    private int eatDelay;
    public float rota;
    public float rotb;
    private float targetAngle;
    private long lastSigh = System.currentTimeMillis() + 1500L;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRAIN_JAR.get(), pos, state);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setEatDelay(int eatDelay) {
        this.eatDelay = eatDelay;
    }

    public int getComparatorSignal() {
        float ratio = (float) xp / XP_MAX;
        return Mth.floor(ratio * 14.0F) + (xp > 0 ? 1 : 0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        if (jar.xp > XP_MAX) {
            jar.xp = XP_MAX;
        }
        if (jar.xp < XP_MAX) {
            ExperienceOrb orb = jar.closestOrb(level);
            if (orb != null && jar.eatDelay == 0) {
                double dx = (pos.getX() + 0.5 - orb.getX()) / 7.0;
                double dy = (pos.getY() + 0.5 - orb.getY()) / 7.0;
                double dz = (pos.getZ() + 0.5 - orb.getZ()) / 7.0;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double pull = 1.0 - distance;
                if (pull > 0.0) {
                    pull *= pull;
                    orb.setDeltaMovement(orb.getDeltaMovement().add(dx / distance * pull * 0.15, dy / distance * pull * 0.33, dz / distance * pull * 0.15));
                }
            }
        }
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
        } else if (jar.xp < XP_MAX) {
            List<ExperienceOrb> orbs = level.getEntitiesOfClass(
                ExperienceOrb.class,
                new AABB(pos.getX() - 0.1, pos.getY() - 0.1, pos.getZ() - 0.1, pos.getX() + 1.1, pos.getY() + 1.1, pos.getZ() + 1.1)
            );
            if (!orbs.isEmpty()) {
                for (ExperienceOrb orb : orbs) {
                    jar.xp += orb.getValue() * orb.count;
                    level.playSound(
                        null,
                        orb,
                        SoundEvents.GENERIC_EAT,
                        SoundSource.BLOCKS,
                        0.1F,
                        (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F
                    );
                    orb.discard();
                }
                jar.sync();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        Entity target = null;
        if (jar.xp < XP_MAX) {
            target = jar.closestOrb(level);
        }
        jar.rotb = jar.rota;
        if (target == null) {
            target = level.getNearestPlayer(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 6.0, false);
            if (target != null && jar.lastSigh < System.currentTimeMillis()) {
                level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    ModSounds.BRAIN.get(),
                    SoundSource.BLOCKS,
                    0.15F,
                    0.8F + level.getRandom().nextFloat() * 0.4F,
                    false
                );
                jar.lastSigh = System.currentTimeMillis() + 5000L + level.getRandom().nextInt(25000);
            }
        }
        if (target != null) {
            double dx = target.getX() - (pos.getX() + 0.5F);
            double dz = target.getZ() - (pos.getZ() + 0.5F);
            jar.targetAngle = (float) Math.atan2(dz, dx);
        } else {
            jar.targetAngle += 0.01F;
        }
        while (jar.rota >= 3.141593F) {
            jar.rota -= 6.283185F;
        }
        while (jar.rota < -3.141593F) {
            jar.rota += 6.283185F;
        }
        while (jar.targetAngle >= 3.141593F) {
            jar.targetAngle -= 6.283185F;
        }
        while (jar.targetAngle < -3.141593F) {
            jar.targetAngle += 6.283185F;
        }
        float difference = jar.targetAngle - jar.rota;
        while (difference >= 3.141593F) {
            difference -= 6.283185F;
        }
        while (difference < -3.141593F) {
            difference += 6.283185F;
        }
        jar.rota += difference * 0.04F;
    }

    private @Nullable ExperienceOrb closestOrb(Level level) {
        double closest = Double.MAX_VALUE;
        ExperienceOrb found = null;
        AABB box = new AABB(worldPosition).inflate(6.0);
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
            double distance = distanceTo(orb.position());
            if (distance < closest) {
                found = orb;
                closest = distance;
            }
        }
        return found;
    }

    private double distanceTo(Vec3 position) {
        double dx = worldPosition.getX() + 0.5 - position.x;
        double dy = worldPosition.getY() + 0.5 - position.y;
        double dz = worldPosition.getZ() + 0.5 - position.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public void releaseAll() {
        if (level == null || level.isClientSide()) {
            return;
        }
        int remaining = xp;
        while (remaining > 0) {
            int split = ExperienceOrb.getExperienceValue(remaining);
            remaining -= split;
            level.addFreshEntity(new ExperienceOrb(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), split));
        }
    }

    public void release(int amount) {
        if (level == null) {
            return;
        }
        int remaining = amount;
        while (remaining > 0) {
            int split = ExperienceOrb.getExperienceValue(remaining);
            remaining -= split;
            level.addFreshEntity(new ExperienceOrb(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, split));
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        releaseAll();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xp = input.getIntOr("XP", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("XP", xp);
    }
}
