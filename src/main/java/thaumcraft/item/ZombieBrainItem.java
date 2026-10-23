package thaumcraft.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

public class ZombieBrainItem extends Item {
    public ZombieBrainItem(Properties properties) {
        super(properties);
    }

    public static void applyWarp(ServerPlayer player, boolean sticky, int amount) {
        Warp.add(player, amount, sticky ? WarpType.STICKY : WarpType.TEMPORARY);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (level.getRandom().nextFloat() < 0.1F) {
                applyWarp(player, true, 1);
            } else {
                applyWarp(player, false, 1 + level.getRandom().nextInt(3));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
