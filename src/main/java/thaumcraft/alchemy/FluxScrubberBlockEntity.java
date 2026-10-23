package thaumcraft.alchemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.vis.VisNet;

public class FluxScrubberBlockEntity extends TcBlockEntity implements EssentiaTransport {
    private static final int DISTANCE = 16;

    private int essentia;
    private int charges;
    private int power;
    private final List<BlockPos> checklist = new ArrayList<>();

    public FluxScrubberBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUX_SCRUBBER.get(), pos, state);
    }

    protected FluxScrubberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getFacing() {
        return getBlockState().getValue(FluxScrubberBlock.FACING);
    }

    public int getEssentia() {
        return essentia;
    }

    public static void serverTick(Level level, BlockPos pos, FluxScrubberBlockEntity scrubber) {
        if (scrubber.charges >= 4) {
            scrubber.charges -= 4;
            if (level.getRandom().nextInt(4) == 0) {
                scrubber.essentia++;
                if (scrubber.essentia > 4) {
                    scrubber.essentia = 4;
                }
                scrubber.setChanged();
            }
        }
        if (scrubber.power < 5) {
            scrubber.power += VisNet.drainVis(level, pos, Aspect.AIR, 10);
        }
        if (scrubber.power >= 5 && level instanceof ServerLevel serverLevel) {
            scrubber.checkFlux(serverLevel, pos);
        }
    }

    private void checkFlux(ServerLevel level, BlockPos origin) {
        if (checklist.isEmpty()) {
            for (int x = -DISTANCE; x <= DISTANCE; x++) {
                for (int y = -DISTANCE; y <= DISTANCE; y++) {
                    for (int z = -DISTANCE; z <= DISTANCE; z++) {
                        checklist.add(origin.offset(x, y, z));
                    }
                }
            }
            Collections.shuffle(checklist, new java.util.Random(level.getRandom().nextLong()));
        }
        int checked = 0;
        while (checked < 16 && !checklist.isEmpty()) {
            checked++;
            BlockPos target = checklist.remove(0);
            BlockState state = level.getBlockState(target);
            if (!state.isAir() && state.getBlock() instanceof FluxBlock
                && origin.distToCenterSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5) < DISTANCE * DISTANCE) {
                power -= 5;
                int quanta = state.getValue(FluxBlock.LEVEL);
                if (quanta > 1) {
                    level.setBlock(target, state.setValue(FluxBlock.LEVEL, quanta - 1), Block.UPDATE_ALL);
                } else {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                ModNetwork.sendToNear(level, null, target.getX(), target.getY(), target.getZ(), 32.0, new BlockSparkleFxPayload(target, 14483711));
                charges++;
                setChanged();
                return;
            }
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == getFacing();
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        int taken = Math.min(essentia, amount);
        essentia -= taken;
        setChanged();
        return taken;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return Aspect.MAGIC;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return essentia;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charges = input.getIntOr("charges", 0);
        power = input.getIntOr("power", 0);
        essentia = input.getIntOr("essentia", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("charges", charges);
        output.putInt("power", power);
        output.putInt("essentia", essentia);
    }
}
