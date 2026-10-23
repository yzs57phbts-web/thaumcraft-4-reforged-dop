package thaumcraft.item;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.loot.TcLoot;
import thaumcraft.registry.ModSounds;

public class LootBagItem extends Item {
    public LootBagItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tc.lootbag"));
    }

    private int tier(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.startsWith("rare") ? 2 : path.startsWith("uncommon") ? 1 : 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = handleUse(level, player, hand);
        return InteractionResults.holder(result, player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            int tier = tier(stack);
            int rolls = 8 + level.getRandom().nextInt(5);
            for (int index = 0; index < rolls; index++) {
                ItemStack loot = TcLoot.generateLoot(level.registryAccess(), tier, level.getRandom());
                if (!loot.isEmpty()) {
                    serverLevel.addFreshEntity(new ItemEntity(serverLevel, player.getX(), player.getY(), player.getZ(), loot));
                }
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.COINS.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
