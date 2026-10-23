package thaumcraft.item.golem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.entity.golem.TravelingTrunk;
import thaumcraft.registry.ModEntities;

public class TravelingTrunkItem extends Item {
    public TravelingTrunkItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        if (!context.getLevel().isClientSide()) {
            TravelingTrunk trunk = new TravelingTrunk(ModEntities.TRAVELING_TRUNK.get(), context.getLevel());
            var pos = context.getClickedPos().relative(context.getClickedFace());
            trunk.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            trunk.restore(context.getItemInHand());
            trunk.setOwnerUUID(context.getPlayer().getUUID());
            trunk.setCustomName(context.getItemInHand().get(DataComponents.CUSTOM_NAME));
            if (!context.getLevel().noCollision(trunk) || !context.getLevel().addFreshEntity(trunk)) return InteractionResult.FAIL;
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }
}
