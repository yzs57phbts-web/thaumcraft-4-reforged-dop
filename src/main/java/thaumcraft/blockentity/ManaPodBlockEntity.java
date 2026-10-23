package thaumcraft.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.block.ManaPodBlock;
import thaumcraft.registry.ModBlockEntities;

public class ManaPodBlockEntity extends TcBlockEntity {
    private @Nullable Aspect aspect;

    public ManaPodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_POD.get(), pos, state);
    }

    public @Nullable Aspect getAspect() {
        return aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
        sync();
    }

    public void checkGrowth() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        int age = state.getValue(ManaPodBlock.AGE);
        if (age < 7) {
            age++;
            level.setBlock(worldPosition, state.setValue(ManaPodBlock.AGE, age), Block.UPDATE_ALL);
        }
        if (age <= 2) {
            return;
        }
        if (age == 3) {
            AspectList neighbours = new AspectList();
            if (aspect != null) {
                neighbours.add(aspect, 1);
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockEntity entity = level.getBlockEntity(worldPosition.relative(direction));
                if (entity instanceof ManaPodBlockEntity pod && pod.aspect != null) {
                    neighbours.add(pod.aspect, 1);
                }
            }
            if (neighbours.size() > 1) {
                List<Aspect> found = neighbours.getAspects();
                List<Aspect> candidates = new ArrayList<>();
                for (int first = 0; first < found.size(); first++) {
                    candidates.add(found.get(first));
                    for (int second = 0; second < found.size(); second++) {
                        if (first != second) {
                            Aspect combination = Aspect.combine(found.get(first), found.get(second));
                            if (combination != null) {
                                candidates.add(combination);
                                candidates.add(combination);
                            }
                        }
                    }
                }
                if (!candidates.isEmpty()) {
                    aspect = candidates.get(level.getRandom().nextInt(candidates.size()));
                    sync();
                }
            }
            if (neighbours.size() >= 1 && aspect == null) {
                aspect = neighbours.getAspectsSortedAmount().get(0);
                sync();
            }
        }
        if (aspect == null) {
            if (level.getRandom().nextInt(8) == 0) {
                aspect = Aspect.PLANT;
            } else {
                List<Aspect> primals = Aspect.getPrimalAspects();
                aspect = primals.get(level.getRandom().nextInt(primals.size()));
            }
            sync();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("aspect", Aspect.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("aspect", Aspect.CODEC, aspect);
    }
}
