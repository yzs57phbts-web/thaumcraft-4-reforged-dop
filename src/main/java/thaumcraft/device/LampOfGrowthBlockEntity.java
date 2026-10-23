package thaumcraft.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModBlockEntities;

public class LampOfGrowthBlockEntity extends TcBlockEntity implements EssentiaTransport {
    public static final int SPARKLE_COLOR = 4259648;
    private static final int DISTANCE = 6;

    private boolean reserve;
    private int charges = -1;
    private @Nullable BlockPos lastPos;
    private BlockState lastState = Blocks.AIR.defaultBlockState();
    private final List<BlockPos> checklist = new ArrayList<>();
    private int drawDelay;

    public LampOfGrowthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAMP_OF_GROWTH.get(), pos, state);
    }

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = charges;
        refreshLit();
    }

    private Direction facing() {
        return getBlockState().getValue(LampBlock.FACING);
    }

    private void refreshLit() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        boolean lit = charges > 0;
        if (state.hasProperty(ChargedLampBlock.LIT) && state.getValue(ChargedLampBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(ChargedLampBlock.LIT, lit), Block.UPDATE_ALL);
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, LampOfGrowthBlockEntity lamp) {
        lamp.tick(level);
    }

    private void tick(Level level) {
        if (charges <= 0) {
            if (reserve) {
                charges = 100;
                reserve = false;
                refreshLit();
            } else if (drawEssentia()) {
                charges = 100;
                refreshLit();
            }
        }
        if (!reserve && drawEssentia()) {
            reserve = true;
            setChanged();
        }
        if (charges == 0) {
            charges = -1;
            refreshLit();
        }
        if (charges > 0 && level instanceof ServerLevel serverLevel) {
            updatePlant(serverLevel);
        }
    }

    private void updatePlant(ServerLevel level) {
        if (lastPos != null && level.getBlockState(lastPos) != lastState) {
            ModNetwork.sendToNear(level, null, lastPos.getX(), lastPos.getY(), lastPos.getZ(), 32.0, new BlockSparkleFxPayload(lastPos, SPARKLE_COLOR));
            lastState = level.getBlockState(lastPos);
        }
        if (checklist.isEmpty()) {
            for (int a = -DISTANCE; a <= DISTANCE; a++) {
                for (int b = -DISTANCE; b <= DISTANCE; b++) {
                    checklist.add(new BlockPos(worldPosition.getX() + a, worldPosition.getY() + DISTANCE, worldPosition.getZ() + b));
                }
            }
            Collections.shuffle(checklist, new Random(level.getRandom().nextLong()));
        }
        BlockPos column = checklist.remove(0);
        int y = column.getY();
        while (y >= worldPosition.getY() - DISTANCE) {
            BlockPos target = new BlockPos(column.getX(), y, column.getZ());
            if (level.hasChunkAt(target)) {
                BlockState state = level.getBlockState(target);
                if (!state.isAir()
                    && CropUtils.isPlant(state)
                    && worldPosition.distToCenterSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5) < DISTANCE * DISTANCE
                    && !CropUtils.isGrownCrop(level, target)
                    && CropUtils.doesLampGrow(state)) {
                    charges--;
                    lastPos = target;
                    lastState = state;
                    state.randomTick(level, target, level.getRandom());
                    return;
                }
            }
            y--;
        }
    }

    private boolean drawEssentia() {
        if (++drawDelay % 5 != 0) {
            return false;
        }
        Direction facing = facing();
        if (level == null) {
            return false;
        }
        EssentiaTransport connected = Essentia.getConnectable(level, worldPosition, facing);
        if (connected != null) {
            if (!connected.canOutputTo(facing.getOpposite())) {
                return false;
            }
            if (connected.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)
                && connected.takeEssentia(Aspect.PLANT, 1, facing.getOpposite()) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reserve = input.getBooleanOr("reserve", false);
        charges = input.getIntOr("charges", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("reserve", reserve);
        output.putInt("charges", charges);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return Aspect.PLANT;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face != facing() || reserve && charges > 0 ? 0 : 128;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
