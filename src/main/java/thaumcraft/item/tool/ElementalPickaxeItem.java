package thaumcraft.item.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PickaxeItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.fx.Fx;
import thaumcraft.item.gear.Repairable;
import thaumcraft.registry.ModSounds;

public class ElementalPickaxeItem extends PickaxeItem implements Repairable {
    private static final int SCAN_RANGE = 8;
    private static final long SCAN_DURATION = 5000L;

    public ElementalPickaxeItem(Properties properties) {
        super(Registration.tier(TcMaterials.ELEMENTAL_TOOL), properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level() instanceof ServerLevel level && (!(entity instanceof Player) || level.getServer().isPvpAllowed())) {
            entity.igniteForSeconds(2.0F);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        context.getItemInHand().hurtAndBreak(5, player, ((context.getHand()) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
        if (level.isClientSide()) {
            Fx.get().oreScan(level, context.getClickedPos(), SCAN_RANGE, System.currentTimeMillis() + SCAN_DURATION);
        } else {
            level.playSound(null, context.getClickedPos(), ModSounds.WANDFAIL.get(), SoundSource.PLAYERS, 0.2F, 0.2F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.SUCCESS;
    }
}
