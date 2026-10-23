package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.vis.VisNet;

public class AdvancedAlchemicalFurnaceBlockEntity extends TcBlockEntity {
    public static final int MAX_VIS = 500;
    public static final int MAX_POWER = 500;

    private AspectList aspects = new AspectList();
    private int heat;
    private int power1;
    private int power2;
    private int count;
    private int processed;

    public AdvancedAlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), pos, state);
    }

    protected AdvancedAlchemicalFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AspectList getAspects() {
        return aspects;
    }

    public int getVis() {
        return aspects.visSize();
    }

    public int getMaxVis() {
        return MAX_VIS;
    }

    public int getHeat() {
        return heat;
    }

    public int getPower1() {
        return power1;
    }

    public int getPower2() {
        return power2;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedAlchemicalFurnaceBlockEntity furnace) {
        furnace.count++;
        if (furnace.processed > 0) {
            furnace.processed--;
        }
        if (furnace.count % 5 == 0) {
            int previousHeat = furnace.heat;
            int previousPower1 = furnace.power1;
            int previousPower2 = furnace.power2;
            furnace.heat = Math.max(0, furnace.heat - 1);
            if (furnace.heat <= MAX_POWER) {
                furnace.heat += VisNet.drainVis(level, pos, Aspect.FIRE, 50);
            }
            if (furnace.power1 <= MAX_POWER) {
                furnace.power1 += VisNet.drainVis(level, pos, Aspect.ENTROPY, 50);
            }
            if (furnace.power2 <= MAX_POWER) {
                furnace.power2 += VisNet.drainVis(level, pos, Aspect.WATER, 50);
            }
            if (previousHeat / 50 != furnace.heat / 50) {
                furnace.updateLight(state);
                furnace.sync();
            } else if (previousHeat != furnace.heat || previousPower1 != furnace.power1 || previousPower2 != furnace.power2) {
                // Charging can change only water/entropy, without crossing a visible heat band.
                furnace.setChanged();
            }
        }
    }

    private void updateLight(BlockState state) {
        if (level == null) {
            return;
        }
        int light = heat > 100 ? Math.min(12, (int) ((float) heat / MAX_POWER * 12.0F)) : 0;
        if (state.getValue(AdvancedAlchemicalFurnaceBlock.LIGHT) != light) {
            level.setBlock(worldPosition, state.setValue(AdvancedAlchemicalFurnaceBlock.LIGHT, light), Block.UPDATE_CLIENTS);
        }
    }

    public boolean process(ItemStack stack) {
        if (processed != 0 || !canSmelt(stack)) {
            return false;
        }
        AspectList tags = CrucibleCrafting.dissolveTags(stack);
        int visSize = tags.visSize();
        if (visSize * 2 <= heat && visSize <= power1 && visSize <= power2) {
            heat -= visSize * 2;
            power1 -= visSize;
            power2 -= visSize;
            processed += (int) (5.0F + Math.max(0.0F, (1.0F - (float) heat / MAX_POWER) * 100.0F));
            aspects.add(tags);
            sync();
            return true;
        }
        return false;
    }

    private boolean canSmelt(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        AspectList tags = CrucibleCrafting.dissolveTags(stack);
        return !tags.isEmpty() && tags.visSize() + aspects.visSize() <= MAX_VIS;
    }

    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspects.getAmount(aspect) >= amount) {
            aspects.remove(aspect, amount);
            sync();
            return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heat = input.getIntOr("heat", 0);
        power1 = input.getIntOr("power1", 0);
        power2 = input.getIntOr("power2", 0);
        aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("heat", heat);
        output.putInt("power1", power1);
        output.putInt("power2", power2);
        output.store("Aspects", AspectList.CODEC, aspects);
    }
}
