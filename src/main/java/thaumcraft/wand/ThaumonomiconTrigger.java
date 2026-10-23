package thaumcraft.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ThaumonomiconTrigger {
    private ThaumonomiconTrigger() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(state -> state.is(Blocks.BOOKSHELF), ThaumonomiconTrigger::perform));
    }

    private static boolean perform(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        level.removeBlock(pos, false);
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, new ItemStack(ModItems.THAUMONOMICON.get()));
        drop.setDeltaMovement(0.0, 0.0, 0.0);
        drop.setNoGravity(true);
        drop.setUnlimitedLifetime();
        level.addFreshEntity(drop);
        ModNetwork.sendToNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 32.0, new BlockSparkleFxPayload(pos, -9999));
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
