package thaumcraft.modern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** Existing standing candles acquire render entities; obsolete floating IDs become canonical. */
public final class UnifiedCandleMigration {
    public static void loaded(ChunkEvent.Load event){if(event.getChunk() instanceof LevelChunk chunk&&event.getLevel() instanceof ServerLevel l)l.getServer().execute(()->repair(chunk));}
    public static void repair(LevelChunk chunk){
        if(!(chunk.getLevel() instanceof ServerLevel level))return;
        for(int index=0;index<chunk.getSections().length;index++){
            var section=chunk.getSections()[index];if(!section.maybeHas(s->s.getBlock() instanceof FloatingCandleBlock))continue;
            int bottom=level.getSectionYFromSectionIndex(index)<<4;
            for(int y=0;y<16;y++)for(int x=0;x<16;x++)for(int z=0;z<16;z++)if(section.getBlockState(x,y,z).getBlock() instanceof FloatingCandleBlock){
                var pos=new BlockPos(chunk.getPos().getMinBlockX()+x,bottom+y,chunk.getPos().getMinBlockZ()+z);
                var state=section.getBlockState(x,y,z);String color=BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().replace("_floating_candle","").replace("_tallow_candle","");
                var canonical=ModernContent.CANDLES.get(color).get();
                if(state.getBlock()!=canonical)level.setBlockAndUpdate(pos,canonical.defaultBlockState());
                else if(!chunk.getBlockEntities().containsKey(pos)&&chunk.getBlockEntity(pos,LevelChunk.EntityCreationType.IMMEDIATE)!=null){chunk.setUnsaved(true);level.sendBlockUpdated(pos,state,state,3);}
            }
        }
    }
    public static void inventory(PlayerTickEvent.Post event){
        var player=event.getEntity();if(player.level().isClientSide()||player.tickCount%20!=0)return;
        boolean changed=false;
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            var stack=player.getInventory().getItem(i);var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
            if(id.getNamespace().equals("thaumcraft")&&id.getPath().endsWith("_floating_candle")){
                var canonical=ModernContent.CANDLES.get(id.getPath().replace("_floating_candle","")).get().asItem();player.getInventory().setItem(i,stack.transmuteCopy(canonical));changed=true;
            }
        }
        if(changed)player.getInventory().setChanged();
    }
}
