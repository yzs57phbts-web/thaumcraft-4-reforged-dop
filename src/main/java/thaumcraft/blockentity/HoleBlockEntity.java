package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.focus.PortableHoleFocus;

public class HoleBlockEntity extends TcBlockEntity {
    public static final int SPARKLE_COLOR = 4194368;

    private BlockState oldState = Blocks.AIR.defaultBlockState();
    private short countdown;
    private short countdownMax = 120;
    private byte count;
    private byte direction;

    public HoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLE.get(), pos, state);
    }

    public void setup(BlockState oldState, short max, byte count, byte direction) {
        this.oldState = oldState;
        this.countdownMax = max;
        this.count = count;
        this.direction = direction;
        sync();
    }

    public void tick() {
        Level level = this.level;
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            surroundWithSparkles(level);
        }
        if (!level.isClientSide() && countdown == 0 && count > 1 && direction != -1) {
            int x = worldPosition.getX();
            int y = worldPosition.getY();
            int z = worldPosition.getZ();
            for (int index = 0; index < 9; index++) {
                if (index / 3 == 1 && index % 3 == 1) {
                    continue;
                }
                BlockPos around = switch (direction) {
                    case 0, 1 -> new BlockPos(x - 1 + index / 3, y, z - 1 + index % 3);
                    case 2, 3 -> new BlockPos(x - 1 + index / 3, y - 1 + index % 3, z);
                    default -> new BlockPos(x, y - 1 + index / 3, z - 1 + index % 3);
                };
                PortableHoleFocus.createHole(level, around, -1, (byte) 1, countdownMax);
            }
            BlockPos next = worldPosition.relative(Direction.from3DDataValue(direction).getOpposite());
            if (!PortableHoleFocus.createHole(level, next, direction, (byte) (count - 1), countdownMax)) {
                count = 0;
            }
        }
        countdown++;
        if (countdown >= countdownMax) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, worldPosition, SPARKLE_COLOR, 1);
            } else {
                level.setBlock(worldPosition, oldState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                level.scheduleTick(worldPosition, oldState.getBlock(), 2);
                if (!oldState.getFluidState().isEmpty()) {
                    level.scheduleTick(worldPosition, oldState.getFluidState().getType(), 2);
                }
            }
        }
    }

    private boolean opaque(Level level, int x, int y, int z) {
        return level.getBlockState(worldPosition.offset(x, y, z)).isSolidRender(level, worldPosition.offset(x, y, z));
    }

    private boolean notHole(Level level, int x, int y, int z) {
        return !level.getBlockState(worldPosition.offset(x, y, z)).is(ModBlocks.HOLE.get());
    }

    private void sparkle(Level level, float x, float y, float z) {
        Fx.get().sparkle(level, worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z, 1.5F, 2, 0.0F);
    }

    private void surroundWithSparkles(Level level) {
        boolean yp = opaque(level, 0, 1, 0);
        boolean xp = opaque(level, 1, 0, 0);
        boolean zp = opaque(level, 0, 0, 1);
        boolean yn = opaque(level, 0, -1, 0);
        boolean xn = opaque(level, -1, 0, 0);
        boolean zn = opaque(level, 0, 0, -1);
        boolean b1 = notHole(level, 0, 1, 0);
        boolean b2 = notHole(level, 0, -1, 0);
        boolean b3 = notHole(level, 0, 0, -1);
        boolean b4 = notHole(level, 0, 0, 1);
        boolean b5 = notHole(level, -1, 0, 0);
        boolean b6 = notHole(level, 1, 0, 0);
        var random = level.getRandom();
        if (!xp && yp && b6) sparkle(level, 1, 1, random.nextFloat());
        if (!xn && yp && b5) sparkle(level, 0, 1, random.nextFloat());
        if (!zp && yp && b4) sparkle(level, random.nextFloat(), 1, 1);
        if (!zn && yp && b3) sparkle(level, random.nextFloat(), 1, 0);
        if (!xp && yn && b6) sparkle(level, 1, 0, random.nextFloat());
        if (!xn && yn && b5) sparkle(level, 0, 0, random.nextFloat());
        if (!zp && yn && b4) sparkle(level, random.nextFloat(), 0, 1);
        if (!zn && yn && b3) sparkle(level, random.nextFloat(), 0, 0);
        if (!yp && xp && b1) sparkle(level, 1, 1, random.nextFloat());
        if (!yn && xp && b2) sparkle(level, 1, 0, random.nextFloat());
        if (!zp && xp && b4) sparkle(level, 1, random.nextFloat(), 1);
        if (!zn && xp && b3) sparkle(level, 1, random.nextFloat(), 0);
        if (!yp && xn && b1) sparkle(level, 0, 1, random.nextFloat());
        if (!yn && xn && b2) sparkle(level, 0, 0, random.nextFloat());
        if (!zp && xn && b4) sparkle(level, 0, random.nextFloat(), 1);
        if (!zn && xn && b3) sparkle(level, 0, random.nextFloat(), 0);
        if (!xp && zp && b6) sparkle(level, 1, random.nextFloat(), 1);
        if (!xn && zp && b5) sparkle(level, 0, random.nextFloat(), 1);
        if (!yp && zp && b1) sparkle(level, random.nextFloat(), 1, 1);
        if (!yn && zp && b2) sparkle(level, random.nextFloat(), 0, 1);
        if (!xp && zn && b6) sparkle(level, 1, random.nextFloat(), 0);
        if (!xn && zn && b5) sparkle(level, 0, random.nextFloat(), 0);
        if (!yp && zn && b1) sparkle(level, random.nextFloat(), 1, 0);
        if (!yn && zn && b2) sparkle(level, random.nextFloat(), 0, 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        oldState = input.read("old_state", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        countdown = (short) input.getShortOr("countdown", (short) 0);
        countdownMax = (short) input.getShortOr("countdownmax", (short) 120);
        count = input.getByteOr("count", (byte) 0);
        direction = input.getByteOr("direction", (byte) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("old_state", BlockState.CODEC, oldState);
        output.putShort("countdown", countdown);
        output.putShort("countdownmax", countdownMax);
        output.putByte("count", count);
        output.putByte("direction", direction);
    }
}
