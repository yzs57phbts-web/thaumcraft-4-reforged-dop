package thaumcraft.item.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ToolHarvest {
    private static final Map<UUID, Direction> FACES = new HashMap<>();
    private static boolean harvesting;

    private ToolHarvest() {
    }

    public static boolean isHarvesting() {
        return harvesting;
    }

    public static Direction getFace(Player player) {
        Direction face = FACES.get(player.getUUID());
        return face != null ? face : player.getDirection().getOpposite();
    }

    public static boolean destroy(ServerPlayer player, BlockPos pos, int followColor) {
        ServerLevel level = player.serverLevel();
        boolean previous = harvesting;
        harvesting = true;
        try {
            boolean destroyed = player.gameMode.destroyBlock(pos);
            if (destroyed && followColor >= 0) {
                FollowingItems.attractNear(level, pos, player);
            }
            return destroyed;
        } finally {
            harvesting = previous;
        }
    }

    public static boolean destroyUnlessProtected(ServerPlayer player, BlockPos pos, int followColor) {
        BlockState state = player.level().getBlockState(pos);
        if (state.getDestroySpeed(player.level(), pos) < 0.0F || !player.level().mayInteract(player, pos)) {
            return false;
        }
        return destroy(player, pos, followColor);
    }

    public static void breakArea(ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos pos, Predicate<BlockState> effective, int followColor) {
        int face = getFace(player).get3DDataValue();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                int x = 0;
                int y = 0;
                int z = 0;
                if (face <= 1) {
                    x = a;
                    z = b;
                } else if (face <= 3) {
                    x = a;
                    y = b;
                } else {
                    z = a;
                    y = b;
                }
                BlockPos target = pos.offset(x, y, z);
                if (target.equals(pos)) {
                    continue;
                }
                if (!level.mayInteract(player, target)) {
                    continue;
                }
                BlockState state = level.getBlockState(target);
                if (state.isAir() || state.getDestroySpeed(level, target) < 0.0F || !effective.test(state)) {
                    continue;
                }
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                destroy(player, target, followColor);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    public static boolean isEffective(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean consumeInventoryItem(Player player, Item item) {
        if (player.isCreative()) {
            return true;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getFace() != null) {
            FACES.put(event.getEntity().getUUID(), event.getFace());
        }
    }

    @SubscribeEvent
    static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        FACES.remove(event.getEntity().getUUID());
    }
}
