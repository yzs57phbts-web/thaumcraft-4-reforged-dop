package thaumcraft.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.ExcavationFocus;
import thaumcraft.wand.focus.FocusItem;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ToolEvents {
    private ToolEvents() {
    }

    @SubscribeEvent
    static void onBreakBlock(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getMainHandItem();
        BlockState state = event.getState();
        if (event.isCanceled() || ToolHarvest.isHarvesting() || !(held.getItem() instanceof ElementalAxeItem) || player.isShiftKeyDown() || !state.is(BlockTags.LOGS)) {
            return;
        }
        event.setCanceled(true);
        if (event.getLevel() instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = event.getPos();
            ElementalAxeItem.breakFurthest(level, serverPlayer, pos, state);
            held.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
        }
    }

    @SubscribeEvent
    static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || event.getDrops().isEmpty()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        int fortune;
        if (held.getItem() instanceof ElementalPickaxeItem || held.getItem() instanceof PrimalCrusherItem) {
            fortune = event.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .get(Enchantments.FORTUNE).map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, held)).orElse(0);
        } else if (Wands.isWand(held) && Wands.getFocus(held) instanceof FocusItem focus && focus.isUpgradedWith(Wands.getFocusItem(held), ExcavationFocus.DOWSING)) {
            fortune = Wands.getFocusTreasure(held);
        } else {
            return;
        }
        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        if (SpecialMining.replaceDrops(event.getState(), event.getDrops(), 0.2F + fortune * 0.075F, level.getRandom())) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.7F + level.getRandom().nextFloat() * 0.2F);
        }
    }
}
