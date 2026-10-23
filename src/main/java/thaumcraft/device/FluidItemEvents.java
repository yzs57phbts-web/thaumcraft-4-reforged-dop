package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.damage.ModDamageTypes;
import thaumcraft.item.BathSaltsItem;
import thaumcraft.item.AspectTooltips;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ScanManager;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class FluidItemEvents {
    private FluidItemEvents() {
    }

    @SubscribeEvent
    static void onItemExpire(ItemExpireEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (!(itemEntity.getItem().getItem() instanceof BathSaltsItem)) {
            return;
        }
        Level level = itemEntity.level();
        BlockPos pos = itemEntity.blockPosition();
        if (level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource() && level.getFluidState(pos).getType() == Fluids.WATER) {
            level.setBlock(pos, ModBlocks.PURIFYING_FLUID.get().defaultBlockState(), 3);
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (!event.getSource().is(ModDamageTypes.DISSOLVE)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        AspectList aspects = ScanManager.entityAspects(entity);
        if (aspects == null || aspects.isEmpty()) {
            return;
        }
        for (Aspect aspect : aspects.getAspects()) {
            int aspectAmount = aspects.getAmount(aspect);
            if (aspectAmount < 1 || entity.getRandom().nextBoolean()) {
                continue;
            }
            int amount = Math.max(1, (1 + entity.getRandom().nextInt(aspectAmount)) / 2);
            ItemStack essence = new ItemStack(ModItems.CRYSTALLIZED_ESSENCE.get(), amount);
            AspectTooltips.setSingle(essence, aspect, 1);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), essence));
        }
    }
}
