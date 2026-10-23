package thaumcraft.vis;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class VisRelayBlockEntity extends VisNode {
    public static final int[] COLORS = {16777086, 16727041, 37119, 40960, 15650047, 5592439};
    public static final Map<Integer, WeakReference<VisRelayBlockEntity>> NEARBY_PLAYERS = new HashMap<>();

    private byte color = -1;
    private int px;
    private int py;
    private int pz;
    private boolean parentLoaded;
    private int pulse;
    private float pulseRed = 0.5F;
    private float pulseGreen = 0.5F;
    private float pulseBlue = 0.5F;

    public VisRelayBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VIS_RELAY.get(), pos, state);
    }

    protected VisRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VisRelayBlockEntity entity) {
        entity.drawEffect(level);
        entity.tickNetwork(level);
        if (!level.isClientSide() && entity.nodeCounter % 20 == 0) {
            entity.trackPlayers(level);
        }
    }

    private void trackPlayers(Level tickLevel) {
        AABB area = new AABB(worldPosition).inflate(5.0, 5.0, 5.0);
        List<Player> players = tickLevel.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            WeakReference<VisRelayBlockEntity> known = NEARBY_PLAYERS.get(player.getId());
            if (known == null
                || known.get() == null
                || !(known.get().worldPosition.distToCenterSqr(player.position()) < worldPosition.distToCenterSqr(player.position()))) {
                NEARBY_PLAYERS.put(player.getId(), new WeakReference<>(this));
            }
        }
    }

    private void drawEffect(Level tickLevel) {
        if (tickLevel.isClientSide()) {
            if (parentLoaded || !VisNet.isNodeValid(getParent())) {
                if (px == 0 && py == 0 && pz == 0) {
                    setParent(null);
                    parentLoaded = false;
                } else {
                    BlockEntity entity = tickLevel.getBlockEntity(worldPosition.offset(-px, -py, -pz));
                    if (entity instanceof VisNode node) {
                        setParent(new WeakReference<>(node));
                        parentLoaded = false;
                    } else {
                        // A neighbor chunk's block entity may arrive after our update packet.
                        setParent(null);
                        parentLoaded = true;
                    }
                }
                parentChanged();
            }
            pulseRed = Math.min(1.0F, pulseRed + 0.025F);
            pulseGreen = Math.min(1.0F, pulseGreen + 0.025F);
            pulseBlue = Math.min(1.0F, pulseBlue + 0.025F);
        }
        if (pulse > 0) {
            pulse--;
        }
    }

    @Override
    public byte getAttunement() {
        return color;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public void parentChanged() {
        if (level instanceof ServerLevel serverLevel && !isRemoved() && !isShuttingDown()) {
            BlockState state = getBlockState();
            if (state.hasProperty(VisRelayBlock.CONNECTED) && serverLevel.getBlockState(worldPosition).is(state.getBlock())) {
                boolean connected = VisNet.isNodeValid(getParent());
                if (state.getValue(VisRelayBlock.CONNECTED) != connected) {
                    serverLevel.setBlock(worldPosition, state.setValue(VisRelayBlock.CONNECTED, connected), 2);
                }
            }
        }
    }

    @Override
    public void triggerConsumeEffect(Aspect aspect) {
        int index = Aspect.getPrimalAspects().indexOf(aspect);
        if (index >= 0 && index < COLORS.length && pulse == 0 && level != null) {
            pulse = 5;
            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, index);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 0) {
            return super.triggerEvent(id, param);
        }
        if (level != null && level.isClientSide() && param >= 0 && param < COLORS.length) {
            int rgb = COLORS[param];
            pulse = 5;
            pulseRed = ((rgb >> 16) & 255) / 255.0F;
            pulseGreen = ((rgb >> 8) & 255) / 255.0F;
            pulseBlue = (rgb & 255) / 255.0F;
            WeakReference<VisNode> reference = getParent();
            while (VisNet.isNodeValid(reference) && reference.get() instanceof VisRelayBlockEntity relay && relay.pulse == 0) {
                relay.pulseRed = pulseRed;
                relay.pulseGreen = pulseGreen;
                relay.pulseBlue = pulseBlue;
                relay.pulse = 5;
                reference = relay.getParent();
            }
        }
        return true;
    }

    public Direction getOrientation() {
        return getBlockState().getValue(VisRelayBlock.FACING);
    }

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
        refreshAttunement();
    }

    public void cycleColor() {
        color++;
        if (color > 5) {
            color = -1;
        }
        refreshAttunement();
    }

    private void refreshAttunement() {
        removeThisNode();
        requestNodeRefresh();
        sync();
        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, ModSounds.CRYSTAL.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        }
    }

    public @Nullable BlockPos getParentPos() {
        if (!(level != null && level.isClientSide())) {
            WeakReference<VisNode> reference = getParent();
            return VisNet.isNodeValid(reference) ? reference.get().getBlockPos() : null;
        }
        if (px == 0 && py == 0 && pz == 0) {
            return null;
        }
        return worldPosition.offset(-px, -py, -pz);
    }

    public BlockPos getParentOffset() {
        BlockPos parentPos = getParentPos();
        return parentPos == null ? BlockPos.ZERO : parentPos.subtract(worldPosition);
    }

    public int getPulse() {
        return pulse;
    }

    public float getPulseRed() {
        return pulseRed;
    }

    public float getPulseGreen() {
        return pulseGreen;
    }

    public float getPulseBlue() {
        return pulseBlue;
    }

    public static @Nullable VisRelayBlockEntity getNearbyRelay(Player player) {
        WeakReference<VisRelayBlockEntity> reference = NEARBY_PLAYERS.get(player.getId());
        return reference == null ? null : reference.get();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        color = input.getByteOr("color", (byte) -1);
        px = input.getByteOr("px", (byte) 0);
        py = input.getByteOr("py", (byte) 0);
        pz = input.getByteOr("pz", (byte) 0);
        parentLoaded = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("color", color);
        WeakReference<VisNode> reference = getParent();
        if (VisNet.isNodeValid(reference)) {
            BlockPos parentPos = reference.get().getBlockPos();
            output.putByte("px", (byte) (worldPosition.getX() - parentPos.getX()));
            output.putByte("py", (byte) (worldPosition.getY() - parentPos.getY()));
            output.putByte("pz", (byte) (worldPosition.getZ() - parentPos.getZ()));
        } else {
            output.putByte("px", (byte) 0);
            output.putByte("py", (byte) 0);
            output.putByte("pz", (byte) 0);
        }
    }
}
