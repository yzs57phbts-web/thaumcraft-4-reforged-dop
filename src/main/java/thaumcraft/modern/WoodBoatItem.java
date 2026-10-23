package thaumcraft.modern;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;

    public final class WoodBoatItem extends Item {
        final String wood;
        final boolean chest;
        WoodBoatItem(String wood,Properties properties){this(wood,false,properties);}
        WoodBoatItem(String wood,boolean chest,Properties properties){super(properties);this.wood=wood;this.chest=chest;}
        private static Boat create(Level level,String wood,boolean chest){Boat boat=chest?ModernContent.CHEST_BOAT.get().create(level):ModernContent.BOAT.get().create(level);((MagicalWoodBoat)boat).wood(wood);return boat;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var stack=player.getItemInHand(hand);var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.ANY);if(hit.getType()!=HitResult.Type.BLOCK)return InteractionResultHolder.pass(stack);
            for(var entity:level.getEntities(player,player.getBoundingBox().expandTowards(player.getViewVector(1).scale(5)).inflate(1),EntitySelector.NO_SPECTATORS.and(Entity::isPickable)))if(entity.getBoundingBox().inflate(entity.getPickRadius()).contains(player.getEyePosition()))return InteractionResultHolder.pass(stack);
            if(!level.mayInteract(player,hit.getBlockPos())||!player.mayUseItemAt(hit.getBlockPos(),hit.getDirection(),stack))return InteractionResultHolder.fail(stack);
            var boat=create(level,wood,chest);boat.setPos(hit.getLocation());boat.setYRot(player.getYRot());if(!level.noCollision(boat,boat.getBoundingBox()))return InteractionResultHolder.fail(stack);
            if(level instanceof ServerLevel server){EntityType.<Boat>createDefaultStackConfig(server,stack,player).accept(boat);if(!server.addFreshEntity(boat))return InteractionResultHolder.fail(stack);level.gameEvent(player,GameEvent.ENTITY_PLACE,hit.getLocation());stack.consume(1,player);}player.awardStat(Stats.ITEM_USED.get(this));return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
        }
    
    public static void dispenser(String wood,boolean chest,Item item){DispenserBlock.registerBehavior(item,new DefaultDispenseItemBehavior(){
        private final DefaultDispenseItemBehavior fallback=new DefaultDispenseItemBehavior();
        protected ItemStack execute(BlockSource source,ItemStack stack){
            var face=source.state().getValue(DispenserBlock.FACING);var pos=source.pos().relative(face);var level=source.level();double rise;
            if(level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER))rise=1;
            else if(level.isEmptyBlock(pos)&&level.getFluidState(pos.below()).is(net.minecraft.tags.FluidTags.WATER))rise=0;
            else return fallback.dispense(source,stack);
            var boat=create(level,wood,chest);boat.setPos(source.pos().getX()+.5+face.getStepX()*1.125,source.pos().getY()+face.getStepY()+rise,source.pos().getZ()+.5+face.getStepZ()*1.125);boat.setYRot(face.toYRot());EntityType.<Boat>createDefaultStackConfig(level,stack,null).accept(boat);
            if(level.noCollision(boat,boat.getBoundingBox())&&level.addFreshEntity(boat)){stack.shrink(1);level.gameEvent(null,GameEvent.ENTITY_PLACE,boat.position());}return stack;
        }
    });}
}
