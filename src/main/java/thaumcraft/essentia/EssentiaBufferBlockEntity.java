package thaumcraft.essentia;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.alchemy.BellowsBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandInteractable;

public class EssentiaBufferBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport, WandInteractable {
    public static final int MAX_AMOUNT = 8;

    private AspectList aspects = new AspectList();
    private final boolean[] openSides = {true, true, true, true, true, true};
    private final byte[] chokedSides = new byte[6];
    private int count;
    private int bellows = -1;

    public EssentiaBufferBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ESSENTIA_BUFFER.get(), pos, state);
    }

    protected EssentiaBufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaBufferBlockEntity buffer) {
        buffer.tick();
    }

    private void tick() {
        count++;
        if (bellows < 0 || count % 20 == 0) {
            refreshBellows();
        }
        if (level != null && !level.isClientSide() && count % 5 == 0 && aspects.visSize() < MAX_AMOUNT) {
            fillBuffer();
        }
    }

    public int getChokedSide(Direction side) {
        return chokedSides[side.ordinal()];
    }

    public boolean isSideOpen(Direction side) {
        return openSides[side.ordinal()];
    }

    public int getComparatorSignal() {
        float ratio = aspects.visSize() / 8.0F;
        return (int) Math.floor(ratio * 14.0F) + (aspects.visSize() > 0 ? 1 : 0);
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount != 1) {
            return amount;
        }
        if (aspects.visSize() < MAX_AMOUNT) {
            aspects.add(aspect, amount);
            sync();
            return 0;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspects.getAmount(aspect) >= amount) {
            aspects.remove(aspect, amount);
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return aspects.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return aspects.getAmount(aspect);
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return face != null && openSides[face.ordinal()];
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
        if (face == null) {
            return 1;
        }
        int choke = chokedSides[face.ordinal()];
        return choke == 2 ? 0 : (bellows > 0 && choke != 1 ? bellows * 32 : 1);
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        if (aspects.size() > 0 && level != null) {
            List<Aspect> list = aspects.getAspects();
            return list.get(level.getRandom().nextInt(list.size()));
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return aspects.visSize();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (!canOutputTo(face)) {
            return 0;
        }
        int suction = 0;
        EssentiaTransport target = Essentia.getConnectable(level, worldPosition, face);
        if (target != null) {
            suction = target.getSuctionAmount(face.getOpposite());
        }
        for (Direction dir : Direction.values()) {
            if (canOutputTo(dir) && dir != face) {
                EssentiaTransport other = Essentia.getConnectable(level, worldPosition, dir);
                if (other != null) {
                    int otherSuction = other.getSuctionAmount(dir.getOpposite());
                    Aspect otherType = other.getSuctionType(dir.getOpposite());
                    if ((otherType == aspect || otherType == null) && suction < otherSuction && getSuctionAmount(dir) < otherSuction) {
                        return 0;
                    }
                }
            }
        }
        int taken = Math.min(amount, aspects.getAmount(aspect));
        return takeFromContainer(aspect, taken) ? taken : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    private void fillBuffer() {
        for (Direction dir : Direction.values()) {
            EssentiaTransport other = Essentia.getConnectable(level, worldPosition, dir);
            if (other != null
                && other.getEssentiaAmount(dir.getOpposite()) > 0
                && other.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(dir)
                && getSuctionAmount(dir) >= other.getMinimumSuction()) {
                Aspect aspect = other.getEssentiaType(dir.getOpposite());
                if (aspect != null) {
                    addToContainer(aspect, other.takeEssentia(aspect, 1, dir.getOpposite()));
                }
                return;
            }
        }
    }

    public void refreshBellows() {
        if (level != null) {
            bellows = BellowsBlockEntity.getBellows(level, worldPosition, Direction.values());
        }
    }

    public List<TubeArms.TraceBox> traceBoxes() {
        return TubeArms.traceBoxes(level, worldPosition, 0.25, 0.75);
    }

    @Override
    public InteractionResult onWandUseOn(Level useLevel, BlockPos pos, Player player, ItemStack wand, Direction side) {
        int hit = TubeArms.trace(player, pos, traceBoxes());
        Direction arm = TubeArms.direction(hit);
        if (arm == null) {
            return InteractionResult.PASS;
        }
        if (useLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            useLevel.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.6F, 1.1F + useLevel.getRandom().nextFloat() * 0.2F);
            chokedSides[arm.ordinal()]++;
            if (chokedSides[arm.ordinal()] > 2) {
                chokedSides[arm.ordinal()] = 0;
            }
            sync();
        } else {
            useLevel.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F, 0.9F + useLevel.getRandom().nextFloat() * 0.2F);
            openSides[arm.ordinal()] = !openSides[arm.ordinal()];
            BlockEntity neighbour = useLevel.getBlockEntity(pos.relative(arm));
            if (neighbour instanceof TubeBlockEntity tube) {
                tube.setSideOpen(arm.getOpposite(), openSides[arm.ordinal()]);
            }
            if (neighbour instanceof EssentiaBufferBlockEntity buffer) {
                buffer.openSides[arm.getOpposite().ordinal()] = openSides[arm.ordinal()];
                buffer.sync();
            }
            sync();
            TubeArms.refreshAround(useLevel, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        int mask = input.getIntOr("open", 63);
        for (int index = 0; index < 6; index++) {
            openSides[index] = (mask & (1 << index)) != 0;
            chokedSides[index] = (byte) input.getIntOr("choke" + index, 0);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspects", AspectList.CODEC, aspects);
        int mask = 0;
        for (int index = 0; index < 6; index++) {
            if (openSides[index]) {
                mask |= 1 << index;
            }
            output.putInt("choke" + index, chokedSides[index]);
        }
        output.putInt("open", mask);
    }
}
