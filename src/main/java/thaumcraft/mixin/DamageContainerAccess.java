package thaumcraft.mixin;

import java.util.Stack;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface DamageContainerAccess {
    @Accessor("damageContainers")
    Stack<DamageContainer> thaumcraft$damageContainers();
}
