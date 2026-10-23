package thaumcraft.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class LongRangeAttackGoal extends RangedAttackGoal {
    private final Mob mob;
    private final double minimumDistanceSquared;

    public LongRangeAttackGoal(RangedAttackMob attacker, double minimumDistance, double speed, int minimumInterval, int maximumInterval, float range) {
        super(attacker, speed, minimumInterval, maximumInterval, range);
        this.mob = (Mob) attacker;
        this.minimumDistanceSquared = minimumDistance * minimumDistance;
    }

    private boolean inRange() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive() && this.mob.distanceToSqr(this.mob.getTarget()) >= this.minimumDistanceSquared;
    }

    @Override
    public boolean canUse() {
        return this.inRange() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.inRange() && super.canContinueToUse();
    }
}
