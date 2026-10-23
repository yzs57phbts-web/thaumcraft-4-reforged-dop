package thaumcraft.wand.focus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import thaumcraft.entity.CombatUtils;
import thaumcraft.entity.projectile.Ember;
import thaumcraft.entity.projectile.ExplosiveOrb;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.Wands;

public class FireFocus extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 10);
    private static final AspectList COST_BEAM = new AspectList().add(Aspect.FIRE, 10).add(Aspect.ORDER, 3);
    private static final AspectList COST_BALL = new AspectList().add(Aspect.FIRE, 66).add(Aspect.ENTROPY, 33);

    public static final FocusUpgradeType FIREBALL = new FocusUpgradeType(9, "fireball", new AspectList().add(Aspect.DARKNESS, 1));
    public static final FocusUpgradeType FIREBEAM = new FocusUpgradeType(10, "firebeam", new AspectList().add(Aspect.ENERGY, 1).add(Aspect.AIR, 1));

    public FireFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "AF" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 15028484;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_fire");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, FIREBEAM) ? COST_BEAM : this.isUpgradedWith(focus, FIREBALL) ? COST_BALL : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return this.isUpgradedWith(focus, FIREBALL) ? 1000 : 0;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focus) {
        return this.isUpgradedWith(focus, FIREBALL) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        ItemStack focus = Wands.getFocusItem(wand);
        if (this.isUpgradedWith(focus, FIREBALL)) {
            if (!item.consumeAllVis(wand, player, this.getVisCost(wand), !level.isClientSide(), false)) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                ExplosiveOrb orb = ExplosiveOrb.create(serverLevel, player);
                orb.strength += Wands.getFocusPotency(wand) * 0.4F;
                orb.onFire = this.isUpgradedWith(focus, FocusUpgradeType.ALCHEMISTS_FIRE);
                serverLevel.addFreshEntity(orb);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        CombatUtils.startUsingWand(player, wand);
        WandManager.setCooldown(player, wand, -1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, this.getVisCost(wand), false, false)) {
            player.stopUsingItem();
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack focus = Wands.getFocusItem(wand);
        if (player.tickCount % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FIRELOOP.get(), SoundSource.PLAYERS, 0.33F, 2.0F);
        }
        if (!item.consumeAllVis(wand, player, this.getVisCost(wand), true, false)) {
            return;
        }
        int potency = Wands.getFocusPotency(wand);
        boolean beam = this.isUpgradedWith(focus, FIREBEAM);
        float scatter = beam ? 0.25F : 15.0F;
        for (int index = 0; index < 2 + potency; index++) {
            Ember ember = Ember.create(level, player, scatter);
            ember.damage = 2 + potency;
            if (beam) {
                ember.damage += 0.5F;
                ember.damage *= 1.5F;
                ember.setDuration(30);
            }
            ember.firey = this.getUpgradeLevel(focus, FocusUpgradeType.ALCHEMISTS_FIRE);
            ember.setPos(ember.position().add(ember.getDeltaMovement()));
            level.addFreshEntity(ember);
        }
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 2 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FIRE};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FIREBALL, FIREBEAM};
            case 4 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FIRE};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            default -> null;
        };
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.ALCHEMISTS_FIRE)
            || !this.isUpgradedWith(focus, FIREBALL)
            || !this.isUpgradedWith(focus, FocusUpgradeType.ALCHEMISTS_FIRE);
    }
}
