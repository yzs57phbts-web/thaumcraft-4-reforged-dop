package thaumcraft.entity.monster.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import javax.annotation.Nullable;

public final class TaintedVillagerRestrictDoorGoal extends Goal {
    private final TaintedVillager villager;
    @Nullable
    private TaintedVillagerIndoorGoal.VillageDoorInfo doorInfo;

    public TaintedVillagerRestrictDoorGoal(TaintedVillager villager) {
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
        if (!(this.villager.level() instanceof ServerLevel level) || TaintedVillagerIndoorGoal.isDay(level, this.villager.blockPosition())) {
            return false;
        }
        this.doorInfo = TaintedVillagerIndoorGoal.nearestVillageDoor(level, this.villager.blockPosition(), 16);
        return this.doorInfo != null && this.villager.distanceToSqr(
            this.doorInfo.insidePos().getX(), this.doorInfo.insidePos().getY(), this.doorInfo.insidePos().getZ()
        ) < 2.25;
    }

    @Override
    public boolean canContinueToUse() {
        return this.doorInfo != null
            && this.villager.level() instanceof ServerLevel level
            && !TaintedVillagerIndoorGoal.isDay(level, this.villager.blockPosition())
            && this.doorInfo.isInside(this.villager.blockPosition());
    }

    @Override
    public void start() {
        ((net.minecraft.world.entity.ai.navigation.GroundPathNavigation) this.villager.getNavigation()).setCanOpenDoors(false);
    }

    @Override
    public void stop() {
        ((net.minecraft.world.entity.ai.navigation.GroundPathNavigation) this.villager.getNavigation()).setCanOpenDoors(true);
        this.doorInfo = null;
    }
}
