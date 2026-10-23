package thaumcraft.wand.focus;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.entity.CombatUtils;
import thaumcraft.entity.projectile.ShockOrb;
import thaumcraft.fx.Fx;
import thaumcraft.network.ShockZapPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.Wands;

public class ShockFocus extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.AIR, 25);
    private static final AspectList COST_CHAIN = new AspectList().add(Aspect.AIR, 40).add(Aspect.WATER, 10);
    private static final AspectList COST_GROUND = new AspectList().add(Aspect.AIR, 75).add(Aspect.EARTH, 25);

    public static final FocusUpgradeType CHAINLIGHTNING = new FocusUpgradeType(17, "chainlightning", new AspectList().add(Aspect.WEATHER, 1));
    public static final FocusUpgradeType EARTHSHOCK = new FocusUpgradeType(18, "earthshock", new AspectList().add(Aspect.WEATHER, 1));

    public ShockFocus(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BL" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 10466239;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return Thaumcraft.id("item/focus_shock");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, CHAINLIGHTNING) ? COST_CHAIN : this.isUpgradedWith(focus, EARTHSHOCK) ? COST_GROUND : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return this.isUpgradedWith(focus, CHAINLIGHTNING) ? 500 : this.isUpgradedWith(focus, EARTHSHOCK) ? 1000 : 250;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focus) {
        return this.isUpgradedWith(focus, EARTHSHOCK) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        WandItem item = (WandItem) wand.getItem();
        ItemStack focus = Wands.getFocusItem(wand);
        if (this.isUpgradedWith(focus, EARTHSHOCK)) {
            if (!item.consumeAllVis(wand, player, this.getVisCost(wand), !level.isClientSide(), false)) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                ShockOrb orb = ShockOrb.create(serverLevel, player);
                orb.area += this.getUpgradeLevel(focus, FocusUpgradeType.ENLARGE) * 2;
                orb.damage = (int) (orb.damage + Wands.getFocusPotency(wand) * 1.33);
                serverLevel.addFreshEntity(orb);
                serverLevel.playSound(
                    null,
                    orb.getX(),
                    orb.getY(),
                    orb.getZ(),
                    ModSounds.ZAP.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F + (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2F
                );
            }
            return InteractionResult.SUCCESS;
        }
        CombatUtils.startUsingWand(player, wand);
        WandManager.setCooldown(player, wand, -1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        this.doLightningBolt(wand, player);
    }

    public void doLightningBolt(ItemStack wand, Player player) {
        WandItem item = (WandItem) wand.getItem();
        Level level = player.level();
        if (!item.consumeAllVis(wand, player, this.getVisCost(wand), !level.isClientSide(), false)) {
            player.stopUsingItem();
            return;
        }
        ItemStack focus = Wands.getFocusItem(wand);
        int potency = Wands.getFocusPotency(wand);
        Entity pointed = CombatUtils.getPointedEntity(level, player, 0.0, 20.0, 1.1F);
        if (!(level instanceof ServerLevel serverLevel)) {
            this.drawBolt(level, player, pointed);
            return;
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SHOCK.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
        if (!(pointed instanceof LivingEntity target) || !canTarget(serverLevel, target)) {
            return;
        }
        int chain = this.getUpgradeLevel(focus, CHAINLIGHTNING) * 2;
        target.hurt(serverLevel.damageSources().playerAttack(player), (chain > 0 ? 6 : 4) + potency);
        if (chain <= 0) {
            return;
        }
        chain += this.getUpgradeLevel(focus, FocusUpgradeType.ENLARGE) * 2;
        LivingEntity center = target;
        List<Integer> targets = new ArrayList<>();
        targets.add(target.getId());
        while (chain > 0) {
            chain--;
            double best = Double.MAX_VALUE;
            LivingEntity closest = null;
            for (Entity entity : CombatUtils.getEntitiesInRange(serverLevel, center.getX(), center.getY(), center.getZ(), player, LivingEntity.class, 8.0)) {
                if (!targets.contains(entity.getId()) && canTarget(serverLevel, entity)) {
                    double distance = entity.distanceToSqr(center);
                    if (distance < best) {
                        closest = (LivingEntity) entity;
                        best = distance;
                    }
                }
            }
            if (closest != null) {
                ModNetwork.sendToNear(serverLevel, null, center.getX(), center.getY(), center.getZ(), 64.0, new ShockZapPayload(center.getId(), closest.getId()));
                targets.add(closest.getId());
                closest.hurt(serverLevel.damageSources().playerAttack(player), 4 + potency);
                center = closest;
            }
        }
    }

    private static boolean canTarget(ServerLevel level, Entity entity) {
        return !(entity instanceof Player) || level.getServer().isPvpAllowed();
    }

    private void drawBolt(Level level, Player player, @Nullable Entity pointed) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double x = eye.x + look.x * 10.0;
        double y = eye.y + look.y * 10.0;
        double z = eye.z + look.z * 10.0;
        BlockHitResult block = level.clip(new ClipContext(eye, eye.add(look.scale(10.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            x = block.getLocation().x;
            y = block.getLocation().y;
            z = block.getLocation().z;
            this.sparkles(level, x, y, z, 0.3F);
        }
        if (pointed != null) {
            x = pointed.getX();
            y = pointed.getBoundingBox().minY + pointed.getBbHeight() / 2.0F;
            z = pointed.getZ();
            this.sparkles(level, x, y, z, 0.6F);
        }
        Fx.get().shockBolt(level, player, x, y, z);
    }

    private void sparkles(Level level, double x, double y, double z, float spread) {
        for (int index = 0; index < 5; index++) {
            Fx.get().sparkle(
                level,
                (float) x + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * spread,
                (float) y + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * spread,
                (float) z + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * spread,
                2.0F + level.getRandom().nextFloat(),
                2,
                0.05F + level.getRandom().nextFloat() * 0.05F
            );
        }
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.ENLARGE) || this.isUpgradedWith(focus, CHAINLIGHTNING) || this.isUpgradedWith(focus, EARTHSHOCK);
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return switch (rank) {
            case 1, 2 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, CHAINLIGHTNING, EARTHSHOCK};
            case 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE};
            default -> null;
        };
    }
}
