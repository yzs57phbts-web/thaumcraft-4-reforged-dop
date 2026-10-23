package thaumcraft.device;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;

public class LampOfFertilityBlockEntity extends TcBlockEntity implements EssentiaTransport {
    private int charges;
    private int count;
    private int drawDelay;

    public LampOfFertilityBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAMP_OF_FERTILITY.get(), pos, state);
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

    public static void serverTick(Level level, BlockPos pos, LampOfFertilityBlockEntity lamp) {
        if (lamp.charges < 4 && lamp.drawEssentia()) {
            lamp.charges++;
            lamp.refreshLit();
        }
        if (lamp.charges > 1 && lamp.count++ % 300 == 0) {
            lamp.updateAnimals(level);
        }
    }

    private void updateAnimals(Level level) {
        int distance = 7;
        List<Animal> animals = level.getEntitiesOfClass(
            Animal.class,
            new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1)
                .inflate(distance, distance, distance)
        );
        for (Animal animal : animals) {
            if (animal.getAge() == 0 && !animal.isInLove()) {
                List<Animal> sameKind = new ArrayList<>();
                for (Animal other : animals) {
                    if (other.getClass().equals(animal.getClass())) {
                        sameKind.add(other);
                    }
                }
                if (sameKind.size() <= 7) {
                    Animal partner = null;
                    for (Animal candidate : sameKind) {
                        if (candidate.getAge() == 0 && !candidate.isInLove()) {
                            if (partner != null) {
                                charges -= 2;
                                candidate.setInLove(null);
                                partner.setInLove(null);
                                refreshLit();
                                return;
                            }
                            partner = candidate;
                        }
                    }
                }
            }
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
                && connected.takeEssentia(Aspect.LIFE, 1, facing.getOpposite()) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charges = input.getIntOr("charges", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
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
        return Aspect.LIFE;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face == facing() ? 128 - charges * 10 : 0;
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
