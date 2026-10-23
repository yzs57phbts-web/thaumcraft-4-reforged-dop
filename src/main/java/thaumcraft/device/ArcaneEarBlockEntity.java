package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneEarBlockEntity extends TcBlockEntity {
    private int note;
    private NoteBlockInstrument tone = NoteBlockInstrument.HARP;
    private int redstoneSignal;

    public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public int getNote() {
        return note;
    }

    public NoteBlockInstrument getTone() {
        return tone;
    }

    public int getRedstoneSignal() {
        return redstoneSignal;
    }

    public void updateTone() {
        if (level == null) {
            return;
        }
        NoteBlockInstrument below = level.getBlockState(worldPosition.below()).instrument();
        NoteBlockInstrument updated = below.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : below;
        if (updated != tone) {
            tone = updated;
            sync();
        }
    }

    public void changePitch() {
        note = (note + 1) % 25;
        sync();
    }

    public void triggerNote(Level level, BlockPos pos, boolean sound) {
        if (level.getBlockState(pos.above()).isAir()) {
            int instrument = -1;
            if (sound) {
                NoteBlockInstrument below = level.getBlockState(pos.below()).instrument();
                instrument = (below.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : below).ordinal();
            }
            level.blockEvent(pos, getBlockState().getBlock(), instrument, note);
        }
    }

    private void setPowered(Level level, BlockPos pos, BlockState state, boolean powered) {
        if (state.getValue(ArcaneEarBlock.POWERED) != powered) {
            level.setBlock(pos, state.setValue(ArcaneEarBlock.POWERED, powered), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos.below(), state.getBlock());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
        if (ear.redstoneSignal > 0) {
            ear.redstoneSignal--;
            if (ear.redstoneSignal == 0) {
                ear.setPowered(level, pos, state, false);
                ear.sync();
            }
        }
        for (NoteEvents.Event event : NoteEvents.get(level)) {
            if (event.instrument() == ear.tone
                && event.note() == ear.note
                && pos.distToCenterSqr(event.pos().getX() + 0.5, event.pos().getY() + 0.5, event.pos().getZ() + 0.5) <= 4096.0) {
                ear.triggerNote(level, pos, false);
                ear.redstoneSignal = 10;
                ear.setPowered(level, pos, level.getBlockState(pos), true);
                ear.sync();
                break;
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        note = Math.clamp(input.getIntOr("note", 0), 0, 24);
        tone = input.getString("tone").map(name -> {
            for (NoteBlockInstrument instrument : NoteBlockInstrument.values()) {
                if (instrument.getSerializedName().equals(name)) {
                    return instrument;
                }
            }
            return NoteBlockInstrument.HARP;
        }).orElse(NoteBlockInstrument.HARP);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("note", note);
        output.putString("tone", tone.getSerializedName());
    }
}
