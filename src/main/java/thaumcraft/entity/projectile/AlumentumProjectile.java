package thaumcraft.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.HitResult;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModItems;

public class AlumentumProjectile extends ThrowableItemProjectile {
    public AlumentumProjectile(EntityType<? extends AlumentumProjectile> type, Level level) {
        super(type, level);
    }

    public AlumentumProjectile(Level level, net.minecraft.world.entity.LivingEntity owner) {
        super(thaumcraft.registry.ModEntities.ALUMENTUM_PROJECTILE.get(), owner, level);
        this.setItem(new ItemStack(ModItems.ALUMENTUM.get()));
        thaumcraft.entity.CombatUtils.launch(this, owner, 0.75F);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ALUMENTUM.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        boolean griefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        level.explode(null, this.getX(), this.getY(), this.getZ(), 1.66F,
            griefing ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Fx.get().alumentumTrail(this.level(), this);
        }
    }
}
