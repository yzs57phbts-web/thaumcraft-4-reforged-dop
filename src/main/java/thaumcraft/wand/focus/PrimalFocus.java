package thaumcraft.wand.focus;

import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.entity.projectile.PrimalOrb;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class PrimalFocus extends FocusItem {
    public static final FocusUpgradeType SEEKER = new FocusUpgradeType(16, "seeker", new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1));

    public PrimalFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "FP" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 10854849;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_primal");
    }

    @Override
    public @Nullable ResourceLocation getDepthLayerTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_primal_depth");
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return 500;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        Random random = new Random(System.currentTimeMillis() / 200L);
        return new AspectList()
            .add(Aspect.WATER, 50 + random.nextInt(5) * 50)
            .add(Aspect.AIR, 50 + random.nextInt(5) * 50)
            .add(Aspect.EARTH, 50 + random.nextInt(5) * 50)
            .add(Aspect.FIRE, 50 + random.nextInt(5) * 50)
            .add(Aspect.ORDER, 50 + random.nextInt(5) * 50)
            .add(Aspect.ENTROPY, 50 + random.nextInt(5) * 50);
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        if (level instanceof ServerLevel serverLevel && item.consumeAllVis(wand, player, this.getVisCost(wand), true, false)) {
            PrimalOrb orb = PrimalOrb.create(serverLevel, player, this.isUpgradedWith(Wands.getFocusItem(wand), SEEKER));
            serverLevel.addFreshEntity(orb);
            serverLevel.playSound(null, orb.getX(), orb.getY(), orb.getZ(), ModSounds.ICE.get(), SoundSource.PLAYERS, 0.3F, 0.8F + serverLevel.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1, 2, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, SEEKER};
            default -> null;
        };
    }
}
