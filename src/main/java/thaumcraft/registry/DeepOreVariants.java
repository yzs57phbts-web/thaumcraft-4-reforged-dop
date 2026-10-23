package thaumcraft.registry;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.HostRockOreBlock;

/** Canonical blocks with compatibility for the old deepslate=true states. */
public final class DeepOreVariants {
    public static final List<String> FAMILIES = List.of("cinnabar_ore", "air_infused_stone", "fire_infused_stone", "water_infused_stone", "earth_infused_stone", "order_infused_stone", "entropy_infused_stone");
    public static boolean supports(BlockState state) {
        var id=BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals("thaumcraft") && FAMILIES.contains(id.getPath().replaceFirst("^deepslate_", ""));
    }
    public static BlockState select(BlockState state, boolean deep) {
        if(supports(state)) {
            String family=BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().replaceFirst("^deepslate_", "");
            return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", (deep?"deepslate_":"")+family)).defaultBlockState().setValue(HostRockOreBlock.DEEPSLATE,deep);
        }
        return state.hasProperty(HostRockOreBlock.DEEPSLATE)?state.setValue(HostRockOreBlock.DEEPSLATE,deep):state;
    }
}
