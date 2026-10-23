package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.vis.VisNet;

public class EssentiaCrystallizerBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport {
    public static final int PROGRESS_MAX = 200;

    private @Nullable Aspect aspect;
    private int count;
    private int progress;
    private float spin;
    private float spinIncrement;
    private float currentRed = 1.0F;
    private float currentGreen = 1.0F;
    private float currentBlue = 1.0F;
    private int venting;

    public EssentiaCrystallizerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), pos, state);
    }

    protected EssentiaCrystallizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaCrystallizerBlockEntity crystallizer) {
        crystallizer.tick();
    }

    public Direction getFacing() {
        return getBlockState().getValue(EssentiaCrystallizerBlock.FACING);
    }

    public @Nullable Aspect getAspect() {
        return aspect;
    }

    public int getProgress() {
        return progress;
    }

    public float getSpin() {
        return spin;
    }

    public float getSpinIncrement() {
        return spinIncrement;
    }

    public float getCurrentRed() {
        return currentRed;
    }

    public float getCurrentGreen() {
        return currentGreen;
    }

    public float getCurrentBlue() {
        return currentBlue;
    }

    public boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private void tick() {
        if (level == null) {
            return;
        }
        if (!level.isClientSide()) {
            if (++count % 5 == 0 && !gettingPower()) {
                if (aspect == null) {
                    fillFromSource();
                    progress = 0;
                } else {
                    int drained = VisNet.drainVis(level, worldPosition, Aspect.EARTH, Math.min(20, Math.max(1, (PROGRESS_MAX - progress) / 2)));
                    progress = progress + 1 + drained * 2;
                }
            }
            if (aspect != null && progress >= PROGRESS_MAX) {
                eject();
                aspect = null;
                progress = 0;
                sync();
            }
        } else {
            clientTick();
        }
    }

    private void clientTick() {
        float targetRed = 1.0F;
        float targetGreen = 1.0F;
        float targetBlue = 1.0F;
        if (aspect != null) {
            int color = aspect.getColor();
            targetRed = (color >> 16 & 255) / 220.0F;
            targetGreen = (color >> 8 & 255) / 220.0F;
            targetBlue = (color & 255) / 220.0F;
        }
        currentRed = approach(currentRed, targetRed);
        currentGreen = approach(currentGreen, targetGreen);
        currentBlue = approach(currentBlue, targetBlue);
        spin += spinIncrement;
        if (spin > 360.0F) {
            spin -= 360.0F;
        }
        if (aspect != null && spinIncrement < 20.0F && !gettingPower()) {
            spinIncrement += 0.1F;
            if (spinIncrement > 20.0F) {
                spinIncrement = 20.0F;
            }
        } else if ((aspect == null || gettingPower()) && spinIncrement > 0.0F) {
            spinIncrement -= 0.2F;
            if (spinIncrement < 0.0F) {
                spinIncrement = 0.0F;
            }
        }
        if (venting > 0) {
            venting--;
            RandomSource random = level.getRandom();
            Direction back = getFacing().getOpposite();
            Fx.get().ventParticles(
                level,
                worldPosition.getX() + 0.5F + 0.1F - random.nextFloat() * 0.2F + back.getStepX() / 2.1F,
                worldPosition.getY() + 0.5F + 0.1F - random.nextFloat() * 0.2F + back.getStepY() / 2.1F,
                worldPosition.getZ() + 0.5F + 0.1F - random.nextFloat() * 0.2F + back.getStepZ() / 2.1F,
                back.getStepX() / 4.0F + 0.1F - random.nextFloat() * 0.2F,
                back.getStepY() / 4.0F + 0.1F - random.nextFloat() * 0.2F,
                back.getStepZ() / 4.0F + 0.1F - random.nextFloat() * 0.2F,
                0xFFFFFF,
                1.0F
            );
        }
    }

    private static float approach(float current, float target) {
        float result = current;
        if (result < target) {
            result += 0.05F;
        }
        if (result > target) {
            result -= 0.05F;
        }
        return result;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (level != null && level.isClientSide()) {
                venting = 7;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void eject() {
        ItemStack stack = new ItemStack(ModItems.CRYSTALLIZED_ESSENCE.get());
        AspectKeys.ASPECTS.set(stack, new AspectList().add(aspect, 1));
        Direction back = getFacing().getOpposite();
        stack = Transfer.insert(level, worldPosition.relative(back), getFacing(), stack, false);
        if (!stack.isEmpty()) {
            ItemEntity entity = new ItemEntity(
                level,
                worldPosition.getX() + 0.5 + back.getStepX() * 0.65,
                worldPosition.getY() + 0.5 + back.getStepY() * 0.65,
                worldPosition.getZ() + 0.5 + back.getStepZ() * 0.65,
                stack
            );
            entity.setDeltaMovement(new Vec3(back.getStepX() * 0.04F, back.getStepY() * 0.04F, back.getStepZ() * 0.04F));
            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
            level.addFreshEntity(entity);
        }
        level.playSound(
            null,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.25F,
            2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F
        );
    }

    private void fillFromSource() {
        Direction facing = getFacing();
        EssentiaTransport source = Essentia.getConnectable(level, worldPosition, facing);
        if (source == null || !source.canOutputTo(facing.getOpposite())) {
            return;
        }
        Aspect wanted = null;
        if (source.getEssentiaAmount(facing.getOpposite()) > 0
            && source.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)
            && getSuctionAmount(facing) >= source.getMinimumSuction()) {
            wanted = source.getEssentiaType(facing.getOpposite());
        }
        if (wanted != null && source.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)) {
            addToContainer(wanted, source.takeEssentia(wanted, 1, facing.getOpposite()));
        }
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (aspect != null) {
            aspects.add(aspect, 1);
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect added, int amount) {
        if (amount == 0) {
            return amount;
        }
        int remaining = amount;
        if (aspect == null) {
            remaining--;
            aspect = added;
            sync();
        }
        return remaining;
    }

    @Override
    public boolean takeFromContainer(Aspect taken, int amount) {
        if (aspect != null && amount == 1) {
            aspect = null;
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect checked, int amount) {
        return amount == 1 && aspect != null && checked == aspect;
    }

    @Override
    public int containerContains(Aspect checked) {
        return aspect != null && checked == aspect ? 1 : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect checked) {
        return true;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return gettingPower() ? 0 : (face == getFacing() && aspect == null ? 128 : 64);
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return aspect == null ? 0 : 1;
    }

    @Override
    public int takeEssentia(Aspect taken, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect added, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(added, amount) : 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.store("Aspect", Aspect.CODEC, aspect);
        }
        output.putInt("progress", progress);
    }
}
