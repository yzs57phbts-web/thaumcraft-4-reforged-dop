package thaumcraft.client;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.*;
import thaumcraft.essentia.*;
import thaumcraft.node.*;
import thaumcraft.registry.*;
import thaumcraft.research.Knowledge;
import thaumcraft.vis.*;

/** Opt-in private test world: delayed parent BE delivery and original FM aspect art. */
@EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class AlchemyNetworkSmoke {
    private static final BlockPos SOURCE=new BlockPos(-3,102,0),RELAY=new BlockPos(3,102,0);
    private static final List<Aspect> DARK=List.of(Aspect.INFERNUS,Aspect.IRA,Aspect.INVIDIA,Aspect.GULA,Aspect.SUPERBIA,Aspect.DESIDIA,Aspect.LUXURIA);
    private static boolean started;
    private static int ticks,captures;
    private static CompoundTag sourceTag;
    private static BlockState sourceState;
    private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();var id=mc.player.getUUID();server.execute(()->{var p=server.getPlayerList().getPlayer(id);if(p!=null)action.accept(p);});}
    private static void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),msg->captures++);}
    @SubscribeEvent static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("thaumcraft.alchemySmoke"))return;
        var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;
        if(!started&&mc.getOverlay()==null&&mc.screen!=null){
            started=true;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);
            var rules=new GameRules();rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0,null);
            mc.createWorldOpenFlows().createFreshLevel("alchemy-network-"+System.currentTimeMillis(),new LevelSettings("Alchemy and aspect validation",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(4179,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        }
        if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
        ticks++;if(ticks==5)mc.setScreen(null);
        if(ticks==10)server(player->{
            var level=player.serverLevel();for(var pos:BlockPos.betweenClosed(-9,99,-5,9,99,10))level.setBlockAndUpdate(pos,Blocks.STONE_BRICKS.defaultBlockState());
            level.setDayTime(6000);player.connection.teleport(0.5,100,8.5,180,0);player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.GOGGLES_OF_REVEALING.get()));
            level.setBlockAndUpdate(SOURCE,ModBlocks.ENERGIZED_AURA_NODE.get().defaultBlockState());var n=(EnergizedNodeBlockEntity)level.getBlockEntity(SOURCE);n.setAspects(new AspectList().add(Aspect.FIRE,100).add(Aspect.WATER,100));n.setupNode();
            level.setBlockAndUpdate(RELAY,ModBlocks.VIS_RELAY.get().defaultBlockState());
            for(int i=0;i<DARK.size();i++){
                BlockPos pos=new BlockPos(i-3,100,3);level.setBlockAndUpdate(pos,ModBlocks.WARDED_JAR.get().defaultBlockState());var j=(JarBlockEntity)level.getBlockEntity(pos);j.setFacing(Direction.SOUTH);j.setAspectFilter(DARK.get(i));j.addToContainer(DARK.get(i),40);j.sync();
                ItemStack item=((JarBlock)j.getBlockState().getBlock()).createDrop(j);player.getInventory().setItem(i,item);Knowledge.get(player).discoverAspect(DARK.get(i));
            }Knowledge.sync(player);
        });
        if(ticks==100){
            var relay=(VisRelayBlockEntity)mc.level.getBlockEntity(RELAY);if(relay==null||!VisNet.isNodeValid(relay.getParent()))throw new IllegalStateException("Client relay never connected");
            for(int i=0;i<DARK.size();i++){
                var j=(JarBlockEntity)mc.level.getBlockEntity(new BlockPos(i-3,100,3));if(j==null||j.getAspect()!=DARK.get(i)||j.getAmount()!=40)throw new IllegalStateException("New aspect jar did not synchronize");
                if(Knowledge.get(mc.player).hasDiscoveredAspect(DARK.get(i))!=DARK.get(i).isEnabled())throw new IllegalStateException("Optional aspect visibility did not synchronize");
            }capture("alchemy-01-vis-beam-and-seven-aspects");
        }
        if(ticks==120){
            var relay=(VisRelayBlockEntity)mc.level.getBlockEntity(RELAY);CompoundTag relayTag=relay.getUpdateTag(mc.level.registryAccess());
            var source=(EnergizedNodeBlockEntity)mc.level.getBlockEntity(SOURCE);sourceTag=source.getUpdateTag(mc.level.registryAccess());sourceState=source.getBlockState();
            mc.level.removeBlockEntity(SOURCE);relay.loadWithComponents(relayTag,mc.level.registryAccess());
        }
        if(ticks==140){var n=new EnergizedNodeBlockEntity(SOURCE,sourceState);n.loadWithComponents(sourceTag,mc.level.registryAccess());mc.level.setBlockEntity(n);}
        if(ticks==170){
            var relay=(VisRelayBlockEntity)mc.level.getBlockEntity(RELAY);if(!VisNet.isNodeValid(relay.getParent())||!SOURCE.equals(relay.getParentPos()))throw new IllegalStateException("Relay failed to retry delayed parent BE");
            System.out.println("ALCHEMY_CLIENT_DELAYED_PARENT_PASS stored_aspects=7 active_aspects="+Aspect.getEnabledAspects().length+" visible_fm_knowledge="+(Aspect.INFERNUS.isEnabled()?7:0));capture("alchemy-02-delayed-parent-recovered");
        }
        if(ticks==190){if(captures<2)throw new IllegalStateException("Missing client captures");System.out.println("ALCHEMY_CLIENT_ALL_PASS screenshots="+captures);mc.stop();}
    }
}
