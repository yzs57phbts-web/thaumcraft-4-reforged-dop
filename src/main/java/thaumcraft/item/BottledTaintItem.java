package thaumcraft.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.entity.CombatUtils;
import thaumcraft.entity.projectile.BottleTaint;
import thaumcraft.registry.ModEntities;

public class BottledTaintItem extends Item {
    public BottledTaintItem(Properties properties) {
        super(properties.stacksTo(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.PLAYERS, 0.5F,
            0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide()) {
            BottleTaint bottle = new BottleTaint(ModEntities.BOTTLE_TAINT.get(), level);
            CombatUtils.launch(bottle, player, 0.5F);
            bottle.setXRot(bottle.getXRot() - 20.0F);
            level.addFreshEntity(bottle);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
