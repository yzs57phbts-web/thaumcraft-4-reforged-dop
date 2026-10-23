package thaumcraft.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.effect.EffectHooks;
import thaumcraft.effect.WarpMobEffect;
import thaumcraft.effect.WarpEffectEvents;
import thaumcraft.entity.monster.Pech;
import thaumcraft.entity.monster.InhabitedZombie;
import thaumcraft.entity.monster.eldritch.EldritchCrab;
import thaumcraft.menu.PechTradeMenu;
import thaumcraft.menu.PechTradeInventory;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModEntities;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpEvents;
import thaumcraft.research.WarpType;
import thaumcraft.wand.WandManager;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class WarpGameTests {
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void tempWarpDecayAndWard(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Knowledge.get(player);
        Warp.set(player, 2, WarpType.TEMPORARY);
        WarpEvents.checkWarpEvent(player);
        helper.assertValueEqual(Warp.get(player, WarpType.TEMPORARY), 1, "temporary warp should decay once per event check");
        Warp.set(player, 3, WarpType.TEMPORARY);
        player.tickCount = 1999;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertValueEqual(Warp.get(player, WarpType.TEMPORARY), 3, "temporary warp must not decay before the scheduled check");
        player.tickCount = 2000;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertValueEqual(Warp.get(player, WarpType.TEMPORARY), 2, "scheduled check must decay temporary warp once");
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.WARP_WARD, 100, 0));
        Warp.set(player, 2, WarpType.TEMPORARY);
        player.tickCount = 4000;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertValueEqual(Warp.get(player, WarpType.TEMPORARY), 2, "warp ward should suppress event and decay");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void visExhaustWandCost(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        EffectHooks.applyVisExhaust(player, 100, 1);
        float discount = WandManager.getTotalVisDiscount(player, Aspect.AIR);
        helper.assertTrue(Math.abs(discount + 0.2F) < 0.001F, "vis exhaust should add 20 percent wand cost at amplifier one");
        player.removeEffect(ModEffects.VIS_EXHAUST);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.INFECTIOUS_VIS_EXHAUST, 100, 2));
        discount = WandManager.getTotalVisDiscount(player, Aspect.AIR);
        helper.assertTrue(Math.abs(discount + 0.3F) < 0.001F, "infectious vis exhaust should add 30 percent wand cost at amplifier two");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void taintPoisonDamagesBat(GameTestHelper helper) {
        LivingEntity bat = EntityType.BAT.create(helper.getLevel());
        if (bat == null) {
            helper.fail("bat entity could not be created");
            return;
        }
        BlockPos pos = helper.absolutePos(BlockPos.ZERO.above());
        bat.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(bat);
        float health = bat.getHealth();
        ModEffects.TAINT_POISON.value().applyEffectTick(bat, 0);
        helper.assertTrue(bat.getHealth() < health, "taint poison should hurt an ordinary small non-undead mob");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpHungerEventBoundaries(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Knowledge.get(player);
        Warp.set(player, 3, WarpType.STICKY);
        try {
            var method = WarpEvents.class.getDeclaredMethod("applyWarpEvent", ServerPlayer.class, int.class, int.class);
            method.setAccessible(true);
            for (int effect : new int[] {73, 74, 75, 77, 78, 79, 80}) {
                method.invoke(null, player, 100, effect);
                helper.assertTrue(player.hasEffect(ModEffects.UNNATURAL_HUNGER), "effect " + effect + " should apply unnatural hunger");
                player.removeEffect(ModEffects.UNNATURAL_HUNGER);
            }
            method.invoke(null, player, 100, 76);
        } catch (ReflectiveOperationException exception) {
            helper.fail("warp event entry could not be invoked: " + exception.getMessage());
            return;
        }
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), 2, "effect 76 should remove one sticky warp");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void pechTradeOfferTables(GameTestHelper helper) {
        var registryAccess = helper.getLevel().registryAccess();
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 1, registryAccess).size(), 8, "normal value-one offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 2, registryAccess).size(), 4, "normal value-two offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 3, registryAccess).size(), 5, "normal value-three offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 4, registryAccess).size(), 1, "normal value-four offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 5, registryAccess).size(), 3, "normal value-five offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 1, registryAccess).size(), 8, "warrior value-one offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 2, registryAccess).size(), 3, "warrior value-two offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 3, registryAccess).size(), 4, "warrior value-three offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 4, registryAccess).size(), 7, "warrior value-four offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 5, registryAccess).size(), 5, "warrior value-five offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 1, registryAccess).size(), 16, "archer value-one offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 2, registryAccess).size(), 4, "archer value-two offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 3, registryAccess).size(), 5, "archer value-three offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 4, registryAccess).size(), 1, "archer value-four offer count");
        helper.assertValueEqual(PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 5, registryAccess).size(), 4, "archer value-five offer count");
        helper.assertTrue(PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 1, registryAccess).stream().noneMatch(stack -> stack.is(thaumcraft.registry.ModItems.COPPER_NUGGET)), "normal value-one offers should use native clusters, not standalone metal nuggets");
        var normalValueTwo = PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 2, registryAccess);
        helper.assertValueEqual(potionDuration(normalValueTwo.get(2)), 3600, "strength potion duration");
        helper.assertValueEqual(potionDuration(normalValueTwo.get(3)), 3600, "speed potion duration");
        var normalValueThree = PechTradeMenu.tradeOffers(Pech.PechType.NORMAL, 3, registryAccess);
        helper.assertValueEqual(potionDuration(normalValueThree.get(3)), 9600, "extended strength potion duration");
        helper.assertValueEqual(potionDuration(normalValueThree.get(4)), 9600, "night vision potion duration");
        var warriorValueTwo = PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 2, registryAccess);
        helper.assertValueEqual(potionDuration(warriorValueTwo.get(1)), 900, "regeneration potion duration");
        helper.assertValueEqual(potionDuration(warriorValueTwo.get(2)), 1, "instant health potion duration");
        var warriorValueThree = PechTradeMenu.tradeOffers(Pech.PechType.WARRIOR, 3, registryAccess);
        helper.assertValueEqual(potionDuration(warriorValueThree.get(2)), 450, "regeneration II potion duration");
        helper.assertValueEqual(potionAmplifier(warriorValueThree.get(2)), 1, "regeneration II amplifier");
        helper.assertValueEqual(potionDuration(warriorValueThree.get(3)), 1, "instant health II potion duration");
        helper.assertValueEqual(potionAmplifier(warriorValueThree.get(3)), 1, "instant health II amplifier");
        var archerValueThree = PechTradeMenu.tradeOffers(Pech.PechType.ARCHER, 3, registryAccess);
        helper.assertValueEqual(potionDuration(archerValueThree.get(2)), 9600, "invisibility potion duration");
        helper.assertValueEqual(potionDuration(archerValueThree.get(3)), 450, "archer regeneration II duration");
        helper.succeed();
    }

    private static int potionDuration(ItemStack stack) {
        return potionEffect(stack).getDuration();
    }

    private static int potionAmplifier(ItemStack stack) {
        return potionEffect(stack).getAmplifier();
    }

    private static MobEffectInstance potionEffect(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents.getAllEffects().iterator().next();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void pechDungeonRejection(GameTestHelper helper) {
        try {
            var contentsMethod = PechTradeMenu.class.getDeclaredMethod("dungeonOffers");
            contentsMethod.setAccessible(true);
            helper.assertValueEqual(((java.util.List<?>) contentsMethod.invoke(null)).size(), 38, "vanilla and Thaumcraft dungeon candidates");
            var rewardMethod = PechTradeMenu.class.getDeclaredMethod("dungeonReward", RandomSource.class, net.minecraft.core.RegistryAccess.class);
            rewardMethod.setAccessible(true);
            FixedRandomSource rejectionThenApple = new FixedRandomSource(0, 7);
            ItemStack apple = (ItemStack) rewardMethod.invoke(null, rejectionThenApple, helper.getLevel().registryAccess());
            helper.assertTrue(apple.is(Items.GOLDEN_APPLE), "dungeon selection should retry an ineligible uniform candidate");
            helper.assertValueEqual(rejectionThenApple.calls, 2, "dungeon selection should reject then accept");
            helper.assertValueEqual(rejectionThenApple.lastBound, 38, "dungeon candidate selection should use uniform array bounds");
            FixedRandomSource exhausted = new FixedRandomSource(0);
            ItemStack fallback = (ItemStack) rewardMethod.invoke(null, exhausted, helper.getLevel().registryAccess());
            helper.assertTrue(fallback.is(Items.SADDLE), "after 50 rejected picks the source returns the last non-null item");
            helper.assertValueEqual(exhausted.calls, 50, "dungeon candidate rejection should stop after 50 attempts");
        } catch (ReflectiveOperationException exception) {
            helper.fail("dungeon selection entry could not be invoked: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpDeathGazeCone(GameTestHelper helper) {
        try {
            var method = WarpEvents.class.getDeclaredMethod("isWithinDeathGazeCone", net.minecraft.world.phys.Vec3.class, net.minecraft.world.phys.Vec3.class, net.minecraft.world.phys.Vec3.class, double.class);
            method.setAccessible(true);
            net.minecraft.world.phys.Vec3 look = new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0);
            net.minecraft.world.phys.Vec3 origin = net.minecraft.world.phys.Vec3.ZERO;
            boolean outsideEuclideanRadius = (boolean) method.invoke(null, look, origin, new net.minecraft.world.phys.Vec3(2.9, 0.0, 7.5), 8.0);
            boolean outsideCone = (boolean) method.invoke(null, look, origin, new net.minecraft.world.phys.Vec3(3.0, 0.0, 7.5), 8.0);
            helper.assertTrue(outsideEuclideanRadius, "death gaze range is measured by axial projection, not Euclidean distance");
            helper.assertTrue(!outsideCone, "death gaze must reject targets outside the strict cone angle");
        } catch (ReflectiveOperationException exception) {
            helper.fail("death gaze geometry could not be checked: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void pechTradeNoLoss(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Pech pech = ModEntities.PECH.get().create(helper.getLevel());
        if (pech == null) {
            helper.fail("Pech entity could not be created");
            return;
        }
        BlockPos pos = helper.absolutePos(BlockPos.ZERO.above());
        pech.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(pech);
        pech.setTamed(true);
        pech.getRandom().setSeed(314159L);
        PechTradeInventory tradeInventory = new PechTradeInventory(pech);
        tradeInventory.setItem(0, new ItemStack(Items.EMERALD, 3));
        PechTradeMenu menu = new PechTradeMenu(0, player.getInventory(), pech.getId(), tradeInventory);
        helper.assertTrue(menu.clickMenuButton(player, 0), "valid trade should complete");
        helper.assertValueEqual(tradeInventory.getItem(0).getCount(), 2, "one input is consumed by the accepted trade");
        int outputCount = 0;
        for (int slot = 1; slot < 5; slot++) {
            outputCount += tradeInventory.getItem(slot).getCount();
        }
        helper.assertTrue(outputCount > 0, "accepted trade should produce rewards");
        helper.assertTrue(!menu.clickMenuButton(player, 0), "repeat button press must not consume input while output slots remain occupied");
        helper.assertValueEqual(tradeInventory.getItem(0).getCount(), 2, "rejected repeat must not consume input");
        int inventoryBeforeMove = inventoryCount(player);
        int dropsBeforeMove = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, player.getBoundingBox().inflate(8.0)).stream()
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
        ItemStack shifted = menu.quickMoveStack(player, 1);
        int movedCount = shifted.getCount();
        int unusedInputCount = tradeInventory.getItem(0).getCount();
        menu.removed(player);
        int retainedCount = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            retainedCount += player.getInventory().getItem(slot).getCount();
        }
        int droppedCount = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, player.getBoundingBox().inflate(8.0)).stream()
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
        helper.assertValueEqual(retainedCount + droppedCount - inventoryBeforeMove - dropsBeforeMove, outputCount + unusedInputCount, "shift-click plus menu close must conserve rewards and return unused input exactly once");
        helper.assertTrue(movedCount <= outputCount, "shift-click cannot duplicate output count");
        helper.assertTrue(!pech.isTrading(), "closing the menu releases Pech trading state");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpResearchRollThresholds(GameTestHelper helper) {
        try {
            var method = WarpEvents.class.getDeclaredMethod("checkWarpEvent", ServerPlayer.class, RandomSource.class);
            method.setAccessible(true);
            for (int warp : new int[] {11, 26, 51}) {
                ServerPlayer player = TestSupport.mockServerPlayer(helper);
                Knowledge.get(player).setWarpCounter(100);
                Warp.set(player, warp, WarpType.PERMANENT);
                method.invoke(null, player, new FixedRandomSource(0, 39, 0));
                if (warp == 11) {
                    helper.assertTrue(ResearchManager.isComplete(player, "BATHSALTS") || ResearchManager.isComplete(player, "@BATHSALTS"), "successful roll above 10 permanent warp should unlock bath salts");
                } else if (warp == 26) {
                    helper.assertTrue(ResearchManager.isComplete(player, "ELDRITCHMINOR"), "successful roll above 25 permanent warp should unlock minor eldritch research");
                } else {
                    helper.assertTrue(ResearchManager.isComplete(player, "ELDRITCHMAJOR"), "successful roll above 50 permanent warp should unlock major eldritch research");
                }
            }
            ServerPlayer failedRoll = TestSupport.mockServerPlayer(helper);
            Knowledge.get(failedRoll).setWarpCounter(100);
            Warp.set(failedRoll, 51, WarpType.PERMANENT);
            method.invoke(null, failedRoll, new FixedRandomSource(99));
            helper.assertTrue(!ResearchManager.isComplete(failedRoll, "ELDRITCHMAJOR"), "research must not unlock when the warp event roll fails");
        } catch (ReflectiveOperationException exception) {
            helper.fail("warp research event could not be invoked: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpWussGate(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        try {
            var method = WarpEvents.class.getDeclaredMethod("shouldSkipWarpEvents", ServerPlayer.class, boolean.class);
            method.setAccessible(true);
            helper.assertTrue((boolean) method.invoke(null, player, true), "WUSS mode must suppress warp events");
            helper.assertTrue(!(boolean) method.invoke(null, player, false), "normal mode without a ward must allow warp events");
            player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, 100, 0));
            helper.assertTrue((boolean) method.invoke(null, player, false), "warp ward must suppress warp events");
        } catch (ReflectiveOperationException exception) {
            helper.fail("warp event gate could not be inspected: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpEffectTicks(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        player.getFoodData().setFoodLevel(10);
        helper.assertTrue(ModEffects.UNNATURAL_HUNGER.value().shouldApplyEffectTickThisTick(100, 1), "unnatural hunger must tick every tick");
        ModEffects.UNNATURAL_HUNGER.value().applyEffectTick(player, 1);
        net.minecraft.nbt.CompoundTag foodOutput = new net.minecraft.nbt.CompoundTag();
        player.getFoodData().addAdditionalSaveData(foodOutput);
        float exhaustion = foodOutput.getFloat("foodExhaustionLevel");
        helper.assertTrue(exhaustion > 0.0F, "unnatural hunger should add food exhaustion");
        helper.assertTrue(!ModEffects.BLURRED_VISION.value().shouldApplyEffectTickThisTick(100, 0), "blurred vision is a client post effect and must not tick server-side");

        BlockPos fluxRelativePos = new BlockPos(3, 3, 3);
        BlockPos fluxAbsolutePos = helper.absolutePos(fluxRelativePos);
        LivingEntity fluxTarget = createMob(helper, EntityType.BAT, fluxAbsolutePos.getX() + 0.5, fluxAbsolutePos.getY(), fluxAbsolutePos.getZ() + 0.5);
        invokeRandomEffect("applyThaumarhia", helper, fluxTarget, findSeedForNextInt(15, 0));
        helper.assertTrue(helper.getBlockState(fluxRelativePos).is(thaumcraft.registry.ModBlocks.FLUX_GOO), "thaumarhia should create flux goo after a successful server roll");

        BlockPos darkRelativePos = new BlockPos(5, 3, 3);
        BlockPos darkAbsolutePos = helper.absolutePos(darkRelativePos);
        for (int relativeY = 0; relativeY <= 2; relativeY++) {
            helper.setBlock(darkRelativePos.above(relativeY), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
        helper.setBlock(darkRelativePos.above(3), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        LivingEntity darkTarget = createMob(helper, EntityType.BAT, darkAbsolutePos.getX() + 0.5, darkAbsolutePos.getY(), darkAbsolutePos.getZ() + 0.5);
        darkTarget.hurt(helper.getLevel().damageSources().generic(), 4.0F);
        helper.getLevel().setDayTime(18000L);
        helper.getLevel().updateSkyBrightness();
        helper.assertTrue(darkTarget.getLightLevelDependentMagicValue() < 0.25F, "sun scorned healing test target must be dark enough for its healing branch");
        float darkHealth = darkTarget.getHealth();
        invokeRandomEffect("applySunScorned", helper, darkTarget, findSeedForFloat(darkTarget.getLightLevelDependentMagicValue() * 2.0F, false));
        helper.assertTrue(darkTarget.getHealth() > darkHealth, "sun scorned should heal in darkness");

        helper.getLevel().setDayTime(1000L);
        helper.getLevel().updateSkyBrightness();
        BlockPos sunlitColumn = helper.absolutePos(new BlockPos(5, 0, 3));
        int sunlitY = helper.getLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, sunlitColumn.getX(), sunlitColumn.getZ());
        BlockPos sunlitAbsolutePos = new BlockPos(sunlitColumn.getX(), sunlitY, sunlitColumn.getZ());
        LivingEntity sunlitTarget = createMob(helper, EntityType.SHEEP, sunlitAbsolutePos.getX() + 0.5, sunlitAbsolutePos.getY(), sunlitAbsolutePos.getZ() + 0.5);
        helper.assertTrue(helper.getLevel().canSeeSky(sunlitTarget.blockPosition()), "sun scorned damage test target must have open sky");
        helper.assertTrue(sunlitTarget.getLightLevelDependentMagicValue() > 0.5F, "sun scorned damage test target must be bright enough for its daylight branch");
        float sunlitRollLimit = (sunlitTarget.getLightLevelDependentMagicValue() - 0.4F) * 2.0F / 30.0F;
        invokeRandomEffect("applySunScorned", helper, sunlitTarget, findSeedForFloat(sunlitRollLimit, true));
        helper.assertTrue(sunlitTarget.isOnFire(), "sun scorned should ignite an exposed entity under daylight");

        BlockPos gazeRelativePos = new BlockPos(3, 3, 3);
        BlockPos gazeAbsolutePos = helper.absolutePos(gazeRelativePos);
        player.setPos(gazeAbsolutePos.getX() + 0.5, gazeAbsolutePos.getY(), gazeAbsolutePos.getZ() + 0.5);
        helper.assertTrue(helper.getLevel().addFreshEntity(player), "death gaze player must be present in the test level");
        LivingEntity deathGazeTarget = createMob(helper, EntityType.SHEEP, gazeAbsolutePos.getX() + 0.5, gazeAbsolutePos.getY() + 1.0, gazeAbsolutePos.getZ() + 2.5);
        player.setYRot(0.0F);
        helper.assertTrue(player.hasLineOfSight(deathGazeTarget), "death gaze fixture target must have line of sight");
        net.minecraft.world.phys.Vec3 gazeTarget = new net.minecraft.world.phys.Vec3(
            deathGazeTarget.getX(),
            deathGazeTarget.getBoundingBox().minY + deathGazeTarget.getBbHeight() / 2.0,
            deathGazeTarget.getZ()
        );
        double gazeAlignment = player.getViewVector(1.0F).normalize().dot(gazeTarget.subtract(player.getEyePosition()).normalize());
        helper.assertTrue(gazeAlignment > Math.cos(0.75 / 2.0), "death gaze fixture target must be inside the cone, alignment=" + gazeAlignment);
        player.addEffect(new MobEffectInstance(ModEffects.DEATH_GAZE, 100, 0));
        try {
            var method = WarpEvents.class.getDeclaredMethod("checkDeathGaze", ServerPlayer.class);
            method.setAccessible(true);
            method.invoke(null, player);
        } catch (ReflectiveOperationException exception) {
            helper.fail("death gaze server effect could not be invoked: " + exception.getMessage());
            return;
        }
        helper.assertTrue(deathGazeTarget.hasEffect(net.minecraft.world.effect.MobEffects.WITHER), "death gaze should wither a visible target in its cone");

        LivingEntity tainted = ModEntities.THAUMIC_SLIME.get().create(helper.getLevel());
        helper.assertTrue(tainted != null, "tainted slime factory returned null");
        BlockPos taintedPos = helper.absolutePos(new BlockPos(7, 3, 3));
        tainted.setPos(taintedPos.getX() + 0.5, taintedPos.getY(), taintedPos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(tainted);
        float taintedHealth = tainted.getHealth();
        ModEffects.TAINT_POISON.value().applyEffectTick(tainted, 0);
        helper.assertTrue(tainted.getHealth() >= taintedHealth, "taint poison should not damage a tainted mob");
        helper.succeed();
    }

    private static LivingEntity createMob(GameTestHelper helper, EntityType<? extends LivingEntity> type, double x, double y, double z) {
        LivingEntity entity = type.create(helper.getLevel());
        if (entity == null) {
            helper.fail("failed to create effect test entity " + type);
            return null;
        }
        entity.setPos(x, y, z);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static long findSeedForNextInt(int bound, int expected) {
        for (long seed = 0; seed < 10000L; seed++) {
            if (RandomSource.create(seed).nextInt(bound) == expected) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed found for random bound " + bound + " and value " + expected);
    }

    private static long findSeedForFloat(float limit, boolean below) {
        for (long seed = 0; seed < 10000L; seed++) {
            float value = RandomSource.create(seed).nextFloat();
            if (below ? value < limit : value > limit) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed found for random float threshold " + limit);
    }

    private static void invokeRandomEffect(String methodName, GameTestHelper helper, LivingEntity entity, long seed) {
        try {
            var method = WarpMobEffect.class.getDeclaredMethod(methodName, net.minecraft.server.level.ServerLevel.class, LivingEntity.class);
            method.setAccessible(true);
            helper.getLevel().getRandom().setSeed(seed);
            method.invoke(null, helper.getLevel(), entity);
        } catch (ReflectiveOperationException exception) {
            helper.fail("warp effect roll could not be invoked: " + exception.getMessage());
        }
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void pechSaveLoadVariants(GameTestHelper helper) {
        for (Pech.PechType type : Pech.PechType.values()) {
            Pech original = ModEntities.PECH.get().create(helper.getLevel());
            helper.assertTrue(original != null, "Pech factory returned null for " + type);
            original.setPechType(type);
            original.setTamed(true);
            original.setLoot(2, new ItemStack(Items.ENDER_PEARL, 3));
            net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
            output = original.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            Pech loaded = ModEntities.PECH.get().create(helper.getLevel());
            helper.assertTrue(loaded != null, "Pech reload factory returned null for " + type);
            loaded.load(output);
            helper.assertTrue(loaded.getPechType() == type, "Pech type was not preserved for " + type);
            helper.assertTrue(loaded.isTamed(), "Pech tamed state was not preserved for " + type);
            helper.assertValueEqual(loaded.getLoot(2).getCount(), 3, "Pech stored loot was not preserved for " + type);
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void pechTradeBoundaries(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Pech pech = ModEntities.PECH.get().create(helper.getLevel());
        helper.assertTrue(pech != null, "Pech factory returned null");
        BlockPos pos = helper.absolutePos(BlockPos.ZERO.above());
        pech.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(pech);
        pech.setTamed(true);
        PechTradeInventory inventory = new PechTradeInventory(pech);
        PechTradeMenu menu = new PechTradeMenu(0, player.getInventory(), pech.getId(), inventory);
        helper.assertTrue(!menu.clickMenuButton(player, 0), "empty trade input must be rejected");
        inventory.setItem(0, new ItemStack(Items.STONE));
        helper.assertTrue(!menu.clickMenuButton(player, 0), "unvalued trade input must be rejected");
        helper.assertValueEqual(inventory.getItem(0).getCount(), 1, "rejected trade must preserve input");
        inventory.setItem(0, new ItemStack(Items.EMERALD, 2));
        inventory.setItem(1, new ItemStack(Items.APPLE));
        helper.assertTrue(!menu.clickMenuButton(player, 0), "occupied output slot must reject the trade atomically");
        helper.assertValueEqual(inventory.getItem(0).getCount(), 2, "occupied output rejection must preserve all input");
        inventory.setItem(1, ItemStack.EMPTY);
        pech.setTamed(false);
        helper.assertTrue(!menu.clickMenuButton(player, 0), "untamed Pech must reject trade");
        helper.assertValueEqual(inventory.getItem(0).getCount(), 2, "untamed trade rejection must preserve all input");
        menu.removed(player);
        helper.assertTrue(!pech.isTrading(), "closing an invalid trade menu must release trading state");
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        PechTradeInventory fullInventory = new PechTradeInventory(pech);
        fullInventory.setItem(1, new ItemStack(Items.APPLE, 3));
        PechTradeMenu fullMenu = new PechTradeMenu(1, player.getInventory(), pech.getId(), fullInventory);
        helper.assertTrue(fullMenu.quickMoveStack(player, 1).isEmpty(), "shift-click must leave rewards in output when the player inventory is full");
        helper.assertValueEqual(fullInventory.getItem(1).getCount(), 3, "failed shift-click must preserve every output");
        fullMenu.removed(player);
        int droppedApples = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, player.getBoundingBox().inflate(8.0)).stream()
            .filter(item -> item.getItem().is(Items.APPLE))
            .mapToInt(item -> item.getItem().getCount())
            .sum();
        helper.assertValueEqual(droppedApples, 3, "closing a full-inventory menu must drop each unclaimed reward once");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void inhabitedZombieSpawnsCrab(GameTestHelper helper) {
        InhabitedZombie zombie = ModEntities.INHABITED_ZOMBIE.get().create(helper.getLevel());
        helper.assertTrue(zombie != null, "InhabitedZombie factory returned null");
        BlockPos pos = helper.absolutePos(BlockPos.ZERO.above());
        zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(zombie);
        zombie.setHealth(1.0F);
        zombie.hurt(helper.getLevel().damageSources().generic(), 4.0F);
        helper.assertTrue(!zombie.isAlive(), "lethal damage should start the InhabitedZombie conversion");
        awaitInhabitedCrab(helper, pos, 0);
    }

    private static void awaitInhabitedCrab(GameTestHelper helper, BlockPos pos, int waitedTicks) {
        helper.runAfterDelay(1, () -> {
            List<EldritchCrab> crabs = helper.getLevel().getEntitiesOfClass(EldritchCrab.class, new net.minecraft.world.phys.AABB(pos).inflate(8.0));
            if (crabs.isEmpty() && waitedTicks < 40) {
                awaitInhabitedCrab(helper, pos, waitedTicks + 1);
                return;
            }
            helper.assertTrue(!crabs.isEmpty(), "death tick must spawn an Eldritch Crab from the InhabitedZombie within 40 ticks");
            helper.assertTrue(crabs.stream().anyMatch(EldritchCrab::hasHelm), "crab born from an InhabitedZombie must carry its helm");
            helper.succeed();
        });
    }

    private static final class FixedRandomSource implements RandomSource {
        private final int[] values;
        private int index;
        private int calls;
        private int lastBound;

        private FixedRandomSource(int... values) {
            this.values = values;
        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {
            index = 0;
        }

        @Override
        public int nextInt() {
            return nextInt(Integer.MAX_VALUE);
        }

        @Override
        public int nextInt(int bound) {
            calls++;
            lastBound = bound;
            int value = values[Math.min(index++, values.length - 1)];
            return Math.floorMod(value, bound);
        }

        @Override
        public long nextLong() {
            return nextInt();
        }

        @Override
        public boolean nextBoolean() {
            return nextInt(2) != 0;
        }

        @Override
        public float nextFloat() {
            return nextInt(1000) / 1000.0F;
        }

        @Override
        public double nextDouble() {
            return nextInt(1000) / 1000.0;
        }

        @Override
        public double nextGaussian() {
            return 0.0;
        }
    }
    @GameTest(template = "test/platform", timeoutTicks = 400)
    public static void warpHungerCures(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        WarpEffectEvents.addUncurableEffect(player, new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, 2000, 2));
        player.addEffect(new MobEffectInstance(ModEffects.BLURRED_VISION, 100, 0));
        for (var effect : List.of(ModEffects.VIS_EXHAUST, ModEffects.THAUMARHIA, ModEffects.SUN_SCORNED, ModEffects.INFECTIOUS_VIS_EXHAUST, ModEffects.DEATH_GAZE)) {
            WarpEffectEvents.addUncurableEffect(player, new MobEffectInstance(effect, 100, 0));
        }
        drinkMilk(player);
        MobEffectInstance hunger = player.getEffect(ModEffects.UNNATURAL_HUNGER);
        helper.assertTrue(hunger != null, "milk must not cure unnatural hunger");
        helper.assertTrue(!player.hasEffect(ModEffects.BLURRED_VISION), "milk should still cure blurred vision");
        for (var effect : List.of(ModEffects.VIS_EXHAUST, ModEffects.THAUMARHIA, ModEffects.SUN_SCORNED, ModEffects.INFECTIOUS_VIS_EXHAUST, ModEffects.DEATH_GAZE)) {
            helper.assertTrue(player.hasEffect(effect), "milk should not cure legacy non-curable effect " + effect);
            player.removeEffect(effect);
        }
        ServerPlayer mergedDefault = TestSupport.mockServerPlayer(helper);
        mergedDefault.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 100, 0));
        WarpEffectEvents.addUncurableEffect(mergedDefault, new MobEffectInstance(ModEffects.VIS_EXHAUST, 200, 1));
        drinkMilk(mergedDefault);
        helper.assertTrue(!mergedDefault.hasEffect(ModEffects.VIS_EXHAUST), "effect merge should retain the existing default curative policy");
        ServerPlayer infectedPlayer = TestSupport.mockServerPlayer(helper);
        BlockPos infectionPos = helper.absolutePos(new BlockPos(3, 3, 3));
        infectedPlayer.setPos(infectionPos.getX() + 0.5, infectionPos.getY(), infectionPos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(infectedPlayer);
        LivingEntity source = createMob(helper, EntityType.BAT, infectionPos.getX() + 2.0, infectionPos.getY(), infectionPos.getZ() + 0.5);
        source.addEffect(new MobEffectInstance(ModEffects.INFECTIOUS_VIS_EXHAUST, 100, 0));
        ModEffects.INFECTIOUS_VIS_EXHAUST.value().applyEffectTick(source, 0);
        helper.assertTrue(infectedPlayer.hasEffect(ModEffects.VIS_EXHAUST), "infectious effect should spread an ordinary vis exhaustion instance");
        drinkMilk(infectedPlayer);
        helper.assertTrue(!infectedPlayer.hasEffect(ModEffects.VIS_EXHAUST), "milk should cure vis exhaustion acquired through secondary infection");
        WarpEffectEvents.addUncurableEffect(player, new MobEffectInstance(ModEffects.VIS_EXHAUST, 100, 0));
        player.removeEffect(ModEffects.VIS_EXHAUST);
        player.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 100, 0));
        drinkMilk(player);
        helper.assertTrue(!player.hasEffect(ModEffects.VIS_EXHAUST), "removing and restoring an effect instance should reset its source curative policy");
        NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(
            player, new ItemStack(Items.ROTTEN_FLESH), 32, ItemStack.EMPTY
        ));
        hunger = player.getEffect(ModEffects.UNNATURAL_HUNGER);
        helper.assertTrue(hunger != null, "rotten flesh should leave lower unnatural hunger active");
        helper.assertValueEqual(hunger.getDuration(), 1400, "rotten flesh should remove 600 ticks");
        helper.assertValueEqual(hunger.getAmplifier(), 1, "rotten flesh should reduce amplifier by one");
        NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(
            player, new ItemStack(Items.COOKED_BEEF), 32, ItemStack.EMPTY
        ));
        helper.assertValueEqual(player.getEffect(ModEffects.UNNATURAL_HUNGER).getDuration(), 1400, "ordinary food should not reduce unnatural hunger");
        NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(
            player, new ItemStack(thaumcraft.registry.ModItems.ZOMBIE_BRAIN.get()), 32, ItemStack.EMPTY
        ));
        hunger = player.getEffect(ModEffects.UNNATURAL_HUNGER);
        helper.assertTrue(hunger != null, "brain should leave unnatural hunger active");
        helper.assertValueEqual(hunger.getDuration(), 800, "zombie brain should remove another 600 ticks");
        helper.assertValueEqual(hunger.getAmplifier(), 0, "zombie brain should reduce amplifier by one");
        helper.succeed();
    }

    private static void drinkMilk(ServerPlayer player) {
        ItemStack milk = new ItemStack(Items.MILK_BUCKET);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, milk);
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        ItemStack result = milk.finishUsingItem(player.level(), player);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, result);
        player.stopUsingItem();
    }

    private static int inventoryCount(ServerPlayer player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            count += player.getInventory().getItem(slot).getCount();
        }
        return count;
    }
}
