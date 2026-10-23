package thaumcraft.test;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.ChampionModifier;
import thaumcraft.entity.AspectOrb;
import thaumcraft.node.NodeHooks;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModEntities;
import thaumcraft.wand.Wands;
import thaumcraft.world.gen.HalloweenFirebatSpawnModifier;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ChampionGameTests {
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void modifiersApplyOnce(GameTestHelper helper) {
        helper.assertValueEqual(ChampionModifier.count(), 13, "champion modifier count");
        helper.assertValueEqual(ChampionModifier.names().getFirst(), "bold", "champion modifier id 0");
        helper.assertValueEqual(ChampionModifier.names().get(12), "infested", "champion modifier id 12");
        helper.assertTrue(HalloweenFirebatSpawnModifier.isHalloween(10, 31), "Halloween seasonal spawn date");
        helper.assertFalse(HalloweenFirebatSpawnModifier.isHalloween(10, 30), "seasonal spawn must not include October 30");
        helper.assertFalse(HalloweenFirebatSpawnModifier.isHalloween(11, 1), "seasonal spawn must not include November 1");

        var zombieType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.withDefaultNamespace("zombie"));
        for (int id = 0; id < ChampionModifier.count(); id++) {
            Mob mob = (Mob) zombieType.create(helper.getLevel());
            helper.assertTrue(mob != null, "zombie factory returned null for champion id " + id);
            BlockPos spawnPos = helper.absolutePos(new BlockPos(2 + id % 4, 3, 2 + id / 4));
            mob.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0.0F, 0.0F);
            double baseDamage = mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
            ChampionModifier.assign(mob, id, false);
            ChampionModifier.assign(mob, id, false);
            helper.assertValueEqual(ChampionModifier.get(mob), id, "champion attachment id " + id);
            helper.assertValueEqual(mob.getAttributeValue(Attributes.MAX_HEALTH), mob.getAttributeBaseValue(Attributes.MAX_HEALTH) + 30.0, "champion health bonus id " + id);
            double expectedDamage = baseDamage * (id == 3 ? 12.0 : 3.0);
            helper.assertValueEqual(mob.getAttributeValue(Attributes.ATTACK_DAMAGE), expectedDamage, "champion attack modifier id " + id);
            if (id == 0) {
                helper.assertValueEqual(mob.getAttributeValue(Attributes.MOVEMENT_SPEED), mob.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) * 1.3, "bold speed bonus");
            }
            helper.getLevel().addFreshEntity(mob);
        }
        Mob saved = (Mob) zombieType.create(helper.getLevel());
        ChampionModifier.assign(saved, 10, true);
        net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
        output = saved.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        Mob loaded = (Mob) zombieType.create(helper.getLevel());
        loaded.load(output);
        helper.assertValueEqual(ChampionModifier.get(loaded), 10, "champion modifier survives entity save and load");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void championCombatAndRewards(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombieType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.withDefaultNamespace("zombie"));
        var sheepType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.withDefaultNamespace("sheep"));
        Mob attacker = (Mob) zombieType.create(level);
        Mob victim = (Mob) sheepType.create(level);
        Mob undeadVictim = (Mob) zombieType.create(level);
        Vec3 attackerPos = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 3, 3)));
        Vec3 victimPos = Vec3.atCenterOf(helper.absolutePos(new BlockPos(5, 3, 3)));
        attacker.setPos(attackerPos.x, attackerPos.y, attackerPos.z);
        victim.setPos(victimPos.x, victimPos.y, victimPos.z);
        undeadVictim.setPos(victimPos.x, victimPos.y, victimPos.z + 1.0);
        ChampionModifier.assign(attacker, 4, false);
        ChampionModifier.applyAttackEffect(4, attacker, victim, 4.0F, 0.39F, 0);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.WITHER), "grim applies wither below the 40 percent threshold");
        victim.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
        ChampionModifier.applyAttackEffect(4, attacker, victim, 4.0F, 0.40F, 0);
        helper.assertFalse(victim.hasEffect(net.minecraft.world.effect.MobEffects.WITHER), "grim does not apply wither at the threshold");
        ChampionModifier.applyAttackEffect(9, attacker, victim, 4.0F, 0.1F, 0);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER), "sickly applies hunger");
        ChampionModifier.applyAttackEffect(10, attacker, victim, 4.0F, 0.1F, 0);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "venomous applies poison");
        ChampionModifier.applyAttackEffect(10, attacker, undeadVictim, 4.0F, 0.1F, 0);
        helper.assertFalse(undeadVictim.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "venomous respects undead poison immunity");
        attacker.setHealth(1.0F);
        ChampionModifier.applyAttackEffect(11, attacker, victim, 4.0F, 0.99F, 0);
        helper.assertValueEqual(attacker.getHealth(), 3.0F, "vampiric heals by half of damage with a two point minimum");

        java.util.List<ItemEntity> drops = new java.util.ArrayList<>();
        ChampionModifier.awardChampionRewards(level, attacker, drops, 0, 1, 0);
        helper.assertValueEqual(drops.size(), 1, "champion awards one loot bag");
        helper.assertTrue(drops.getFirst().getItem().is(thaumcraft.registry.ModItems.COMMON_LOOT_BAG.get()), "fixed low roll awards common bag");
        int xp = level.getEntitiesOfClass(ExperienceOrb.class, attacker.getBoundingBox().inflate(4.0)).stream().mapToInt(orb -> orb.getValue() * orb.count).sum();
        helper.assertValueEqual(xp, 6, "champion awards fixed XP amount for deterministic roll");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void aspectOrbPersistencePickupAndNode(GameTestHelper helper) {
        var level = helper.getLevel();
        AspectOrb orb = AspectOrb.create(level, helper.absolutePos(new BlockPos(3, 3, 3)).getX() + 0.5, helper.absolutePos(new BlockPos(3, 3, 3)).getY() + 0.5, helper.absolutePos(new BlockPos(3, 3, 3)).getZ() + 0.5, Aspect.FIRE, 1);
        helper.assertTrue(orb != null, "aspect orb factory creates an entity");
        orb.tick();
        net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
        output = orb.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        AspectOrb loaded = ModEntities.ASPECT_ORB.get().create(level);
        loaded.load(output);
        helper.assertTrue(loaded.getAspect() == Aspect.FIRE, "aspect orb saves and restores its aspect");
        helper.assertValueEqual(loaded.getAspectValue(), 1, "aspect orb saves and restores its amount");
        helper.assertValueEqual(loaded.getAge(), 1, "aspect orb saves and restores lifetime");

        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        ItemStack wand = Wands.create(thaumcraft.wand.WandRegistry.cap("iron"), thaumcraft.wand.WandRegistry.rod("greatwood"), false);
        player.getInventory().setItem(0, wand);
        loaded.setPos(player.getX(), player.getY(), player.getZ());
        loaded.playerTouch(player);
        helper.assertTrue(loaded.isRemoved(), "touching a primal orb with wand space consumes it");
        helper.assertValueEqual(Wands.getVis(wand, Aspect.FIRE), 100, "one vis picked up is stored as 100 centivis");

        Wands.storeVis(wand, Aspect.FIRE, Wands.getMaxVis(wand) - 200);
        AspectOrb partial = AspectOrb.create(level, player.getX(), player.getY(), player.getZ(), Aspect.FIRE, 7);
        partial.playerTouch(player);
        helper.assertFalse(partial.isRemoved(), "recent XP pickup cooldown blocks an immediate second orb pickup");
        ServerPlayer partialPlayer = TestSupport.mockServerPlayer(helper);
        ItemStack partialWand = Wands.create(thaumcraft.wand.WandRegistry.cap("iron"), thaumcraft.wand.WandRegistry.rod("greatwood"), false);
        partialPlayer.getInventory().setItem(0, partialWand);
        Wands.storeVis(partialWand, Aspect.FIRE, Wands.getMaxVis(partialWand) - 200);
        AspectOrb partialCapacity = AspectOrb.create(level, partialPlayer.getX(), partialPlayer.getY(), partialPlayer.getZ(), Aspect.FIRE, 7);
        partialCapacity.playerTouch(partialPlayer);
        helper.assertTrue(partialCapacity.isRemoved(), "partial wand capacity still attracts and consumes the orb");
        helper.assertValueEqual(Wands.getVis(partialWand, Aspect.FIRE), Wands.getMaxVis(partialWand), "partial pickup caps the wand at capacity");

        BlockPos nodePos = helper.absolutePos(new BlockPos(2, 3, 5));
        helper.assertTrue(NodeHooks.spawnAspectOrb(level, nodePos, Aspect.AIR, 3), "node hook successfully spawns aspect orb");
        helper.assertTrue(!level.getEntitiesOfClass(AspectOrb.class, new net.minecraft.world.phys.AABB(nodePos).inflate(2.0)).isEmpty(), "node orb is present near node");
        helper.succeed();
    }

}
