package thaumcraft.entity.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.registry.ModItems;

public class GiantBrainyZombie extends BrainyZombie {
    private static final EntityDataAccessor<Float> DATA_ANGER = SynchedEntityData.defineId(GiantBrainyZombie.class, EntityDataSerializers.FLOAT);

    public GiantBrainyZombie(EntityType<? extends GiantBrainyZombie> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BrainyZombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ANGER, 1.0F);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
    }

    public float getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    private void setAnger(float anger) {
        this.entityData.set(DATA_ANGER, anger);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0F + (anger - 1.0F) * 5.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_ANGER.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.2F + this.getAnger());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getAnger() > 1.0F) {
            this.setAnger(Math.max(1.0F, this.getAnger() - 0.002F));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        this.setAnger(Math.min(2.0F, this.getAnger() + 0.1F));
        return super.hurt(source, damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueOutput output = thaumcraft.compat.ValueOutput.of(tag, this.registryAccess());
        super.addAdditionalSaveData(tag);
        output.putFloat("Anger", this.getAnger());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueInput input = thaumcraft.compat.ValueInput.of(tag, this.registryAccess());
        super.readAdditionalSaveData(tag);
        this.setAnger(input.getFloatOr("Anger", 1.0F));
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        for (int index = 0; index < 12; index++) {
            if (this.random.nextBoolean()) {
                this.spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 2));
            }
        }
        int looting = 0;
        if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity killer) {
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING),
                killer
            );
        }
        if (this.random.nextInt(10) - looting <= 4) {
            this.spawnAtLocation(new ItemStack(ModItems.ZOMBIE_BRAIN.get()), 2.0F);
        }
        if (recentlyHit && this.random.nextInt(200) - looting < 5) {
            ItemStack rare = switch (this.random.nextInt(4)) {
                case 0 -> new ItemStack(ModItems.THAUMIUM_INGOT.get());
                case 1 -> new ItemStack(Items.CARROT);
                case 2 -> new ItemStack(Items.POTATO);
                default -> new ItemStack(ModItems.AMBER.get());
            };
            this.spawnAtLocation(rare, 2.0F);
        }
    }
}
