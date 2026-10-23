package thaumcraft.entity.golem;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.item.golem.GolemPartItem;
import thaumcraft.menu.TravelingTrunkMenu;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModItems;

public class TravelingTrunk extends Mob implements MenuProvider {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(TravelingTrunk.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(TravelingTrunk.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(TravelingTrunk.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(TravelingTrunk.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TravelingTrunk.class, EntityDataSerializers.INT);
    private SimpleContainer inventory = new SimpleContainer(27);
    private int viewers;
    private int jumpDelay;
    private int attackDelay;
    private int eatDelay;
    private float lidRotation;
    private float previousLidRotation;

    public TravelingTrunk(EntityType<? extends TravelingTrunk> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        jumpDelay = random.nextInt(20) + 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPEN, false);
        builder.define(STAY, false);
        builder.define(OWNER, "");
        builder.define(UPGRADE, -1);
        builder.define(ANGER, 0);
    }

    public SimpleContainer inventory() {
        return inventory;
    }

    public int getUpgrade() {
        return entityData.get(UPGRADE);
    }

    public void setUpgrade(int upgrade) {
        entityData.set(UPGRADE, upgrade);
        setInvSize();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor == UPGRADE && inventory != null) setInvSize();
    }

    public int getRows() {
        return getUpgrade() == 1 ? 4 : 3;
    }

    public int getAnger() {
        return entityData.get(ANGER);
    }

    private void setAnger(int anger) {
        entityData.set(ANGER, Mth.clamp(anger, 0, 32767));
    }

    public float getLidOpen(float partialTick) {
        return Mth.lerp(partialTick, previousLidRotation, lidRotation);
    }

    public boolean isOpen() {
        return entityData.get(OPEN);
    }

    public void setOpen(boolean open) {
        viewers = open ? viewers + 1 : Math.max(0, viewers - 1);
        entityData.set(OPEN, viewers > 0);
    }

    public boolean getStay() {
        return entityData.get(STAY);
    }

    public void setStay(boolean stay) {
        entityData.set(STAY, stay);
    }

    public @Nullable UUID getOwnerUUID() {
        return entityData.get(OWNER).isEmpty() ? null : UUID.fromString(entityData.get(OWNER));
    }

    public void setOwnerUUID(@Nullable UUID owner) {
        entityData.set(OWNER, owner == null ? "" : owner.toString());
    }

    public boolean canOpen(Player player) {
        if (!isAlive() || distanceToSqr(player) > 64.0) return false;
        return getUpgrade() != 3 || getOwnerUUID() != null && getOwnerUUID().equals(player.getUUID());
    }

    public void setInvSize() {
        int size = getRows() * 9;
        if (inventory.getContainerSize() == size) return;
        SimpleContainer resized = new SimpleContainer(size);
        for (int slot = 0; slot < Math.min(inventory.getContainerSize(), size); slot++) resized.setItem(slot, inventory.getItem(slot));
        inventory = resized;
    }

    public ItemStack toItem(boolean dismantle) {
        ItemStack stack = new ItemStack(ModItems.TRAVELING_TRUNK.get());
        if (!dismantle) thaumcraft.entity.golem.GolemKeys.TRUNK_UPGRADE.set(stack, getUpgrade());
        if (getUpgrade() == 4 && !dismantle) {
            thaumcraft.entity.golem.GolemKeys.TRUNK_CONTENTS.set(stack, ItemContainerContents.fromItems(inventory.getItems()));
            inventory.clearContent();
        } else {
            dropInventory();
        }
        if (!dismantle && hasCustomName()) stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        return stack;
    }

    public void restore(ItemStack stack) {
        setUpgrade(thaumcraft.entity.golem.GolemKeys.TRUNK_UPGRADE.getOrDefault(stack, -1));
        ItemContainerContents contents = thaumcraft.entity.golem.GolemKeys.TRUNK_CONTENTS.getOrDefault(stack, ItemContainerContents.EMPTY);
        contents.copyInto(inventory.getItems());
        if (stack.has(DataComponents.CUSTOM_NAME)) setCustomName(stack.get(DataComponents.CUSTOM_NAME));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof thaumcraft.item.golem.GolemBellItem) return getUpgrade() == 3 && !player.getUUID().equals(getOwnerUUID()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (stack.getItem() instanceof GolemPartItem part && part.kind() == GolemPartItem.Kind.UPGRADE && getUpgrade() == -1) {
            if (!level().isClientSide()) {
                setUpgrade(part.index());
                stack.shrink(1);
                level().playSound(null, blockPosition(), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(Thaumcraft.id("upgrade")), SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null && getHealth() < getMaxHealth()) {
            if (!level().isClientSide()) {
                stack.shrink(1);
                heal(food.nutrition());
                level().broadcastEntityEvent(this, (byte) 18);
            }
            return InteractionResult.SUCCESS;
        }
        if (getUpgrade() == 3 && !player.getUUID().equals(getOwnerUUID())) return InteractionResult.SUCCESS;
        if (!level().isClientSide() && player instanceof ServerPlayer server) Menus.openEntity(server, this, getId());
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TravelingTrunkMenu(id, playerInventory, getId());
    }

    @Override
    public void tick() {
        super.tick();
        if (isInWater()) setDeltaMovement(getDeltaMovement().add(0, 0.033F, 0));
        if (level().isClientSide()) {
            animateLid();
            return;
        }
        if (getHealth() < getMaxHealth() && (getUpgrade() == 3 || tickCount % 50 == 0)) heal(1.0F);
        if (eatDelay > 0) eatDelay--;
        if (getAnger() > 0) setAnger(getAnger() - 1);
        if (getUpgrade() == 5) pullItems((ServerLevel) level());
        Entity owner = resolveOwner();
        if (!(owner instanceof LivingEntity livingOwner)) return;
        if (!getStay() && getUpgrade() == 2 && getAnger() == 0 && getTarget() == null) {
            LivingEntity recentTarget = livingOwner.getLastHurtByMob();
            if (recentTarget != null && recentTarget.isAlive() && recentTarget != livingOwner && hasLineOfSight(recentTarget)) {
                setAnger(600);
                setTarget(recentTarget);
            }
        }
        LivingEntity target = getTarget();
        boolean moving = false;
        if (getAnger() > 0 && target != null && target.isAlive() && target != livingOwner) {
            lookAt(target, 10, 20);
            getNavigation().moveTo(target, 1);
            moving = true;
            if (attackDelay <= 0
                && distanceTo(target) < 1.5F
                && target.getBoundingBox().maxY > getBoundingBox().minY
                && target.getBoundingBox().minY < getBoundingBox().maxY) {
                attackDelay = 10 + random.nextInt(5);
                target.hurt(damageSources().mobAttack(this), 4.0F);
                level().broadcastEntityEvent(this, (byte) 17);
                playSound(SoundEvents.BLAZE_HURT, 0.5F, random.nextFloat() * 0.1F + 0.9F);
            }
        } else if (target != null && !target.isAlive()) {
            setTarget(null);
            setAnger(5);
        }
        if (attackDelay > 0) attackDelay--;
        if (!getStay() && getAnger() == 0 && distanceTo(livingOwner) > 5.0F) {
            lookAt(livingOwner, 10, 20);
            getNavigation().moveTo(livingOwner, (getUpgrade() == 0 ? 1.25 : 1));
            moving = true;
        }
        if (!getStay() && distanceTo(livingOwner) > 20.0F && teleportNear(livingOwner)) return;
        if (moving && (onGround() || isInWater()) && jumpDelay-- <= 0) {
            jumpDelay = (random.nextInt(10) + 5) / 3;
            jumpFromGround();
            Vec3 movement = getDeltaMovement();
            double speed = getUpgrade() == 0 ? 0.04 : 0.03;
            double forward = getUpgrade() == 0 ? 8.0 : 6.0;
            if (isInWater()) forward *= 0.75;
            setDeltaMovement(movement.x + getLookAngle().x * forward * speed, 0.3, movement.z + getLookAngle().z * forward * speed);
            playSound(SoundEvents.CHEST_CLOSE, 0.1F, random.nextFloat() * 0.1F + 0.9F);
        } else if (!moving && onGround()) {
            getNavigation().stop();
        }
    }

    private void animateLid() {
        previousLidRotation = lidRotation;
        if (!onGround() && getDeltaMovement().y < 0 && !isInWater()) lidRotation += 0.015F;
        if ((onGround() || isInWater()) && !isOpen()) lidRotation = Math.max(0, lidRotation - 0.1F);
        if (isOpen()) lidRotation += 0.035F;
        lidRotation = Mth.clamp(lidRotation, 0, isOpen() ? 0.5F : 0.2F);
    }

    private @Nullable Entity resolveOwner() {
        UUID owner = getOwnerUUID();
        return owner == null ? null : level().getPlayerByUUID(owner);
    }

    private boolean teleportNear(LivingEntity owner) {
        BlockPos base = owner.blockPosition().offset(-2, 0, -2);
        for (int x = 0; x <= 4; x++) for (int z = 0; z <= 4; z++) {
            if (x > 0 && z > 0 && x < 4 && z < 4) continue;
            BlockPos pos = base.offset(x, 0, z);
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                BlockPos candidate = pos.above(yOffset);
                if (!level().hasChunkAt(candidate)) continue;
                BlockState floor = level().getBlockState(candidate.below());
                boolean supported = floor.isFaceSturdy(level(), candidate.below(), net.minecraft.core.Direction.UP)
                    || floor.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
                if (!supported || !level().isEmptyBlock(candidate) || !level().isEmptyBlock(candidate.above())) continue;
                level().playSound(null, candidate, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                setPos(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
                getNavigation().stop();
                setTarget(null);
                return true;
            }
        }
        return false;
    }

    private void pullItems(ServerLevel server) {
        AABB pickup = getBoundingBox().inflate(0.5);
        for (net.minecraft.world.entity.item.ItemEntity item : server.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, pickup)) {
            ItemStack remaining = insert(item.getItem());
            if (remaining.getCount() != item.getItem().getCount()) {
                item.setItem(remaining);
                playSound(SoundEvents.CHEST_OPEN, 0.5F, random.nextFloat() * 0.5F + 0.5F);
                server.broadcastEntityEvent(this, (byte) 17);
            }
        }
        AABB suction = getBoundingBox().inflate(3.0);
        for (net.minecraft.world.entity.item.ItemEntity item : server.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, suction)) {
            double dx = item.getX() - getX();
            double dy = item.getY() - getY() + getBbHeight() * 0.8F;
            double dz = item.getZ() - getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0) item.setDeltaMovement(item.getDeltaMovement().subtract(dx / distance * 0.075, dy / distance * 0.075, dz / distance * 0.075));
        }
    }

    private ItemStack insert(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, remaining)) {
                int amount = Math.min(remaining.getCount(), Math.min(current.getMaxStackSize(), inventory.getMaxStackSize()) - current.getCount());
                if (amount > 0) {
                    current.grow(amount);
                    remaining.shrink(amount);
                    inventory.setChanged();
                }
            }
        }
        for (int slot = 0; slot < inventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                int amount = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), inventory.getMaxStackSize()));
                inventory.setItem(slot, remaining.copyWithCount(amount));
                remaining.shrink(amount);
            }
        }
        return remaining;
    }

    private void dropInventory() {
        if (level() instanceof ServerLevel server) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty()) spawnAtLocation(stack);
            }
        }
        inventory.clearContent();
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS) || getUpgrade() == 3) return false;
        return super.hurt(source, damage);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) spawnAtLocation(stack);
        }
        inventory.clearContent();
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueOutput output = ValueOutput.of(tag, registryAccess());
        super.addAdditionalSaveData(tag);
        output.putBoolean("Stay", getStay());
        output.putInt("Upgrade", getUpgrade());
        output.putInt("Anger", getAnger());
        if (getOwnerUUID() != null) output.store("Owner", UUIDUtil.CODEC, getOwnerUUID());
        output.store("Inventory", ItemContainerContents.CODEC, ItemContainerContents.fromItems(inventory.getItems()));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueInput input = ValueInput.of(tag, registryAccess());
        super.readAdditionalSaveData(tag);
        setStay(input.getBooleanOr("Stay", false));
        setUpgrade(input.getIntOr("Upgrade", -1));
        setAnger(input.getIntOr("Anger", 0));
        setOwnerUUID(input.read("Owner", UUIDUtil.CODEC).orElse(null));
        input.read("Inventory", ItemContainerContents.CODEC).ifPresent(contents -> contents.copyInto(inventory.getItems()));
    }

    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("block.wood.hit"));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.ITEM_BREAK;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 17) lidRotation = 0.15F;
        else if (id == 18) {
            lidRotation = 0.15F;
            for (int index = 0; index < 7; index++) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.HEART, getX() + random.nextDouble() * getBbWidth() * 2 - getBbWidth(), getY() + 0.5 + random.nextDouble() * getBbHeight(), getZ() + random.nextDouble() * getBbWidth() * 2 - getBbWidth(), random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
        } else super.handleEntityEvent(id);
    }
}
