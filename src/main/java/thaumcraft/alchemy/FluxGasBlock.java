package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.effect.EffectHooks;

public class FluxGasBlock extends FluxBlock {
    public FluxGasBlock(Properties properties) {
        super(-4, 1, 12, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || level.getRandom().nextInt(10) != 0 || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (EffectHooks.isTainted(living) || living.getType().getTags().anyMatch(EntityTypeTags.UNDEAD::equals) || living.hasEffect(MobEffects.CONFUSION)) {
            return;
        }
        int quantaIndex = state.getValue(LEVEL) - 1;
        if (level.getRandom().nextBoolean()) {
            EffectHooks.applyVisExhaust(living, 1200, quantaIndex / 3);
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80 + quantaIndex * 20, 0, false, true));
        }
        if (quantaIndex > 0) {
            level.setBlock(pos, withLevel(quantaIndex), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
