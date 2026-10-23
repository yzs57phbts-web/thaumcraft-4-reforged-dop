package thaumcraft.wand.focus;

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
import thaumcraft.entity.projectile.PechBlast;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class PechFocus extends FocusItem {
    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
    private static final AspectList COST_ALL = new AspectList()
        .add(Aspect.AIR, 10)
        .add(Aspect.FIRE, 10)
        .add(Aspect.EARTH, 10)
        .add(Aspect.ORDER, 10)
        .add(Aspect.ENTROPY, 10)
        .add(Aspect.WATER, 10);

    public static final FocusUpgradeType NIGHTSHADE = new FocusUpgradeType(15, "nightshade", new AspectList().add(Aspect.LIFE, 1).add(Aspect.POISON, 1).add(Aspect.MAGIC, 1));

    public PechFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "PP" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 2267460;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_pech");
    }

    @Override
    public @Nullable ResourceLocation getDepthLayerTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_pech_depth");
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return 250;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, NIGHTSHADE) ? COST_ALL : COST;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        if (level instanceof ServerLevel serverLevel && item.consumeAllVis(wand, player, this.getVisCost(wand), true, false)) {
            PechBlast blast = PechBlast.create(
                serverLevel,
                player,
                Wands.getFocusPotency(wand),
                Wands.getFocusExtend(wand),
                this.isUpgradedWith(Wands.getFocusItem(wand), NIGHTSHADE)
            );
            serverLevel.addFreshEntity(blast);
            serverLevel.playSound(null, blast.getX(), blast.getY(), blast.getZ(), ModSounds.ICE.get(), SoundSource.PLAYERS, 0.4F, 1.0F + serverLevel.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1, 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.EXTEND};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, NIGHTSHADE};
            default -> null;
        };
    }
}
