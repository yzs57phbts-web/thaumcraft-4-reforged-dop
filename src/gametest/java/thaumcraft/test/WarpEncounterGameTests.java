package thaumcraft.test;

import java.util.List;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.eldritch.EldritchGuardian;
import thaumcraft.entity.monster.eldritch.MindSpider;
import thaumcraft.registry.ModEntities;
import thaumcraft.research.WarpEncounterHooks;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class WarpEncounterGameTests {
    @GameTest(template = "test/platform", timeoutTicks = 240, batch = "warp_encounter_guardian")
    public static void guardianSpawner(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        positionPlayer(helper, player);
        clearPlatform(helper);
        BlockPos origin = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        long seed = 109L;
        BlockPos blockedPos = candidate(origin, seed);
        helper.getLevel().getRandom().setSeed(seed);
        WarpEncounterHooks.spawnGuardians(helper.getLevel(), player, 1);
        helper.assertTrue(entitiesNear(helper, ModEntities.ELDRITCH_GUARDIAN.get(), blockedPos).isEmpty(), "guardian spawned without a sturdy support block");

        long validSeed = findSeed(origin, helper.getLevel().getMinBuildHeight(), helper.getLevel().getMaxBuildHeight());
        BlockPos validPos = candidate(origin, validSeed);
        clearCandidate(helper, validPos);
        helper.getLevel().setBlockAndUpdate(validPos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().getRandom().setSeed(validSeed);
        WarpEncounterHooks.spawnGuardians(helper.getLevel(), player, 1);
        List<EldritchGuardian> guardians = entitiesNear(helper, ModEntities.ELDRITCH_GUARDIAN.get(), validPos);
        helper.assertValueEqual(guardians.size(), 1, "installed encounter spawner did not create one guardian at the valid candidate");
        helper.assertTrue(guardians.getFirst().getTarget() == player, "guardian did not target the triggering player");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240, batch = "warp_encounter_spider")
    public static void mindSpiderSpawner(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        positionPlayer(helper, player);
        clearPlatform(helper);
        BlockPos origin = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        long blockedSeed = 109L;
        BlockPos blockedPos = candidate(origin, blockedSeed);
        helper.getLevel().getRandom().setSeed(blockedSeed);
        WarpEncounterHooks.spawnMindSpiders(helper.getLevel(), player, true, 1);
        helper.assertTrue(entitiesNear(helper, ModEntities.MIND_SPIDER.get(), blockedPos).isEmpty(), "mind spider spawned without a sturdy support block");

        long harmlessSeed = findSeed(origin, helper.getLevel().getMinBuildHeight(), helper.getLevel().getMaxBuildHeight());
        BlockPos harmlessPos = candidate(origin, harmlessSeed);
        clearCandidate(helper, harmlessPos);
        helper.getLevel().setBlockAndUpdate(harmlessPos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().getRandom().setSeed(harmlessSeed);
        WarpEncounterHooks.spawnMindSpiders(helper.getLevel(), player, true, 1);
        List<MindSpider> harmlessSpiders = entitiesNear(helper, ModEntities.MIND_SPIDER.get(), harmlessPos);
        helper.assertValueEqual(harmlessSpiders.size(), 1, "installed encounter spawner did not create one harmless spider at the valid candidate");
        MindSpider harmless = harmlessSpiders.getFirst();
        helper.assertTrue(harmless.isHarmless(), "harmless encounter flag was not applied");
        helper.assertValueEqual(harmless.getViewer(), player.getUUID().toString(), "harmless spider viewer UUID was not assigned");
        helper.assertTrue(harmless.getTarget() == player, "harmless spider did not retain its encounter target");
        MindSpider harmlessReloaded = reload(helper, harmless);
        helper.assertTrue(harmlessReloaded.isHarmless(), "harmless flag was not persisted");
        helper.assertValueEqual(harmlessReloaded.getViewer(), player.getUUID().toString(), "viewer UUID was not persisted");

        long hostileSeed = findSeed(origin, helper.getLevel().getMinBuildHeight(), helper.getLevel().getMaxBuildHeight(), harmlessSeed + 1L);
        BlockPos hostilePos = candidate(origin, hostileSeed);
        clearCandidate(helper, hostilePos);
        helper.getLevel().setBlockAndUpdate(hostilePos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().getRandom().setSeed(hostileSeed);
        WarpEncounterHooks.spawnMindSpiders(helper.getLevel(), player, false, 1);
        List<MindSpider> hostileSpiders = entitiesNear(helper, ModEntities.MIND_SPIDER.get(), hostilePos);
        helper.assertValueEqual(hostileSpiders.size(), 1, "installed encounter spawner did not create one hostile spider at the valid candidate");
        MindSpider hostile = hostileSpiders.getFirst();
        helper.assertFalse(hostile.isHarmless(), "hostile encounter was marked harmless");
        helper.assertTrue(hostile.getViewer().isEmpty(), "hostile spider retained a harmless viewer UUID");
        helper.assertTrue(hostile.getTarget() == player, "hostile spider did not target the triggering player");
        MindSpider hostileReloaded = reload(helper, hostile);
        helper.assertFalse(hostileReloaded.isHarmless(), "hostile flag was not persisted");
        helper.assertTrue(hostileReloaded.getViewer().isEmpty(), "hostile viewer state was not persisted");
        helper.succeed();
    }


    private static void positionPlayer(GameTestHelper helper, ServerPlayer player) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
    }

    private static long findSeed(BlockPos origin, int minBuildHeight, int maxBuildHeight) {
        return findSeed(origin, minBuildHeight, maxBuildHeight, 0L);
    }

    private static long findSeed(BlockPos origin, int minBuildHeight, int maxBuildHeight, long startSeed) {
        for (long seed = startSeed; seed < startSeed + 10000L; seed++) {
            BlockPos pos = candidate(origin, seed);
            if (Math.abs(pos.getX() - origin.getX()) <= 12
                && pos.getX() != origin.getX()
                && Math.abs(pos.getY() - origin.getY()) <= 12
                && pos.getY() != origin.getY()
                && Math.abs(pos.getZ() - origin.getZ()) <= 12
                && pos.getZ() != origin.getZ()
                && pos.getY() > minBuildHeight + 1
                && pos.getY() < maxBuildHeight - 2) {
                return seed;
            }
        }
        throw new IllegalStateException("No in-range encounter candidate seed was found");
    }

    private static void clearCandidate(GameTestHelper helper, BlockPos pos) {
        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 3, 2))) helper.getLevel().setBlockAndUpdate(check, Blocks.AIR.defaultBlockState());
    }

    private static void clearPlatform(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static BlockPos candidate(BlockPos origin, long seed) {
        RandomSource random = RandomSource.create(seed);
        return origin.offset(offset(random), offset(random), offset(random));
    }

    private static int offset(RandomSource random) {
        return (random.nextInt(18) + 7) * (random.nextInt(3) - 1);
    }

    private static <T extends Entity> List<T> entitiesNear(GameTestHelper helper, net.minecraft.world.entity.EntityType<T> type, BlockPos pos) {
        return helper.getLevel().getEntities(type, new AABB(pos).inflate(1.0), Entity::isAlive);
    }

    private static MindSpider reload(GameTestHelper helper, MindSpider spider) {
        net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
        output = spider.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        MindSpider loaded = ModEntities.MIND_SPIDER.get().create(helper.getLevel());
        helper.assertTrue(loaded != null, "mind spider reload factory returned null");
        loaded.load(output);
        return loaded;
    }
}
