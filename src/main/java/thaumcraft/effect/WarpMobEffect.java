package thaumcraft.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.EntityTypeTags;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEffects;
import thaumcraft.damage.ModDamageTypes;

public final class WarpMobEffect extends MobEffect {
    private final Kind kind;

    public WarpMobEffect(Kind kind, int color) {
        super(kind == Kind.WARP_WARD ? MobEffectCategory.BENEFICIAL : MobEffectCategory.HARMFUL, color);
        this.kind = kind;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        ServerLevel level = entity.level() instanceof ServerLevel serverLevel ? serverLevel : null;
        switch (kind) {
            case SUN_SCORNED -> { if (level != null) applySunScorned(level, entity); }
            case THAUMARHIA -> { if (level != null) applyThaumarhia(level, entity); }
            case UNNATURAL_HUNGER -> {
                if (entity instanceof Player player) {
                    player.causeFoodExhaustion(0.025F * (amplifier + 1));
                }
            }
            case TAINT_POISON -> { if (level != null) applyTaintPoison(level, entity); }
            case INFECTIOUS_VIS_EXHAUST -> { if (level != null) spreadVisExhaust(level, entity, amplifier); }
            case BLURRED_VISION, DEATH_GAZE, VIS_EXHAUST, WARP_WARD -> {
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return switch (kind) {
            case UNNATURAL_HUNGER -> true;
            case SUN_SCORNED, INFECTIOUS_VIS_EXHAUST -> duration % 40 == 0;
            case THAUMARHIA -> duration % 20 == 0;
            case TAINT_POISON -> duration % Math.max(1, 40 >> amplifier) == 0;
            case BLURRED_VISION, DEATH_GAZE, VIS_EXHAUST, WARP_WARD -> false;
        };
    }

    private static void applySunScorned(ServerLevel level, LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        float brightness = entity.getLightLevelDependentMagicValue();
        if (brightness > 0.5F
            && level.getRandom().nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F
            && level.canSeeSky(pos)) {
            entity.igniteForSeconds(4.0F);
        } else if (brightness < 0.25F && level.getRandom().nextFloat() > brightness * 2.0F) {
            entity.heal(1.0F);
        }
    }

    private static void applyThaumarhia(ServerLevel level, LivingEntity entity) {
        if (level.getRandom().nextInt(15) != 0) {
            return;
        }
        BlockPos pos = entity.blockPosition();
        if (level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().defaultBlockState());
        }
    }

    private static void applyTaintPoison(ServerLevel level, LivingEntity entity) {
        if (EffectHooks.isTainted(entity)) {
            entity.heal(1.0F);
            return;
        }
        if (entity.getType().getTags().anyMatch(EntityTypeTags.UNDEAD::equals)) {
            return;
        }
        entity.hurt(level.damageSources().source(ModDamageTypes.TAINT), 1.0F);
    }

    private static void spreadVisExhaust(ServerLevel level, LivingEntity source, int amplifier) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(4.0))) {
            if (target == source || target.hasEffect(ModEffects.INFECTIOUS_VIS_EXHAUST)) {
                continue;
            }
            if (amplifier > 0) {
                target.addEffect(new MobEffectInstance(ModEffects.INFECTIOUS_VIS_EXHAUST, 6000, amplifier - 1, false, true, true));
            } else {
                target.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 6000, 0, false, true, true));
            }
        }
    }

    public enum Kind {
        BLURRED_VISION,
        DEATH_GAZE,
        VIS_EXHAUST,
        SUN_SCORNED,
        THAUMARHIA,
        UNNATURAL_HUNGER,
        WARP_WARD,
        TAINT_POISON,
        INFECTIOUS_VIS_EXHAUST
    }
}
