package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.effect.WarpMobEffect;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Thaumcraft.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> BLURRED_VISION = register("blurred_vision", WarpMobEffect.Kind.BLURRED_VISION, 8421504);
    public static final DeferredHolder<MobEffect, MobEffect> DEATH_GAZE = register("death_gaze", WarpMobEffect.Kind.DEATH_GAZE, 6702131);
    public static final DeferredHolder<MobEffect, MobEffect> VIS_EXHAUST = register("vis_exhaust", WarpMobEffect.Kind.VIS_EXHAUST, 6702199);
    public static final DeferredHolder<MobEffect, MobEffect> SUN_SCORNED = register("sun_scorned", WarpMobEffect.Kind.SUN_SCORNED, 16308330);
    public static final DeferredHolder<MobEffect, MobEffect> THAUMARHIA = register("thaumarhia", WarpMobEffect.Kind.THAUMARHIA, 6702199);
    public static final DeferredHolder<MobEffect, MobEffect> UNNATURAL_HUNGER = register("unnatural_hunger", WarpMobEffect.Kind.UNNATURAL_HUNGER, 4482611);
    public static final DeferredHolder<MobEffect, MobEffect> WARP_WARD = register("warp_ward", WarpMobEffect.Kind.WARP_WARD, 14742263);
    public static final DeferredHolder<MobEffect, MobEffect> TAINT_POISON = register("taint_poison", WarpMobEffect.Kind.TAINT_POISON, 6697847);
    public static final DeferredHolder<MobEffect, MobEffect> INFECTIOUS_VIS_EXHAUST = register("infectious_vis_exhaust", WarpMobEffect.Kind.INFECTIOUS_VIS_EXHAUST, 6706551);

    private ModEffects() {
    }

    private static DeferredHolder<MobEffect, MobEffect> register(String name, WarpMobEffect.Kind kind, int color) {
        return EFFECTS.register(name, () -> new WarpMobEffect(kind, color));
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
