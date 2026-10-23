package thaumcraft.wand.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.entity.CombatUtils;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class HellbatFocus extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 200).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100);
    private static final AspectList COST_BOMB = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 200).add(Aspect.AIR, 100);
    private static final AspectList COST_DEVIL = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100).add(Aspect.EARTH, 100);

    public static final FocusUpgradeType BATBOMBS = new FocusUpgradeType(13, "batbombs", new AspectList().add(Aspect.ENERGY, 1).add(Aspect.TRAP, 1));
    public static final FocusUpgradeType DEVILBATS = new FocusUpgradeType(14, "devilbats", new AspectList().add(Aspect.ARMOR, 1));
    public static final FocusUpgradeType VAMPIREBATS = new FocusUpgradeType(19, "vampirebats", new AspectList().add(Aspect.HUNGER, 1).add(Aspect.LIFE, 1));

    public HellbatFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "HH" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 14431746;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_hellbat");
    }

    @Override
    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_hellbat_orn");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, BATBOMBS) ? COST_BOMB : this.isUpgradedWith(focus, DEVILBATS) ? COST_DEVIL : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return 1000;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        Entity pointed = CombatUtils.getPointedEntity(level, player, 32.0, FireBat.class);
        if (!(pointed instanceof LivingEntity target)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (target instanceof Player && !serverLevel.getServer().isPvpAllowed()) {
            return InteractionResult.PASS;
        }
        float yaw = player.getYRot();
        Vec3 look = player.getViewVector(1.0F);
        double x = player.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F + look.x * 0.5;
        double y = player.getBoundingBox().minY + player.getBbHeight() / 2.0F + 0.25 - 0.05000000014901161 + look.y * 0.5;
        double z = player.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F + look.z * 0.5;
        ItemStack focus = Wands.getFocusItem(wand);
        FireBat bat = new FireBat(ModEntities.FIREBAT.get(), serverLevel);
        bat.moveTo(x, y + bat.getBbHeight(), z, yaw, 0.0F);
        bat.setTarget(target);
        bat.setDamBonus(Wands.getFocusPotency(wand));
        bat.setSummoned(true);
        bat.setHanging(false);
        if (this.isUpgradedWith(focus, DEVILBATS)) {
            bat.setDevil(true);
        }
        if (this.isUpgradedWith(focus, BATBOMBS)) {
            bat.setExplosive(true);
        }
        if (this.isUpgradedWith(focus, VAMPIREBATS)) {
            bat.setOwner(player);
            bat.setVampire(true);
        }
        if (item.consumeAllVis(wand, player, this.getVisCost(wand), true, false) && serverLevel.addFreshEntity(bat)) {
            serverLevel.levelEvent(2004, BlockPos.containing(x, y, z), 0);
            serverLevel.playSound(null, bat.getX(), bat.getY(), bat.getZ(), ModSounds.ICE.get(), SoundSource.PLAYERS, 0.2F, 0.95F + serverLevel.getRandom().nextFloat() * 0.1F);
        } else {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WANDFAIL.get(), SoundSource.PLAYERS, 0.1F, 0.8F + serverLevel.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, BATBOMBS, DEVILBATS};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, VAMPIREBATS};
            default -> null;
        };
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(VAMPIREBATS) || ResearchGate.isComplete(player, "VAMPBAT");
    }
}
