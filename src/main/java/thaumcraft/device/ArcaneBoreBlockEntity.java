package thaumcraft.device;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RepairableExtended;
import thaumcraft.item.tool.ElementalPickaxeItem;
import thaumcraft.item.tool.SpecialMining;
import thaumcraft.network.BoreDigPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModSounds;
import thaumcraft.vis.VisNet;
import thaumcraft.wand.focus.ExcavationFocus;
import thaumcraft.wand.focus.FocusUpgradeType;

public class ArcaneBoreBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int FOCUS_SLOT = 0;
    public static final int PICKAXE_SLOT = 1;
    public static final int MAX_RADIUS = 2;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int spiral;
    private float currentRadius;
    private float vRadX;
    private float vRadZ;
    private float tRadX;
    private float tRadZ;
    private float mRadX;
    private float mRadZ;
    private int count;
    private int topRotation;
    private long soundDelay;
    private @Nullable Object beam1;
    private @Nullable Object beam2;
    private int beamLength;
    private @Nullable ArcaneBoreBaseBlockEntity base;
    private int rotX;
    private int rotZ;
    private int tarX;
    private int tarZ;
    private int speedX;
    private int speedZ;
    private boolean hasFocus;
    private boolean hasPickaxe;
    private int lastX;
    private int lastY;
    private int lastZ;
    private boolean toDig;
    private int digX;
    private int digY;
    private int digZ;
    private @Nullable BlockState digState;
    private float radInc;
    private int paused = 100;
    private int maxPause = 100;
    private long repairCounter;
    private boolean initialized;
    private Direction orientation = Direction.UP;
    private int fortune;
    private int speed;
    private int area;
    private int blockCount;
    private float speedyTime;
    private boolean derivedDirty = true;
    private AspectList repairCost = new AspectList();
    private AspectList currentRepairVis = new AspectList();
    private @Nullable FakePlayer fakePlayer;

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ARCANE_BORE.get(), pos, state);
    }

    protected ArcaneBoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getOrientation() {
        return orientation;
    }

    public Direction getBaseOrientation() {
        return getBlockState().getValue(ArcaneBoreBlock.BASE);
    }

    public int getRotX() {
        return rotX;
    }

    public int getRotZ() {
        return rotZ;
    }

    public int getSpeedX() {
        return speedX;
    }

    public int getSpeedZ() {
        return speedZ;
    }

    public float getVRadX() {
        return vRadX;
    }

    public float getVRadZ() {
        return vRadZ;
    }

    public int getTopRotation() {
        return topRotation;
    }

    public boolean hasFocus() {
        refresh();
        return hasFocus;
    }

    public boolean hasPickaxe() {
        refresh();
        return hasPickaxe;
    }

    public int getFortune() {
        refresh();
        return fortune;
    }

    public int getSpeed() {
        refresh();
        return speed;
    }

    public int getArea() {
        refresh();
        return area;
    }

    public float getSpeedyTime() {
        return speedyTime;
    }

    public int getBeamLength() {
        return beamLength;
    }

    public int getDigX() {
        return digX;
    }

    public int getDigY() {
        return digY;
    }

    public int getDigZ() {
        return digZ;
    }

    public static boolean isPickaxe(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        derivedDirty = true;
    }

    private void refresh() {
        if (!derivedDirty) {
            return;
        }
        derivedDirty = false;
        fortune = 0;
        area = 0;
        speed = 0;
        ItemStack focus = items.get(FOCUS_SLOT);
        if (focus.getItem() instanceof ExcavationFocus excavation) {
            fortune = excavation.getUpgradeLevel(focus, FocusUpgradeType.TREASURE);
            area = excavation.getUpgradeLevel(focus, FocusUpgradeType.ENLARGE);
            speed += excavation.getUpgradeLevel(focus, FocusUpgradeType.POTENCY);
            hasFocus = true;
        } else {
            hasFocus = false;
        }
        ItemStack pickaxe = items.get(PICKAXE_SLOT);
        if (isPickaxe(pickaxe) && level != null) {
            hasPickaxe = true;
            int pickFortune = ArmorSupport.enchantmentLevel(level.registryAccess(), pickaxe, Enchantments.FORTUNE);
            if (pickFortune > fortune) {
                fortune = pickFortune;
            }
            speed += ArmorSupport.enchantmentLevel(level.registryAccess(), pickaxe, Enchantments.EFFICIENCY);
        } else {
            hasPickaxe = false;
        }
    }

    public boolean hasSilkTouch() {
        refresh();
        ItemStack focus = items.get(FOCUS_SLOT);
        ItemStack pickaxe = items.get(PICKAXE_SLOT);
        boolean pick = level != null && !pickaxe.isEmpty() && ArmorSupport.enchantmentLevel(level.registryAccess(), pickaxe, Enchantments.SILK_TOUCH) > 0;
        return pick || focus.getItem() instanceof ExcavationFocus excavation && excavation.isUpgradedWith(focus, FocusUpgradeType.SILKTOUCH);
    }

    public boolean hasDowsing() {
        ItemStack focus = items.get(FOCUS_SLOT);
        return items.get(PICKAXE_SLOT).getItem() instanceof ElementalPickaxeItem
            || focus.getItem() instanceof ExcavationFocus excavation && excavation.isUpgradedWith(focus, ExcavationFocus.DOWSING);
    }

    private boolean areItemsValid() {
        refresh();
        ItemStack pickaxe = items.get(PICKAXE_SLOT);
        boolean notNearBroken = !(hasPickaxe && pickaxe.getDamageValue() + 1 >= pickaxe.getMaxDamage());
        return hasFocus && hasPickaxe && pickaxe.isDamageableItem() && notNearBroken;
    }

    private boolean gettingPower() {
        return level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.relative(getBaseOrientation().getOpposite()));
    }

    public void setOrientation(Direction direction, boolean initial) {
        applyOrientation(direction, initial);
        setChanged();
    }

    private void applyOrientation(Direction direction, boolean initial) {
        orientation = direction;
        lastX = 0;
        lastZ = 0;
        switch (direction) {
            case DOWN -> {
                tarZ = 180;
                tarX = 0;
            }
            case UP -> {
                tarZ = 0;
                tarX = 0;
            }
            case NORTH -> {
                tarZ = 90;
                tarX = 270;
            }
            case SOUTH -> {
                tarZ = 90;
                tarX = 90;
            }
            case WEST -> {
                tarZ = 90;
                tarX = 0;
            }
            case EAST -> {
                tarZ = 90;
                tarX = 180;
            }
        }
        if (initial) {
            rotX = tarX;
            rotZ = tarZ;
        }
        toDig = false;
        radInc = 0.0F;
        paused = 100;
        tRadX = 0.0F;
        tRadZ = 0.0F;
        mRadX = 0.0F;
        mRadZ = 0.0F;
        digX = 0;
        digY = 0;
        digZ = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        bore.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        if (!level.isClientSide() && speedyTime < 20.0F) {
            speedyTime += VisNet.drainVis(level, pos, Aspect.ENTROPY, 100) / 5.0F;
            if (speedyTime < 20.0F && base != null && base.drawEssentia()) {
                speedyTime += 20.0F;
            }
        }
        if (rotX < tarX) {
            rotX += speedX;
            if (rotX < tarX) {
                speedX++;
            } else {
                speedX = (int) (speedX / 3.0F);
            }
        } else if (rotX > tarX) {
            rotX += speedX;
            if (rotX > tarX) {
                speedX--;
            } else {
                speedX = (int) (speedX / 3.0F);
            }
        } else {
            speedX = 0;
        }
        if (rotZ < tarZ) {
            rotZ += speedZ;
            if (rotZ < tarZ) {
                speedZ++;
            } else {
                speedZ = (int) (speedZ / 3.0F);
            }
        } else if (rotZ > tarZ) {
            rotZ += speedZ;
            if (rotZ > tarZ) {
                speedZ--;
            } else {
                speedZ = (int) (speedZ / 3.0F);
            }
        } else {
            speedZ = 0;
        }
        if (gettingPower() && areItemsValid()) {
            if (level instanceof ServerLevel serverLevel) {
                digServer(serverLevel, pos);
            } else {
                digClient(level, pos);
            }
        } else if (level.isClientSide()) {
            idleClient();
        }
        if (level instanceof ServerLevel serverLevel && hasPickaxe && !items.get(PICKAXE_SLOT).isEmpty()) {
            repair(serverLevel);
        }
    }

    private void idleClient() {
        if (topRotation % 90 != 0) {
            topRotation += Math.min(10, 90 - topRotation % 90);
        }
        vRadX *= 0.9F;
        vRadZ *= 0.9F;
    }

    private void repair(ServerLevel serverLevel) {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(UUID.nameUUIDFromBytes("FakeThaumcraftBore".getBytes()), "FakeThaumcraftBore"));
        }
        ItemStack pickaxe = items.get(PICKAXE_SLOT);
        if (repairCounter++ % 40L == 0L && pickaxe.isDamaged()) {
            doRepair(serverLevel, pickaxe, fakePlayer);
        }
        if (repairCost.size() > 0 && repairCounter % 5L == 0L) {
            for (Aspect aspect : repairCost.getAspects()) {
                if (currentRepairVis.getAmount(aspect) < repairCost.getAmount(aspect)) {
                    currentRepairVis.add(aspect, VisNet.drainVis(serverLevel, worldPosition, aspect, repairCost.getAmount(aspect)));
                }
            }
        }
        fakePlayer.tickCount = (int) repairCounter;
        pickaxe.inventoryTick(serverLevel, fakePlayer, PICKAXE_SLOT, false);
    }

    private void doRepair(ServerLevel serverLevel, ItemStack stack, Player player) {
        int enchant = Math.min(2, ArmorSupport.enchantmentLevel(serverLevel.registryAccess(), stack, ModEnchantments.REPAIR));
        if (enchant <= 0) {
            return;
        }
        if (!(stack.getItem() instanceof Repairable)) {
            repairCost = new AspectList();
            return;
        }
        AspectList tags = AspectHelper.getObjectTags(stack);
        if (tags == null || tags.size() == 0) {
            return;
        }
        AspectList primals = AspectHelper.reduceToPrimals(tags);
        for (Aspect aspect : primals.getAspects()) {
            repairCost.merge(aspect, (int) Math.sqrt(primals.getAmount(aspect) * 2) * enchant);
        }
        boolean allowed = !(stack.getItem() instanceof RepairableExtended extended) || extended.doRepair(stack, player, enchant);
        if (allowed) {
            for (Aspect aspect : repairCost.getAspects()) {
                if (currentRepairVis.getAmount(aspect) < repairCost.getAmount(aspect)) {
                    allowed = false;
                    break;
                }
            }
        }
        if (allowed) {
            for (Aspect aspect : repairCost.getAspects()) {
                currentRepairVis.reduce(aspect, repairCost.getAmount(aspect));
            }
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - enchant));
            setChanged();
        }
    }

    private ItemStack harvestTool(ServerLevel serverLevel, int treasure, boolean silk) {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        var enchantments = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(tool, mutable -> {
            if (silk) {
                enchantments.get(Enchantments.SILK_TOUCH).ifPresent(holder -> mutable.set(holder, 1));
            }
            if (treasure > 0) {
                enchantments.get(Enchantments.FORTUNE).ifPresent(holder -> mutable.set(holder, treasure));
            }
        });
        return tool;
    }

    private void digServer(ServerLevel level, BlockPos pos) {
        if (rotX != tarX || rotZ != tarZ) {
            return;
        }
        boolean dug = false;
        if (base == null || base.isRemoved()) {
            base = level.getBlockEntity(pos.relative(getBaseOrientation().getOpposite())) instanceof ArcaneBoreBaseBlockEntity found ? found : null;
        }
        if (--count > 0) {
            return;
        }
        if (toDig) {
            toDig = false;
            BlockPos target = new BlockPos(digX, digY, digZ);
            BlockState targetState = level.getBlockState(target);
            if (!targetState.isAir()) {
                breakBlock(level, pos, target, targetState);
                dug = true;
            }
        }
        findNextBlockToDig(level, pos);
        if (dug && speedyTime > 0.0F) {
            speedyTime--;
        }
    }

    private void breakBlock(ServerLevel level, BlockPos pos, BlockPos target, BlockState targetState) {
        int treasure = fortune;
        boolean wantSilk = hasSilkTouch();
        ItemStack tool = harvestTool(level, treasure, wantSilk);
        BlockEntity targetEntity = targetState.hasBlockEntity() ? level.getBlockEntity(target) : null;
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(targetState, level, target, targetEntity, null, tool));
        boolean silk = wantSilk && drops.stream().anyMatch(drop -> drop.is(targetState.getBlock().asItem()));
        if (silk) {
            treasure = 0;
        }
        ModNetwork.sendToNear(level, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0,
            new BoreDigPayload(pos, target, Block.getId(targetState), true));
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(target).inflate(1.0))) {
            drops.add(entity.getItem().copy());
            entity.discard();
        }
        Direction baseFacing = base != null ? base.getOrientation() : null;
        int offsetX = baseFacing != null ? baseFacing.getStepX() : 0;
        int offsetZ = baseFacing != null ? baseFacing.getStepZ() : 0;
        for (ItemStack drop : drops) {
            ItemStack output = drop.copy();
            if (!silk && hasDowsing()) {
                ItemStack replaced = SpecialMining.find(targetState, drop, 0.2F + treasure * 0.075F, level.getRandom());
                output = replaced;
            }
            if (baseFacing != null) {
                BlockPos inventory = base.getBlockPos().offset(offsetX, 0, offsetZ);
                output = Transfer.insert(level, inventory, baseFacing.getOpposite(), output, false);
            }
            if (!output.isEmpty()) {
                ItemEntity entity = new ItemEntity(
                    level,
                    pos.getX() + 0.5 + offsetX * 0.66,
                    pos.getY() + 0.4 + getBaseOrientation().getOpposite().getStepY(),
                    pos.getZ() + 0.5 + offsetZ * 0.66,
                    output.copy()
                );
                entity.setDeltaMovement(0.075F * offsetX, 0.025F, 0.075F * offsetZ);
                level.addFreshEntity(entity);
            }
        }
        ItemStack pickaxe = items.get(PICKAXE_SLOT);
        pickaxe.hurtAndBreak(1, level, null, item -> {
        });
        if (pickaxe.isEmpty()) {
            items.set(PICKAXE_SLOT, ItemStack.EMPTY);
        }
        setChanged();
        level.removeBlock(target, false);
        if (base != null) {
            placeLamp(level, pos);
        }
    }

    private void placeLamp(ServerLevel level, BlockPos pos) {
        Block lamp = ModBlocks.ARCANE_LAMP.get();
        Block light = ModBlocks.ARCANE_LAMP_LIGHT.get();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(base.getBlockPos().relative(side)).is(lamp)) {
                int distance = level.getRandom().nextInt(32) * 2;
                int x = pos.getX() + orientation.getStepX() + orientation.getStepX() * distance;
                int y = pos.getY() + orientation.getStepY() + orientation.getStepY() * distance;
                int z = pos.getZ() + orientation.getStepZ() + orientation.getStepZ() * distance;
                int phase = distance / 2 % 4;
                if (orientation.getStepX() != 0) {
                    z += phase == 0 ? 3 : phase != 1 && phase != 3 ? -3 : 0;
                } else {
                    x += phase == 0 ? 3 : phase != 1 && phase != 3 ? -3 : 0;
                }
                if (phase == 3 && orientation.getStepY() == 0) {
                    y -= 2;
                }
                BlockPos target = new BlockPos(x, y, z);
                if (level.getBlockState(target).isAir() && !level.getBlockState(target).is(light) && level.getMaxLocalRawBrightness(target) < 15) {
                    level.setBlock(target, light.defaultBlockState(), Block.UPDATE_ALL);
                }
                break;
            }
        }
    }

    private void findNextBlockToDig(ServerLevel level, BlockPos pos) {
        refresh();
        if (radInc == 0.0F) {
            radInc = (MAX_RADIUS + area) / 360.0F;
        }
        int x = lastX;
        int y = lastY;
        int z = lastZ;
        while (x == lastX && z == lastZ && y == lastY) {
            spiral += 2;
            if (spiral >= 360) {
                spiral -= 360;
            }
            currentRadius += radInc;
            if (currentRadius > MAX_RADIUS + area || currentRadius < -(MAX_RADIUS + area)) {
                radInc *= -1.0F;
            }
            double vx = 0.0;
            double vy = currentRadius;
            double vz = 0.0;
            float angle = spiral / 180.0F * (float) Math.PI;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            double nx = vx * cos + vy * sin;
            double ny = vy * cos - vx * sin;
            vx = nx;
            vy = ny;
            angle = (float) (Math.PI / 2) * orientation.getStepX();
            cos = Mth.cos(angle);
            sin = Mth.sin(angle);
            nx = vx * cos + vz * sin;
            double nz = vz * cos - vx * sin;
            vx = nx;
            vz = nz;
            angle = (float) (Math.PI / 2) * orientation.getStepY();
            cos = Mth.cos(angle);
            sin = Mth.sin(angle);
            ny = vy * cos + vz * sin;
            nz = vz * cos - vy * sin;
            vy = ny;
            vz = nz;
            x = Mth.floor(pos.getX() + orientation.getStepX() + 0.5 + vx);
            y = Mth.floor(pos.getY() + orientation.getStepY() + 0.5 + vy);
            z = Mth.floor(pos.getZ() + orientation.getStepZ() + 0.5 + vz);
        }
        lastX = x;
        lastZ = z;
        lastY = y;
        x += orientation.getStepX();
        y += orientation.getStepY();
        z += orientation.getStepZ();
        for (int depth = 0; depth < 64; depth++) {
            x += orientation.getStepX();
            y += orientation.getStepY();
            z += orientation.getStepZ();
            BlockPos probe = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(probe);
            if (state.getDestroySpeed(level, probe) < 0.0F) {
                break;
            }
            if (!state.isAir() && !state.getCollisionShape(level, probe).isEmpty()) {
                digX = x;
                digY = y;
                digZ = z;
                if (++blockCount > 2) {
                    blockCount = 0;
                }
                count = delay(state.getDestroySpeed(level, probe));
                toDig = true;
                Vec3 from = new Vec3(pos.getX() + 0.5 + orientation.getStepX(), pos.getY() + 0.5 + orientation.getStepY(), pos.getZ() + 0.5 + orientation.getStepZ());
                Vec3 to = new Vec3(digX + 0.5, digY + 0.5, digZ + 0.5);
                BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos hitPos = hit.getBlockPos();
                    BlockState hitState = level.getBlockState(hitPos);
                    if (hitState.getDestroySpeed(level, hitPos) > -1.0F && !hitState.getCollisionShape(level, hitPos).isEmpty()) {
                        count = delay(hitState.getDestroySpeed(level, hitPos));
                        digX = hitPos.getX();
                        digY = hitPos.getY();
                        digZ = hitPos.getZ();
                    }
                }
                sendDigEvent(level, pos);
                break;
            }
        }
    }

    private int delay(float hardness) {
        int ticks = Math.max(10 - speed, (int) (hardness * 2.0F) - speed * 2);
        return speedyTime < 1.0F ? ticks * 4 : ticks;
    }

    private void sendDigEvent(ServerLevel level, BlockPos pos) {
        BlockPos target = new BlockPos(digX, digY, digZ);
        ModNetwork.sendToNear(level, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0,
            new BoreDigPayload(pos, target, Block.getId(level.getBlockState(target)), false));
    }

    private void digClient(Level level, BlockPos pos) {
        if (rotX != tarX || rotZ != tarZ) {
            if (topRotation % 90 != 0) {
                topRotation += Math.min(10, 90 - topRotation % 90);
            }
            vRadX *= 0.9F;
            vRadZ *= 0.9F;
            return;
        }
        refresh();
        RandomSource random = level.getRandom();
        paused++;
        if (paused < maxPause && soundDelay < System.currentTimeMillis()) {
            soundDelay = System.currentTimeMillis() + 1200L + random.nextInt(100);
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.RUMBLE.get(), SoundSource.BLOCKS, 0.25F, 0.9F + random.nextFloat() * 0.2F, false);
        }
        if (beamLength > 0 && paused > maxPause) {
            beamLength--;
        }
        if (toDig) {
            paused = 0;
            beamLength = 64;
            BlockPos target = new BlockPos(digX, digY, digZ);
            maxPause = 10 + Math.max(10 - speed, (int) ((digState == null ? level.getBlockState(target) : digState).getDestroySpeed(level, target) * 2.0F) - speed * 2);
            if (speedyTime <= 0.0F) {
                maxPause *= 4;
            }
            toDig = false;
            double xd = pos.getX() + 0.5 - (digX + 0.5);
            double yd = pos.getY() + 0.5 - (digY + 0.5);
            double zd = pos.getZ() + 0.5 - (digZ + 0.5);
            double horizontal = Mth.sqrt((float) (xd * xd + zd * zd));
            float rx = (float) (Math.atan2(zd, xd) * 180.0 / Math.PI);
            float rz = (float) -(Math.atan2(yd, horizontal) * 180.0 / Math.PI) + 90.0F;
            tRadX = Mth.wrapDegrees((float) rotX) + rx;
            if (orientation == Direction.EAST) {
                if (tRadX > 180.0F) {
                    tRadX -= 360.0F;
                }
                if (tRadX < -180.0F) {
                    tRadX += 360.0F;
                }
            }
            tRadZ = rz - rotZ;
            if (orientation.get3DDataValue() <= 1) {
                tRadZ += 180.0F;
                if (vRadX - tRadX >= 180.0F) {
                    vRadX -= 360.0F;
                }
                if (vRadX - tRadX <= -180.0F) {
                    vRadX += 360.0F;
                }
            }
            mRadX = Math.abs((vRadX - tRadX) / 6.0F);
            mRadZ = Math.abs((vRadZ - tRadZ) / 6.0F);
            if (speedyTime > 0.0F) {
                speedyTime--;
            }
        }
        if (paused < maxPause) {
            if (vRadX < tRadX) {
                vRadX += mRadX;
            } else if (vRadX > tRadX) {
                vRadX -= mRadX;
            }
            if (vRadZ < tRadZ) {
                vRadZ += mRadZ;
            } else if (vRadZ > tRadZ) {
                vRadZ -= mRadZ;
            }
        } else {
            vRadX *= 0.9F;
            vRadZ *= 0.9F;
        }
        mRadX *= 0.9F;
        mRadZ *= 0.9F;
        float vx = rotX + 90 - vRadX;
        float vz = rotZ + 90 - vRadZ;
        float dX = Mth.sin(vx / 180.0F * (float) Math.PI) * Mth.cos(vz / 180.0F * (float) Math.PI);
        float dZ = Mth.cos(vx / 180.0F * (float) Math.PI) * Mth.cos(vz / 180.0F * (float) Math.PI);
        float dY = Mth.sin(vz / 180.0F * (float) Math.PI);
        Vec3 start = new Vec3(pos.getX() + 0.5 + dX, pos.getY() + 0.5 + dY, pos.getZ() + 0.5 + dZ);
        Vec3 end = new Vec3(pos.getX() + 0.5 + dX * beamLength, pos.getY() + 0.5 + dY * beamLength, pos.getZ() + 0.5 + dZ * beamLength);
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        int impact = 0;
        double bx = end.x;
        double by = end.y;
        double bz = end.z;
        if (hit.getType() == HitResult.Type.BLOCK) {
            bx = hit.getLocation().x;
            by = hit.getLocation().y;
            bz = hit.getLocation().z;
            impact = 5;
            BlockPos hitPos = hit.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);
            if (!hitState.isAir()) {
                MachineFx.get().boreDig(level, hitPos, pos.relative(orientation), hitState);
            }
        }
        topRotation += beamLength / 6;
        beam1 = MachineFx.get().boreBeam(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bx, by, bz, 1, 65382, true, impact > 0 ? 2.0F : 0.0F, beam1, impact);
        beam2 = MachineFx.get().boreBeam(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bx, by, bz, 2, 16746581, false, impact > 0 ? 2.0F : 0.0F, beam2, impact);
    }

    public void receiveDig(BlockPos target, int stateId, boolean broken) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        BlockState state = Block.stateById(stateId);
        if (broken) {
            if (!state.isAir()) {
                blockBrokenFx(level, target, state);
            }
            digState = null;
        } else {
            digX = target.getX();
            digY = target.getY();
            digZ = target.getZ();
            toDig = true;
            digState = state;
        }
    }

    private void blockBrokenFx(Level level, BlockPos dig, BlockState broken) {
        var sound = broken.getSoundType();
        level.playLocalSound(dig.getX() + 0.5F, dig.getY() + 0.5F, dig.getZ() + 0.5F, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
        for (int index = 0; index < MachineFx.get().particleCount(10); index++) {
            MachineFx.get().boreDig(level, dig, worldPosition.relative(orientation), broken);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcaneBoreMenu(containerId, inventory, worldPosition);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == FOCUS_SLOT ? stack.getItem() instanceof ExcavationFocus : isPickaxe(stack);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        speedyTime = input.getIntOr("SpeedyTime", 0);
        Direction loaded = Direction.from3DDataValue(input.getIntOr("orientation", Direction.UP.get3DDataValue()));
        boolean changed = loaded != orientation;
        if (!initialized) {
            initialized = true;
            applyOrientation(loaded, true);
        } else if (changed) {
            applyOrientation(loaded, false);
        }
        derivedDirty = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
        output.putInt("SpeedyTime", (int) speedyTime);
        output.putInt("orientation", orientation.get3DDataValue());
    }
}
