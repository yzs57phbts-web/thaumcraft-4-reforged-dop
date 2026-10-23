package thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import thaumcraft.item.armor.VoidRobeArmorItem;

public class VoidRobeArmorDyeRecipe extends ArmorDyeRecipe {
    public static final VoidRobeArmorDyeRecipe INSTANCE = new VoidRobeArmorDyeRecipe();
    public static final MapCodec<VoidRobeArmorDyeRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, VoidRobeArmorDyeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<VoidRobeArmorDyeRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<VoidRobeArmorDyeRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VoidRobeArmorDyeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    @Override
    protected boolean isTarget(ItemStack stack) {
        return stack.getItem() instanceof VoidRobeArmorItem;
    }

    @Override
    public RecipeSerializer<VoidRobeArmorDyeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
