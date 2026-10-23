package thaumcraft.modern.client;
import thaumcraft.modern.*;
import thaumcraft.Thaumcraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.registries.BuiltInRegistries;
import thaumcraft.client.geom.ModelBoxGeometry;
import thaumcraft.client.platform.ItemGeometry;
public final class CandleRenderer implements BlockEntityRenderer<FloatingCandleEntity> {
    public CandleRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(FloatingCandleEntity c,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        double time=c.getLevel().getGameTime()+partial;
        String name=BuiltInRegistries.BLOCK.getKey(c.getBlockState().getBlock()).getPath().replace("_floating_candle","").replace("_tallow_candle","");
        int color=0xFF000000|DyeColor.byName(name,DyeColor.WHITE).getTextureDiffuseColor();
        pose.pushPose();pose.translate(0,FloatingCandleBlock.bob(c.getBlockPos(),time),0);
        ItemGeometry.solid(buffers,pose,Thaumcraft.id("textures/models/floatycandle.png"),false,sink->ModelBoxGeometry.box(sink,6,4,6,4,8,4,0,((int)time%8)*12,16,96,false,color,light));
        ItemGeometry.solid(buffers,pose,Thaumcraft.id("textures/models/floatycandlestub.png"),false,sink->ModelBoxGeometry.box(sink,7.5F,12,7.5F,1,2,1,0,0,16,16,false,-1,light));
        pose.popPose();
    }
}
