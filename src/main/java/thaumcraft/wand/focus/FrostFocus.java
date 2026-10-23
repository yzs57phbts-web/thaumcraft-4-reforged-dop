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
import thaumcraft.entity.projectile.FrostShard;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class FrostFocus extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2);
    private static final AspectList COST_SCATTER = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.AIR, 5);
    private static final AspectList COST_BOULDER = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.EARTH, 5);

    public static final FocusUpgradeType SCATTERSHOT = new FocusUpgradeType(11, "scattershot", new AspectList().add(Aspect.COLD, 1).add(Aspect.WEAPON, 1));
    public static final FocusUpgradeType ICEBOULDER = new FocusUpgradeType(12, "iceboulder", new AspectList().add(Aspect.COLD, 1).add(Aspect.CRYSTAL, 1));

    public FrostFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BF" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 5204428;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_frost");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, SCATTERSHOT) ? COST_SCATTER : this.isUpgradedWith(focus, ICEBOULDER) ? COST_BOULDER : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return this.getUpgradeLevel(focus, SCATTERSHOT) <= 0 && this.getUpgradeLevel(focus, ICEBOULDER) <= 0 ? 200 : 500;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        if (level instanceof ServerLevel serverLevel && item.consumeAllVis(wand, player, this.getVisCost(wand), true, false)) {
            ItemStack focus = Wands.getFocusItem(wand);
            int potency = Wands.getFocusPotency(wand);
            int frosty = this.getUpgradeLevel(focus, FocusUpgradeType.ALCHEMISTS_FROST);
            FrostShard shard = null;
            if (this.isUpgradedWith(focus, SCATTERSHOT)) {
                for (int index = 0; index < 5 + potency * 2; index++) {
                    shard = FrostShard.create(serverLevel, player, 8.0F);
                    shard.setDamage(1.0F);
                    shard.setFragile(true);
                    shard.setFrosty(frosty);
                    serverLevel.addFreshEntity(shard);
                }
            } else if (this.isUpgradedWith(focus, ICEBOULDER)) {
                shard = FrostShard.create(serverLevel, player, 1.0F);
                shard.setDamage(4 + potency * 2);
                shard.setBounce(0.8);
                shard.bounceLimit = 6;
                shard.setFrosty(frosty);
                serverLevel.addFreshEntity(shard);
            } else {
                shard = FrostShard.create(serverLevel, player, 1.0F);
                shard.setDamage((float) (3.0 + potency * 1.5));
                shard.setFrosty(frosty);
                serverLevel.addFreshEntity(shard);
            }
            serverLevel.playSound(null, shard.getX(), shard.getY(), shard.getZ(), ModSounds.ICE.get(), SoundSource.PLAYERS, 0.4F, 1.0F + serverLevel.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FROST};
            case 2 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, SCATTERSHOT, ICEBOULDER, FocusUpgradeType.ALCHEMISTS_FROST};
            case 4 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FROST};
            default -> null;
        };
    }
}
