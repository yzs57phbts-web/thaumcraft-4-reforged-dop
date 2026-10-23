package thaumcraft.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.registry.ModEntities;

public class DartProjectile extends AbstractArrow {
    private boolean first = true;

    public DartProjectile(EntityType<? extends DartProjectile> type, Level level) {
        super(type, level);
        this.setBaseDamage(1.0);
        this.pickup = Pickup.DISALLOWED;
    }

    public DartProjectile(Level level, LivingEntity shooter, LivingEntity target, float speed, float inaccuracy) {
        this(ModEntities.DART.get(), level);
        this.setOwner(shooter);
        double y = shooter.getEyeY() - 0.1F;
        double dx = target.getX() - shooter.getX();
        double dy = target.getEyeY() - 0.7F - y;
        double dz = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal >= 1.0E-7) {
            this.setPos(shooter.getX() + dx / horizontal / 5.0, y, shooter.getZ() + dz / horizontal / 5.0);
            double lead = horizontal * 0.2;
            this.shoot(dx, dy + lead, dz, speed, inaccuracy);
        }
    }

    @Override
    protected @Nullable ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        if (this.first && this.level().isClientSide()) {
            this.first = false;
            Vec3 movement = this.getDeltaMovement();
            for (int index = 0; index < 5; index++) {
                this.level().addParticle(
                    ParticleTypes.SMOKE,
                    this.getX() - movement.x / 1.5,
                    this.getY() - movement.y / 1.5,
                    this.getZ() - movement.z / 1.5,
                    movement.x / 9.0 + this.random.nextGaussian() * 0.01,
                    movement.y / 9.0 + this.random.nextGaussian() * 0.01,
                    movement.z / 9.0 + this.random.nextGaussian() * 0.01
                );
            }
        }
        super.tick();
    }
}
