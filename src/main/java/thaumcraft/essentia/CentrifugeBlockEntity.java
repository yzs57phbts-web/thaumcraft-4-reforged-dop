package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class CentrifugeBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport {
    private @Nullable Aspect aspectOut;
    private @Nullable Aspect aspectIn;
    private int count;
    private int process;
    private float rotationSpeed;
    private float rotation;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    protected CentrifugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity centrifuge) {
        centrifuge.tick();
    }

    public @Nullable Aspect getAspectIn() {
        return aspectIn;
    }

    public @Nullable Aspect getAspectOut() {
        return aspectOut;
    }

    public float getRotation() {
        return rotation;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private void tick() {
        if (level == null) {
            return;
        }
        if (!level.isClientSide()) {
            if (!gettingPower()) {
                if (aspectOut == null && aspectIn == null && ++count % 5 == 0) {
                    drawEssentia();
                }
                if (process > 0) {
                    process--;
                }
                if (aspectOut == null && aspectIn != null && process == 0) {
                    processEssentia();
                }
            }
        } else {
            if (aspectIn != null && !gettingPower() && rotationSpeed < 20.0F) {
                rotationSpeed += 2.0F;
            }
            if ((aspectIn == null || gettingPower()) && rotationSpeed > 0.0F) {
                rotationSpeed -= 0.5F;
            }
            int previous = (int) rotation;
            rotation += rotationSpeed;
            if (rotation % 180.0F <= 20.0F && previous % 180 >= 160 && rotationSpeed > 0.0F) {
                level.playLocalSound(
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    ModSounds.PUMP.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false
                );
            }
        }
    }

    private void processEssentia() {
        Aspect[] components = aspectIn.getComponents();
        aspectOut = components[level.getRandom().nextInt(2)];
        aspectIn = null;
        sync();
    }

    private void drawEssentia() {
        EssentiaTransport source = Essentia.getConnectable(level, worldPosition, Direction.DOWN);
        if (source == null || !source.canOutputTo(Direction.UP)) {
            return;
        }
        Aspect wanted = null;
        if (source.getEssentiaAmount(Direction.UP) > 0
            && source.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN)
            && getSuctionAmount(Direction.DOWN) >= source.getMinimumSuction()) {
            wanted = source.getEssentiaType(Direction.UP);
        }
        if (wanted != null
            && !wanted.isPrimal()
            && source.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN)
            && source.takeEssentia(wanted, 1, Direction.UP) == 1) {
            aspectIn = wanted;
            process = 39;
            sync();
        }
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (aspectOut != null) {
            aspects.add(aspectOut, 1);
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        int remaining = amount;
        if (remaining > 0 && aspectOut == null) {
            aspectOut = aspect;
            sync();
            remaining--;
        }
        return remaining;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspectOut != null && aspect == aspectOut) {
            aspectOut = null;
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return amount == 1 && aspect == aspectOut;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return aspect == aspectOut ? 1 : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return face == Direction.UP;
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
        return face == Direction.DOWN ? (gettingPower() ? 0 : (aspectIn == null ? 128 : 64)) : 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return aspectOut;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return aspectOut != null ? 1 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (aspectIn == null && !aspect.isPrimal()) {
            aspectIn = aspect;
            process = 39;
            sync();
            return 1;
        }
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspectIn = input.read("aspectIn", Aspect.CODEC).orElse(null);
        aspectOut = input.read("aspectOut", Aspect.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspectIn != null) {
            output.store("aspectIn", Aspect.CODEC, aspectIn);
        }
        if (aspectOut != null) {
            output.store("aspectOut", Aspect.CODEC, aspectOut);
        }
    }
}
