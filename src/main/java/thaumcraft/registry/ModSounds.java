package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Thaumcraft.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> URNBREAK = register("urnbreak");
    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = register("coins");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND = register("wand");
    public static final DeferredHolder<SoundEvent, SoundEvent> WANDFAIL = register("wandfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = register("zap");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFTFAIL = register("craftfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL = register("crystal");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERATICKS = register("cameraticks");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAR = register("jar");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERACLACK = register("cameraclack");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = register("learn");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRITE = register("write");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERASE = register("erase");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAGE = register("page");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPERS = register("whispers");
    public static final DeferredHolder<SoundEvent, SoundEvent> HHOFF = register("hhoff");
    public static final DeferredHolder<SoundEvent, SoundEvent> HHON = register("hhon");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEY = register("key");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = register("bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPILL = register("spill");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALEMBICKNOCK = register("alembicknock");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAK = register("creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUEEK = register("squeek");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = register("tool");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP = register("pump");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER = register("infuser");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSERSTART = register("infuserstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFTSTART = register("craftstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> GORE = register("gore");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_IRON_SHOOT = register("golemironshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> UPGRADE = register("upgrade");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRELOOP = register("fireloop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE = register("ice");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACOBS = register("jacobs");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = register("shock");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLE = register("rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_EFFECT = register("runic_shield_effect");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_CHARGE = register("runic_shield_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = register("wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWING = register("swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOORFAIL = register("doorfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRAIN = register("brain");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = register("heartbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROOTS = register("roots");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_IDLE = register("pech_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_HIT = register("pech_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DEATH = register("pech_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_TRADE = register("pech_trade");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_CHARGE = register("pech_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISPLIVE = register("wisplive");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISPDEAD = register("wispdead");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLE = register("tentacle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM = register("swarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM_ATTACK = register("swarmattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_CLAW = register("crabclaw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_DEATH = register("crabdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_TALK = register("crabtalk");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELDRITCH_GUARDIAN_IDLE = register("egidle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELDRITCH_GUARDIAN_ATTACK = register("egattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELDRITCH_GUARDIAN_DEATH = register("egdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELDRITCH_GUARDIAN_SCREECH = register("egscreech");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHANT = register("chant");
    public static final DeferredHolder<SoundEvent, SoundEvent> EVIL_PORTAL = register("evilportal");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLY = register("fly");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Thaumcraft.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
