package thaumcraft.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class BaublesCurios {
    private BaublesCurios() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof Bauble bauble) {
                    CuriosApi.registerCurio(item, new Adapter(bauble));
                }
            }
        });
    }

    private record Adapter(Bauble bauble) implements ICurioItem {
        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            bauble.onWornTick(stack, slotContext.entity());
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack previous, ItemStack stack) {
            if (!slotContext.entity().level().isClientSide()) {
                bauble.onEquipped(stack, slotContext.entity());
            }
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack next, ItemStack stack) {
            if (!slotContext.entity().level().isClientSide()) {
                bauble.onUnequipped(stack, slotContext.entity());
            }
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return bauble.canEquip(stack, slotContext.entity());
        }

        @Override
        public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
            return bauble.canUnequip(stack, slotContext.entity());
        }
    }
}
