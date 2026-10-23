package thaumcraft.arcane;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.Wands;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ArcaneTriggers {
    private ArcaneTriggers() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(state -> state.is(ModBlocks.TABLE.get()), ArcaneTriggers::createWorkbench));
    }

    private static boolean createWorkbench(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (level.isClientSide()) {
            return true;
        }
        level.setBlock(pos, ModBlocks.ARCANE_WORKTABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity workbench) {
            if (!Wands.isStaff(wand)) {
                workbench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, wand.copy());
                for (InteractionHand hand : InteractionHand.values()) {
                    if (player.getItemInHand(hand) == wand) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }
            }
            workbench.setChanged();
        }
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15F, 0.5F);
        return true;
    }
}
