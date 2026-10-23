package thaumcraft.modern;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public class FloatingCandleBlock extends BaseEntityBlock implements thaumcraft.block.InfusionStabiliser {
    public FloatingCandleBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(FloatingCandleBlock::new);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new FloatingCandleEntity(p,s);}
    @Override public boolean canStabiliseInfusion(Level l,BlockPos p){return true;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return box(6,0.8,6,10,16,10);}
    public static float bob(BlockPos p,double time){return 0.2F*(float)Math.sin(((p.getX()+p.getY()+p.getZ())*10+time)/30.0);}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){double y=p.getY()+0.95+bob(p,l.getGameTime());l.addParticle(ParticleTypes.FLAME,p.getX()+0.5,y,p.getZ()+0.5,0,0,0);l.addParticle(ParticleTypes.SMOKE,p.getX()+0.5,y,p.getZ()+0.5,0,0,0);}
}
