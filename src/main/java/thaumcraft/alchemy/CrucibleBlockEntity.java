package thaumcraft.alchemy;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.fx.Fx;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandInteractable;

public class CrucibleBlockEntity extends TcBlockEntity implements AspectContainer, WandInteractable {
    public static final int MAX_TAGS = 100;
    public static final int BOIL_HEAT = 150;
    public static final int TANK_CAPACITY = 1000;
    public static final int EVENT_SPARKLE = 1;
    public static final int EVENT_BOIL = 2;

    private short heat;
    private AspectList aspects = new AspectList();
    private final Transfer.FluidTank tank = new Transfer.FluidTank(TANK_CAPACITY, stack -> stack.is(Fluids.WATER), true, this::onTankChanged);
    private int bellows = -1;
    private long counter = -100L;
    private int contactDelay;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    protected CrucibleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable Transfer.FluidTank exposedFluids(@Nullable Direction side) {
        return tank;
    }

    private void onTankChanged() {
        if (level != null && !level.isClientSide()) {
            sync();
        } else {
            setChanged();
        }
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = (short) heat;
    }

    public int getWaterAmount() {
        return tank.getAmount();
    }

    public boolean isBoiling() {
        return tank.getAmount() > 0 && heat > BOIL_HEAT;
    }

    public int tagAmount() {
        return aspects.visSize();
    }

    public void fillWater(int amount) {
        int total = Math.min(TANK_CAPACITY, tank.getAmount() + amount);
        tank.setFluid(new FluidStack(Fluids.WATER, total));
    }

    private void drainWater(int amount) {
        int remaining = tank.getAmount() - amount;
        tank.setFluid(remaining > 0 ? new FluidStack(Fluids.WATER, remaining) : FluidStack.EMPTY);
    }

    public void resetBellows() {
        bellows = -1;
    }

    public boolean tickContactDelay() {
        contactDelay++;
        if (contactDelay < 10) {
            return false;
        }
        contactDelay = 0;
        return true;
    }

    public float getFluidHeight() {
        float base = 0.3F + 0.5F * ((float) tank.getAmount() / TANK_CAPACITY);
        float out = base + tagAmount() / 100.0F * (1.0F - base);
        if (out > 1.0F) {
            out = 1.001F;
        }
        if (out == 1.0F) {
            out = 0.9999F;
        }
        return out;
    }

    private boolean isHeatSource(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getFluidState().is(FluidTags.LAVA) || below.is(BlockTags.FIRE)
            || net.minecraft.world.level.block.CampfireBlock.isLitCampfire(below)
            || AlchemyHooks.isNitor(level, pos.below(), below);
    }

    public static void serverTick(Level level, BlockPos pos, CrucibleBlockEntity crucible) {
        crucible.counter++;
        int previousHeat = crucible.heat;
        if (crucible.bellows < 0) {
            crucible.bellows = BellowsBlockEntity.getBellows(level, pos, Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new));
        }
        if (crucible.tank.getAmount() <= 0) {
            if (crucible.heat > 0) {
                crucible.heat--;
            }
        } else if (crucible.isHeatSource(level, pos)) {
            if (crucible.heat < 200) {
                crucible.heat = (short) (crucible.heat + 1 + crucible.bellows * 2);
                if (previousHeat < BOIL_HEAT + 1 && crucible.heat >= BOIL_HEAT + 1) {
                    crucible.sync();
                }
            }
        } else if (crucible.heat > 0) {
            crucible.heat--;
            if (crucible.heat == BOIL_HEAT - 1) {
                crucible.sync();
            }
        }
        if (crucible.tagAmount() > MAX_TAGS && crucible.counter % 5L == 0L) {
            crucible.takeRandomFromSource();
            crucible.spill();
        }
        if (crucible.counter > 100L && crucible.heat > BOIL_HEAT) {
            crucible.counter = 0L;
            if (crucible.tagAmount() > 0) {
                RandomSource random = level.getRandom();
                List<Aspect> list = crucible.aspects.getAspects();
                Aspect aspect = list.get(random.nextInt(list.size()));
                if (aspect.isPrimal()) {
                    aspect = list.get(random.nextInt(list.size()));
                }
                crucible.drainWater(2);
                crucible.aspects.remove(aspect, 1);
                if (!aspect.isPrimal()) {
                    Aspect[] components = aspect.getComponents();
                    crucible.aspects.add(components[random.nextBoolean() ? 0 : 1], 1);
                } else {
                    crucible.spill();
                }
            }
            crucible.sync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, CrucibleBlockEntity crucible) {
        if (crucible.tank.getAmount() > 0) {
            crucible.drawEffects(level, pos);
        }
    }

    private void drawEffects(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (heat > BOIL_HEAT) {
            Fx.get().crucibleFroth(level, x + 0.2F + random.nextFloat() * 0.6F, y + getFluidHeight(), z + 0.2F + random.nextFloat() * 0.6F);
            if (tagAmount() > MAX_TAGS) {
                for (int index = 0; index < 2; index++) {
                    Fx.get().crucibleFrothDown(level, x, y + 1, z + random.nextFloat());
                    Fx.get().crucibleFrothDown(level, x + 1, y + 1, z + random.nextFloat());
                    Fx.get().crucibleFrothDown(level, x + random.nextFloat(), y + 1, z);
                    Fx.get().crucibleFrothDown(level, x + random.nextFloat(), y + 1, z + 1);
                }
            }
        }
        if (random.nextInt(6) == 0 && aspects.size() > 0) {
            List<Aspect> list = aspects.getAspects();
            int color = list.get(random.nextInt(list.size())).getColor();
            int offsetX = 5 + random.nextInt(22);
            int offsetZ = 5 + random.nextInt(22);
            Fx.get().crucibleBubble(
                level,
                x + offsetX / 32.0F + 0.015625F,
                y + 0.05F + getFluidHeight(),
                z + offsetZ / 32.0F + 0.015625F,
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F
            );
        }
    }

    public void spill() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        if (random.nextInt(4) != 0) {
            return;
        }
        BlockPos above = worldPosition.above();
        BlockState aboveState = serverLevel.getBlockState(above);
        if (aboveState.isAir()) {
            serverLevel.setBlock(above, randomFlux(random).defaultBlockState(), Block.UPDATE_ALL);
        } else if (aboveState.is(ModBlocks.FLUX_GOO.get()) && aboveState.getValue(FluxBlock.LEVEL) < 8) {
            serverLevel.setBlock(above, aboveState.setValue(FluxBlock.LEVEL, aboveState.getValue(FluxBlock.LEVEL) + 1), Block.UPDATE_ALL);
        } else if (aboveState.is(ModBlocks.FLUX_GAS.get()) && aboveState.getValue(FluxBlock.LEVEL) < 8) {
            serverLevel.setBlock(above, aboveState.setValue(FluxBlock.LEVEL, aboveState.getValue(FluxBlock.LEVEL) + 1), Block.UPDATE_ALL);
        } else {
            BlockPos target = worldPosition.offset(-1 + random.nextInt(3), -1 + random.nextInt(3), -1 + random.nextInt(3));
            if (serverLevel.getBlockState(target).isAir()) {
                serverLevel.setBlock(target, randomFlux(random).defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static Block randomFlux(RandomSource random) {
        return random.nextBoolean() ? ModBlocks.FLUX_GAS.get() : ModBlocks.FLUX_GOO.get();
    }

    public void spillRemnants() {
        if (level == null) {
            return;
        }
        if (tank.getAmount() > 0 || aspects.visSize() > 0) {
            tank.setFluid(FluidStack.EMPTY);
            int count = aspects.visSize() / 2;
            for (int index = 0; index < count; index++) {
                spill();
            }
            aspects = new AspectList();
            sync();
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL, 5);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            spillRemnants();
        }
        super.preRemoveSideEffects(pos, state);
    }

    public void ejectItem(ItemStack items) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack remaining = items.copy();
        boolean first = true;
        RandomSource random = serverLevel.getRandom();
        do {
            ItemStack spitOut = remaining.split(Math.min(remaining.getCount(), remaining.getMaxStackSize()));
            ItemEntity entity = new ItemEntity(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5, spitOut);
            entity.addTag(CrucibleBlock.SPECIAL_ITEM_TAG);
            double motionX = first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01F;
            double motionZ = first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01F;
            entity.setDeltaMovement(new Vec3(motionX, 0.1F, motionZ));
            serverLevel.addFreshEntity(entity);
            first = false;
        } while (!remaining.isEmpty());
    }

    public void attemptSmelt(ItemEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        ItemStack item = entity.getItem().copy();
        Player player = entity.getOwner() instanceof Player owner ? owner : null;
        boolean bubble = false;
        boolean event = false;
        boolean bounce = false;
        int stackSize = item.getCount();
        int total = stackSize;
        for (int index = 0; index < total; index++) {
            CrucibleRecipe recipe = CrucibleCrafting.find(player, aspects, item);
            if (recipe != null && tank.getAmount() > 0) {
                ItemStack output = recipe.result().create();
                if (player != null) {
                    EventHooks.firePlayerCraftingEvent(player, output, new SimpleContainer(item.copyWithCount(1)));
                }
                aspects = CrucibleCrafting.removeMatching(recipe, aspects);
                drainWater(50);
                ejectItem(output);
                event = true;
                stackSize--;
                counter = -250L;
            } else {
                AspectList tags = CrucibleCrafting.dissolveTags(item);
                if (tags.isEmpty()) {
                    bounce = true;
                    break;
                }
                for (Aspect aspect : tags.getAspects()) {
                    aspects.add(aspect, tags.getAmount(aspect));
                }
                bubble = true;
                stackSize--;
                counter = -150L;
            }
        }
        if (bubble) {
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.BUBBLE.get(), SoundSource.BLOCKS, 0.2F, 1.0F + random.nextFloat() * 0.4F);
            sync();
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL, 1);
        }
        if (event) {
            sync();
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL, 5);
        }
        if (stackSize <= 0) {
            entity.discard();
        } else {
            entity.setItem(item.copyWithCount(stackSize));
            if (bounce) {
                entity.setDeltaMovement((random.nextFloat() - random.nextFloat()) * 0.2F, 0.35F, (random.nextFloat() - random.nextFloat()) * 0.2F);
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
            }
        }
        setChanged();
    }

    public AspectList takeRandomFromSource() {
        AspectList output = new AspectList();
        if (aspects.size() > 0 && level != null) {
            List<Aspect> list = aspects.getAspects();
            Aspect aspect = list.get(level.getRandom().nextInt(list.size()));
            output.add(aspect, 1);
            aspects.remove(aspect, 1);
        }
        sync();
        return output;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (level == null) {
            return false;
        }
        if (id == EVENT_SPARKLE) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, worldPosition, -9999, 5);
            }
            return true;
        }
        if (id == EVENT_BOIL) {
            if (level.isClientSide()) {
                level.playLocalSound(worldPosition, ModSounds.SPILL.get(), SoundSource.BLOCKS, 0.2F, 1.0F, false);
                for (int index = 0; index < 10; index++) {
                    Fx.get().crucibleBoil(level, worldPosition, getFluidHeight(), tagAmount(), param);
                }
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, Player player, ItemStack wand) {
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            spillRemnants();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heat = (short) input.getShortOr("Heat", (short) 0);
        int water = input.getIntOr("Water", 0);
        tank.setFluid(water > 0 ? new FluidStack(Fluids.WATER, water) : FluidStack.EMPTY);
        aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("Heat", heat);
        output.putInt("Water", tank.getAmount());
        output.store("Aspects", AspectList.CODEC, aspects);
    }
}
