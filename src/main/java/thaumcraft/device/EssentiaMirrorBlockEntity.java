package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.essentia.EssentiaHandler;
import thaumcraft.registry.ModBlockEntities;

public class EssentiaMirrorBlockEntity extends LinkedMirrorBlockEntity implements AspectSource {
    public EssentiaMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_MIRROR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaMirrorBlockEntity mirror) {
        mirror.linkTick();
    }

    @Override
    public AspectList getAspects() {
        return new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (isLinkValid(true) && amount <= 1) {
            return target(true) instanceof EssentiaMirrorBlockEntity other
                && EssentiaHandler.drainEssentia(other, aspect, other.getFacing(), 8, true);
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }
}
