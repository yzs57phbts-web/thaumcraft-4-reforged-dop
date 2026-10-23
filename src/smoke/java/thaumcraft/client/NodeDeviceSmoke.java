package thaumcraft.client;

import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.*;
import thaumcraft.block.WandPedestalBlock;
import thaumcraft.blockentity.WandPedestalBlockEntity;
import thaumcraft.node.*;
import thaumcraft.registry.*;
import thaumcraft.wand.*;

/** Opt-in isolated world; validates real client synchronization and automatic device tickers. */
@EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class NodeDeviceSmoke {
    private static final BlockPos NODE=new BlockPos(3,102,0), PEDESTAL=new BlockPos(-3,100,0), SOURCE=new BlockPos(-3,102,-1);
    private static boolean started;
    private static int ticks, captures;
    private static volatile boolean serverConverted;
    private static void server(Consumer<ServerPlayer> action){
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
        server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player!=null)action.accept(player);});
    }
    private static void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),msg->captures++);}
    @SubscribeEvent static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("thaumcraft.nodeDeviceSmoke"))return;
        var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;
        if(!started && mc.getOverlay()==null && mc.screen!=null){
            started=true;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);
            var rules=new GameRules();rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0,null);
            mc.createWorldOpenFlows().createFreshLevel("node-devices-"+System.currentTimeMillis(),new LevelSettings("Node device validation",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(4178,false,false),registry->registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        }
        if(mc.level==null || mc.player==null || mc.getOverlay()!=null)return;
        ticks++;
        if(ticks==5)mc.setScreen(null);
        if(ticks==10)server(player->{
            var level=player.serverLevel();
            for(var pos:BlockPos.betweenClosed(-9,99,-7,9,99,10))level.setBlockAndUpdate(pos,Blocks.STONE_BRICKS.defaultBlockState());
            level.setDayTime(6000);player.connection.teleport(0.5,100,8.5,180,0);
            player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.GOGGLES_OF_REVEALING.get()));
            NodeGenerator.createNodeAt(level,NODE,NodeType.NORMAL,null,new AspectList().add(Aspect.AIR,100).add(Aspect.FIRE,64));
            level.setBlockAndUpdate(NODE.above(),ModBlocks.NODE_TRANSDUCER.get().defaultBlockState());
            level.setBlockAndUpdate(NODE.above(2),Blocks.REDSTONE_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(PEDESTAL,ModBlocks.WAND_RECHARGE_PEDESTAL.get().defaultBlockState());
            NodeGenerator.createNodeAt(level,SOURCE,NodeType.NORMAL,null,new AspectList().add(Aspect.FIRE,1000));
            level.setBlockAndUpdate(SOURCE.below(),ModBlocks.ADVANCED_NODE_STABILIZER.get().defaultBlockState());
            ((WandPedestalBlockEntity)level.getBlockEntity(PEDESTAL)).setStack(Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false));
        });
        if(ticks==35)server(player->player.serverLevel().setBlockAndUpdate(NODE.below(),ModBlocks.NODE_STABILIZER.get().defaultBlockState()));
        if(ticks==140){
            var pedestal=(WandPedestalBlockEntity)mc.level.getBlockEntity(PEDESTAL);
            var source=(NodeBlockEntity)mc.level.getBlockEntity(SOURCE);
            var converter=(NodeTransducerBlockEntity)mc.level.getBlockEntity(NODE.above());
            if(pedestal==null || VisStorage.getVis(pedestal.getStack(),Aspect.FIRE)<=100)throw new IllegalStateException("Client charge stayed stale");
            if(source.getAspects().getAmount(Aspect.FIRE)>=1000)throw new IllegalStateException("Client source vis stayed stale");
            if(converter.getStatus()!=1 || converter.getCount()<=0)throw new IllegalStateException("Late stabilizer did not start client conversion");
            System.out.println("NODE_DEVICES_CLIENT_CHARGING_PASS vis="+VisStorage.getVis(pedestal.getStack(),Aspect.FIRE)+" source="+source.getAspects().getAmount(Aspect.FIRE)+" conversion="+converter.getCount());
            capture("node-devices-01-charging-and-conversion");
        }
        if(ticks==1090)server(player->{
            var level=player.serverLevel();
            if(!(level.getBlockEntity(NODE) instanceof EnergizedNodeBlockEntity))throw new IllegalStateException("Server actual conversion did not finish");
            serverConverted=true;
        });
        if(ticks==1105){
            if(!(mc.level.getBlockEntity(NODE) instanceof EnergizedNodeBlockEntity))throw new IllegalStateException("Converted node did not synchronize to client");
            capture("node-devices-02-energized-node");
        }
        if(ticks==1110)server(player->WandPedestalBlock.interact(player.serverLevel(),PEDESTAL,player,InteractionHand.MAIN_HAND,ItemStack.EMPTY));
        if(ticks==1130){
            var pedestal=(WandPedestalBlockEntity)mc.level.getBlockEntity(PEDESTAL);
            if(pedestal==null || !pedestal.getStack().isEmpty() || pedestal.isDraining())throw new IllegalStateException("Client pedestal retained removed wand or beam");
            capture("node-devices-03-removed-wand");
        }
        if(ticks==1140){
            if(captures<3 || !serverConverted)throw new IllegalStateException("Client verification incomplete");
            System.out.println("NODE_DEVICES_CLIENT_ALL_PASS screenshots="+captures+" real_conversion_ticks=1000");mc.stop();
        }
    }
}
