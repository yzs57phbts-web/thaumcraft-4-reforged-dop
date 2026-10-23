package thaumcraft.essentia;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandInteractable;

public class EssentiaReservoirBlockEntity extends TcBlockEntity implements AspectSource, EssentiaTransport, WandInteractable {
    public static final int MAX_AMOUNT = 256;

    private AspectList essentia = new AspectList();
    private int count;
    private float targetRed = 1.0F;
    private float targetGreen = 1.0F;
    private float targetBlue = 1.0F;
    private float stepRed;
    private float stepGreen;
    private float stepBlue;
    private float currentRed = 1.0F;
    private float currentGreen = 1.0F;
    private float currentBlue = 1.0F;
    private @Nullable Aspect displayAspect;

    public EssentiaReservoirBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    protected EssentiaReservoirBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaReservoirBlockEntity reservoir) {
        reservoir.tick();
    }

    public Direction getFacing() {
        return getBlockState().getValue(EssentiaReservoirBlock.FACING);
    }

    public int getMaxAmount() {
        return MAX_AMOUNT;
    }

    public @Nullable Aspect getDisplayAspect() {
        return displayAspect;
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

    public int getComparatorSignal() {
        float ratio = (float) essentia.visSize() / MAX_AMOUNT;
        return (int) Math.floor(ratio * 14.0F) + (essentia.visSize() > 0 ? 1 : 0);
    }

    private void tick() {
        if (level == null) {
            return;
        }
        count++;
        if (!level.isClientSide() && count % 5 == 0 && essentia.visSize() < MAX_AMOUNT) {
            fillReservoir();
        }
        if (level.isClientSide()) {
            int visSize = essentia.visSize();
            if (visSize > 0) {
                if (level.getRandom().nextInt(500 - visSize) == 0) {
                    level.playLocalSound(
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        ModSounds.CREAK.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.4F + level.getRandom().nextFloat() * 0.2F,
                        false
                    );
                }
                if (count % 20 == 0 && essentia.size() > 0) {
                    List<Aspect> aspects = essentia.getAspects();
                    displayAspect = aspects.get(count / 20 % aspects.size());
                    int color = displayAspect.getColor();
                    targetRed = (color >> 16 & 255) / 255.0F;
                    targetGreen = (color >> 8 & 255) / 255.0F;
                    targetBlue = (color & 255) / 255.0F;
                    stepRed = (currentRed - targetRed) / 20.0F;
                    stepGreen = (currentGreen - targetGreen) / 20.0F;
                    stepBlue = (currentBlue - targetBlue) / 20.0F;
                }
                if (displayAspect == null) {
                    targetRed = 1.0F;
                    targetGreen = 1.0F;
                    targetBlue = 1.0F;
                    stepRed = 0.0F;
                    stepGreen = 0.0F;
                    stepBlue = 0.0F;
                } else {
                    currentRed -= stepRed;
                    currentGreen -= stepGreen;
                    currentBlue -= stepBlue;
                }
            }
        }
    }

    private void fillReservoir() {
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
        return essentia;
    }

    @Override
    public void setAspects(AspectList aspects) {
        essentia = aspects.copy();
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount == 0) {
            return amount;
        }
        int space = MAX_AMOUNT - essentia.visSize();
        int remaining = amount;
        if (space >= amount) {
            essentia.add(aspect, amount);
            remaining = 0;
        } else {
            essentia.add(aspect, space);
            remaining -= space;
        }
        if (space > 0) {
            sync();
        }
        return remaining;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (essentia.getAmount(aspect) >= amount) {
            essentia.remove(aspect, amount);
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return essentia.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return essentia.getAmount(aspect);
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
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
        return face == getFacing();
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
        return 24;
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return essentia.visSize() < MAX_AMOUNT ? 24 : 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return essentia.visSize() > 0 && face == null ? essentia.getAspects().get(0) : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return essentia.visSize();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    @Override
    public InteractionResult onWandUseOn(Level useLevel, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (!useLevel.isClientSide()) {
            Direction facing = player.isShiftKeyDown() ? side : side.getOpposite();
            useLevel.setBlock(pos, getBlockState().setValue(EssentiaReservoirBlock.FACING, facing), Block.UPDATE_ALL);
            sync();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            EssentiaReservoirBlock.burst(serverLevel, pos, this);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = input.read("essentia", AspectList.CODEC).orElseGet(AspectList::new);
        if (essentia.visSize() > MAX_AMOUNT) {
            essentia = new AspectList();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("essentia", AspectList.CODEC, essentia);
    }
}
