package thaumcraft.entity.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.EldritchContent;
import thaumcraft.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public abstract class ThaumcraftBoss extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(ThaumcraftBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPAWN_TIMER = SynchedEntityData.defineId(ThaumcraftBoss.class, EntityDataSerializers.INT);
    private static final AttributeModifier[] HEALTH_SCALING = new AttributeModifier[5];
    private static final AttributeModifier[] DAMAGE_SCALING = new AttributeModifier[5];

    static {
        for (int index = 0; index < 5; index++) {
            HEALTH_SCALING[index] = new AttributeModifier(Thaumcraft.id("boss/health_" + index), 50.0, AttributeModifier.Operation.ADD_VALUE);
            DAMAGE_SCALING[index] = new AttributeModifier(Thaumcraft.id("boss/damage_" + index), 0.5, AttributeModifier.Operation.ADD_VALUE);
        }
    }
    private final ServerBossEvent bossEvent;
    private final Map<Integer, Integer> aggro = new HashMap<>();
    private int spawnTimer;

    protected ThaumcraftBoss(EntityType<? extends ThaumcraftBoss> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.xpReward = 50;
        this.setCanPickUpLoot(false);
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), color, BossEvent.BossBarOverlay.PROGRESS);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 0.95).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGER, 0);
        builder.define(DATA_SPAWN_TIMER, 0);
    }

    public int getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(DATA_ANGER, Math.max(0, Math.min(Short.MAX_VALUE, anger)));
    }

    public int getSpawnTimer() {
        return this.entityData.get(DATA_SPAWN_TIMER);
    }

    public void setSpawnTimer(int ticks) {
        this.spawnTimer = Math.max(0, ticks);
        this.entityData.set(DATA_SPAWN_TIMER, this.spawnTimer);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
        if (this.spawnTimer > 0) {
            this.spawnTimer--;
            this.entityData.set(DATA_SPAWN_TIMER, this.spawnTimer);
        }
        if (this.getAnger() > 0) {
            this.setAnger(this.getAnger() - 1);
        }
        if (this.tickCount % 30 == 0) {
            this.heal(1.0F);
        }
        this.bossEvent.setName(this.getDisplayName());
        this.bossEvent.setProgress(this.getMaxHealth() <= 0.0F ? 0.0F : this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 20 == 0 && this.getTarget() != null) {
            this.updateAggroTargetAndScaling();
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();
        if (this.getSpawnTimer() <= 0) {
            super.customServerAiStep();
        }
    }

    protected boolean shouldPauseBossGoals() {
        return this.getSpawnTimer() > 0;
    }

    @Override
    protected void serverAiStep() {
        if (!this.shouldPauseBossGoals()) {
            super.serverAiStep();
        }
    }

    private void updateAggroTargetAndScaling() {
        LivingEntity current = this.getTarget();
        if (current == null) {
            return;
        }
        ArrayList<Integer> invalid = new ArrayList<>();
        int currentId = current.getId();
        int currentAggro = this.aggro.getOrDefault(currentId, 0);
        int highestAggro = currentAggro;
        int selectedId = currentId;
        LivingEntity selected = null;
        int playerCount = 0;
        for (Map.Entry<Integer, Integer> entry : this.aggro.entrySet()) {
            int challengerId = entry.getKey();
            int threat = entry.getValue();
            if (threat > currentAggro + 25 && threat > currentAggro * 1.1 && threat > highestAggro) {
                LivingEntity oldTarget = this.level().getEntity(selectedId) instanceof LivingEntity living ? living : null;
                if (oldTarget != null && oldTarget.isAlive() && this.distanceToSqr(oldTarget) <= 16384.0) {
                    selected = oldTarget;
                    selectedId = challengerId;
                    highestAggro = challengerId;
                    if (oldTarget instanceof Player) {
                        playerCount++;
                    }
                } else {
                    invalid.add(challengerId);
                }
            }
        }
        for (int entityId : invalid) {
            this.aggro.remove(entityId);
        }
        if (selected != null && selectedId != currentId) {
            this.setTarget(selected);
        }
        this.bossEvent.setVisible(true);
        float oldMax = this.getMaxHealth();
        float oldHealth = this.getHealth();
        for (int index = 0; index < 5; index++) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_SCALING[index].id());
            this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_SCALING[index].id());
        }
        for (int index = 0; index < Math.min(5, playerCount - 1); index++) {
            this.getAttribute(Attributes.MAX_HEALTH).addOrUpdateTransientModifier(HEALTH_SCALING[index]);
            this.getAttribute(Attributes.ATTACK_DAMAGE).addOrUpdateTransientModifier(DAMAGE_SCALING[index]);
        }
        float multiplier = oldMax == 0.0F ? 0.0F : this.getMaxHealth() / oldMax;
        this.setHealth(oldHealth * multiplier);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity living && this.canAttack(living)) {
            this.aggro.merge(living.getId(), (int) damage, Integer::sum);
        }
        if (damage > 35.0F) {
            if (this.getAnger() == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (damage / 15.0F)));
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) (damage / 40.0F)));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, (int) (damage / 40.0F)));
                this.setAnger(200);
                if (source.getEntity() instanceof ServerPlayer player) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(this.getDisplayName()).append(" ").append(net.minecraft.network.chat.Component.translatable("tc.boss.enrage")));
                }
            }
            damage = 35.0F;
        }
        return super.hurt(source, damage);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.spawnTimer > 0 || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }

    @Override
    protected int increaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return this.spawnTimer <= 0 && super.isPushable();
    }

    @Override
    public boolean isAlliedTo(net.minecraft.world.entity.Entity entity) {
        return entity instanceof thaumcraft.entity.EldritchMob || super.isAlliedTo(entity);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        this.restrictTo(this.blockPosition(), 24);
        return result;
    }

    public void generateName() {
        int modifier = thaumcraft.entity.monster.ChampionModifier.get(this);
        if (modifier >= 0) {
            this.setCustomName(net.minecraft.network.chat.Component.translatable(this.getType().getDescriptionId(), net.minecraft.network.chat.Component.translatable("champion.mod." + thaumcraft.entity.monster.ChampionModifier.name(modifier))));
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.bossEvent.setVisible(false);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(EldritchContent.PRIMORDIAL_PEARL.get()));
        this.spawnAtLocation(new ItemStack(ModItems.RARE_LOOT_BAG.get()));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueOutput output = ValueOutput.of(tag, registryAccess());
        super.addAdditionalSaveData(tag);
        output.putInt("BossSpawnTimer", this.spawnTimer);
        output.putInt("BossAnger", this.getAnger());
        if (this.hasRestriction() && this.getRestrictRadius() > 0) {
            BlockPos pos = this.getRestrictCenter();
            output.putInt("HomeD", (int) this.getRestrictRadius());
            output.putInt("HomeX", pos.getX());
            output.putInt("HomeY", pos.getY());
            output.putInt("HomeZ", pos.getZ());
        }
        output.store("BossAggro", net.minecraft.nbt.CompoundTag.CODEC, this.aggro.entrySet().stream().collect(net.minecraft.nbt.CompoundTag::new, (aggroTag, entry) -> aggroTag.putInt(Integer.toString(entry.getKey()), entry.getValue()), net.minecraft.nbt.CompoundTag::merge));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueInput input = ValueInput.of(tag, registryAccess());
        super.readAdditionalSaveData(tag);
        this.spawnTimer = input.getIntOr("BossSpawnTimer", 0);
        this.entityData.set(DATA_SPAWN_TIMER, this.spawnTimer);
        this.setAnger(input.getIntOr("BossAnger", 0));
        input.getInt("HomeD").ifPresent(radius -> this.restrictTo(new BlockPos(input.getIntOr("HomeX", 0), input.getIntOr("HomeY", 0), input.getIntOr("HomeZ", 0)), radius));
        input.read("BossAggro", net.minecraft.nbt.CompoundTag.CODEC).ifPresent(aggroTag -> aggroTag.getAllKeys().forEach(key -> this.aggro.put(parseId(key), aggroTag.getInt(key))));
    }

    private static int parseId(String key) {
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
