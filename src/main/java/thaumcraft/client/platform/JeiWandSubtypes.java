package thaumcraft.client.platform;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.WandParts;
import thaumcraft.wand.Wands;

public final class JeiWandSubtypes {
    private JeiWandSubtypes() {
    }

    public static void register(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.WAND.get(), new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return Wands.getParts(stack);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                WandParts parts = Wands.getParts(stack);
                return parts.cap() + ":" + parts.rod() + ":" + parts.sceptre();
            }
        });
    }
}
