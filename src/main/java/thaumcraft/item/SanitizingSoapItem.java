package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

public class SanitizingSoapItem extends Item {
    private static final int USE_DURATION = 200;

    public SanitizingSoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int elapsed = USE_DURATION - ticksRemaining;
        if (elapsed > 195) {
            completeUse(stack, level, player);
            player.stopUsingItem();
            return;
        }
        if (level.isClientSide()) {
            if (level.getRandom().nextFloat() < 0.2F) {
                level.playSound(player, player.blockPosition(), ModSounds.ROOTS.get(), SoundSource.PLAYERS, 0.1F, 1.5F + level.getRandom().nextFloat() * 0.2F);
            }
            for (int particle = 0; particle < 5; particle++) {
                level.addParticle(ParticleTypes.BUBBLE, player.getX() + level.getRandom().nextDouble() - 0.5, player.getY() + level.getRandom().nextDouble() * player.getBbHeight(), player.getZ() + level.getRandom().nextDouble() - 0.5, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (USE_DURATION - timeLeft > 195 && entity instanceof Player player) {
            completeUse(stack, level, player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            completeUse(stack, level, player);
        }
        return stack;
    }

    private void completeUse(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            level.playSound(player, player.blockPosition(), ModSounds.CRAFTSTART.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        float chance = 0.33F;
        if (player.hasEffect(ModEffects.WARP_WARD)) {
            chance += 0.25F;
        }
        BlockPos pos = player.blockPosition();
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.PURIFYING_FLUID.get())) {
            chance += 0.25F;
        }
        if (Warp.get(serverPlayer, WarpType.STICKY) > 0 && level.getRandom().nextFloat() < chance) {
            Warp.add(serverPlayer, -1, WarpType.STICKY);
        }
        int temporaryWarp = Warp.get(serverPlayer, WarpType.TEMPORARY);
        if (temporaryWarp > 0) {
            Warp.add(serverPlayer, -temporaryWarp, WarpType.TEMPORARY);
        }
    }
}
