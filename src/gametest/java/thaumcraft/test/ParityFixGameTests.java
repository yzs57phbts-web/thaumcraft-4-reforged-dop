package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.block.LootBlock;
import thaumcraft.entity.projectile.PrimalOrb;
import thaumcraft.entity.monster.ChampionModifier;
import thaumcraft.platform.BiomeEdit;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.BlockSwapper;
import thaumcraft.wand.focus.ExcavationFocus;
import thaumcraft.wand.focus.FocusKeys;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumcraft.wand.focus.FocusUtils;
import thaumcraft.wand.focus.TradeFocus;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ParityFixGameTests {
    private ParityFixGameTests() {
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void tradePreservesRareLootTier(GameTestHelper helper) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos lootOffset = new BlockPos(2, 3, 2);
        BlockPos targetOffset = new BlockPos(3, 3, 2);
        BlockState rareUrn = ModBlocks.LOOT_URN.get().defaultBlockState().setValue(LootBlock.TIER, LootBlock.Tier.RARE);
        helper.setBlock(lootOffset, rareUrn);
        helper.setBlock(targetOffset, Blocks.STONE);
        ItemStack wand = Wands.create(WandRegistry.CAP_IRON, WandRegistry.ROD_WOOD, false);
        Wands.setFocus(wand, new ItemStack(ModItems.FOCUS_EQUAL_TRADE.get()));
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        BlockPos loot = helper.absolutePos(lootOffset);
        player.setPos(loot.getX() + 0.5, loot.getY() - 1.0, loot.getZ() - 1.0);
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.setShiftKeyDown(true);
        TradeFocus focus = (TradeFocus) ModItems.FOCUS_EQUAL_TRADE.get();
        BlockHitResult hit = FocusUtils.getTargetBlock(level, player, false, player.blockInteractionRange());
        helper.assertTrue(hit != null, "shift-pick ray did not hit a block");
        helper.assertValueEqual(hit.getBlockPos(), loot, "shift-pick ray hit the wrong block");
        helper.assertValueEqual(focus.onFocusRightClick(wand, level, player, null), InteractionResult.SUCCESS, "shift-pick did not complete");
        player.setShiftKeyDown(false);
        ItemStack picked = TradeFocus.getPickedBlock(wand);
        helper.assertTrue(!picked.isEmpty(), "shift-pick did not store a block stack");
        helper.assertValueEqual(LootBlock.stateForStack(picked).getValue(LootBlock.TIER), LootBlock.Tier.RARE, "shift-pick did not capture rare tier");
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var saved = ItemStack.CODEC.encodeStart(ops, wand).getOrThrow();
        ItemStack loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();
        ItemStack loadedPick = TradeFocus.getPickedBlock(loaded);
        helper.assertTrue(!loadedPick.isEmpty(), "picked stack disappeared after save/load");
        helper.assertValueEqual(LootBlock.stateForStack(loadedPick).getValue(LootBlock.TIER), LootBlock.Tier.RARE, "picked rare tier did not survive stack save/load");
        Wands.fill(loaded);
        player.getInventory().setItem(0, loaded);
        player.getInventory().setItem(1, loadedPick.copy());
        player.getInventory().selected = 0;
        BlockPos target = helper.absolutePos(targetOffset);
        BlockSwapper.add(level, target, level.getBlockState(target), loadedPick, 0, player, 0);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getBlockState(targetOffset).is(ModBlocks.LOOT_URN.get()), "swap did not place the urn");
            helper.assertValueEqual(helper.getBlockState(targetOffset).getValue(LootBlock.TIER), LootBlock.Tier.RARE, "swap lost the rare tier");
            BlockPos variantOffset = new BlockPos(4, 3, 2);
            helper.setBlock(variantOffset, ModBlocks.LOOT_URN.get().defaultBlockState().setValue(LootBlock.TIER, LootBlock.Tier.UNCOMMON));
            player.getInventory().setItem(1, loadedPick.copy());
            BlockPos variant = helper.absolutePos(variantOffset);
            BlockSwapper.add(level, variant, level.getBlockState(variant), loadedPick, 0, player, 0);
            helper.runAfterDelay(2, () -> {
                helper.assertValueEqual(helper.getBlockState(variantOffset).getValue(LootBlock.TIER), LootBlock.Tier.RARE, "same-block swap rejected or lost a different loot tier");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void excavationKeepsSharedLazyCost(GameTestHelper helper) {
        ExcavationFocus focus = ModItems.FOCUS_EXCAVATION.get();
        ItemStack silk = new ItemStack(focus);
        FocusKeys.UPGRADES.set(silk, List.of(FocusUpgradeType.SILKTOUCH.id()));
        ItemStack dowsing = new ItemStack(focus);
        FocusKeys.UPGRADES.set(dowsing, List.of(ExcavationFocus.DOWSING.id()));
        AspectList first = focus.getVisCost(silk);
        AspectList second = focus.getVisCost(dowsing);
        AspectList reverseFirst = focus.getVisCost(dowsing);
        AspectList reverseSecond = focus.getVisCost(silk);
        helper.assertTrue(first == second && first == reverseFirst && first == reverseSecond, "silk touch and dowsing did not share the lazy cost object");
        boolean silkPrice = first.getAmount(Aspect.AIR) == 1 && first.getAmount(Aspect.FIRE) == 1 && first.getAmount(Aspect.EARTH) == 16
            && first.getAmount(Aspect.WATER) == 1 && first.getAmount(Aspect.ORDER) == 1 && first.getAmount(Aspect.ENTROPY) == 1;
        boolean dowsingPrice = first.getAmount(Aspect.AIR) == 0 && first.getAmount(Aspect.FIRE) == 2 && first.getAmount(Aspect.EARTH) == 15
            && first.getAmount(Aspect.WATER) == 0 && first.getAmount(Aspect.ORDER) == 2 && first.getAmount(Aspect.ENTROPY) == 0;
        helper.assertTrue(silkPrice || dowsingPrice, "first excavation cost was neither original silk nor dowsing cost");
        AspectList base = focus.getVisCost(new ItemStack(focus));
        helper.assertValueEqual(base.getAmount(Aspect.EARTH), 15, "base excavation cost changed");
        helper.assertValueEqual(base.size(), 1, "base excavation cost gained upgraded aspects");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void manipulatorCancellationDrainsOnceWithoutReplacementUpgrade(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 3, 2);
        helper.setBlock(pos, ModBlocks.FOCAL_MANIPULATOR.get());
        FocalManipulatorBlockEntity table = (FocalManipulatorBlockEntity) helper.getBlockEntity(pos);
        ItemStack original = new ItemStack(ModItems.FOCUS_EXCAVATION.get());
        table.setItem(0, original);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.experienceLevel = 100;
        helper.assertTrue(table.startCraft(FocusUpgradeType.FRUGAL.id(), player), "manipulator did not start the test upgrade");
        AspectList pending = table.getAspects().copy();
        ItemStack removed = table.removeItem(0, 1);
        helper.assertTrue(!removed.isEmpty(), "manipulator did not return the removed focus");
        helper.assertTrue(table.getAspects().visSize() > 0, "inventory extraction discarded pending final drain");
        for (Aspect aspect : pending.getAspects()) {
            helper.assertValueEqual(table.getAspects().getAmount(aspect), pending.getAmount(aspect), "inventory extraction changed pending vis");
        }
        ItemStack replacement = new ItemStack(ModItems.FOCUS_EXCAVATION.get());
        table.setItem(0, replacement);
        for (int tick = 0; tick < 5; tick++) {
            FocalManipulatorBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), table);
        }
        FocusItem focus = (FocusItem) replacement.getItem();
        helper.assertTrue(!focus.isUpgradedWith(replacement, FocusUpgradeType.FRUGAL), "replacement focus received cancelled upgrade");
        helper.assertValueEqual(table.getSize(), 0, "cancelled replacement craft did not finish");
        ItemStack secondFocus = new ItemStack(ModItems.FOCUS_EXCAVATION.get());
        table.setItem(0, secondFocus);
        helper.assertTrue(table.startCraft(FocusUpgradeType.FRUGAL.id(), player), "manipulator did not start the second test upgrade");
        AspectList secondPending = table.getAspects().copy();
        ItemStack removedWithoutUpdate = table.removeItemNoUpdate(0);
        helper.assertTrue(!removedWithoutUpdate.isEmpty(), "no-update inventory extraction did not return the focus");
        helper.assertTrue(table.getAspects().visSize() > 0, "no-update inventory extraction discarded pending final drain");
        for (Aspect aspect : secondPending.getAspects()) {
            helper.assertValueEqual(table.getAspects().getAmount(aspect), secondPending.getAmount(aspect), "no-update inventory extraction changed pending vis");
        }
        for (int tick = 0; tick < 5; tick++) {
            FocalManipulatorBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), table);
        }
        helper.assertValueEqual(table.getSize(), 0, "no-update extraction did not complete the cancelled craft after its final drain");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void championWhitelistDoesNotBoostLevelZeroRoll(GameTestHelper helper) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        Difficulty previousDifficulty = level.getDifficulty();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        Mob zombie = (Mob) EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(zombie != null, "zombie factory returned null");
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        BiomeEdit.setColumn(level, pos, level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
        ChampionModifier.processSpawn(zombie, 0);
        helper.assertTrue(ChampionModifier.get(zombie) >= 0, "level-zero whitelist incorrectly raised the spawn roll above zero");
        level.getServer().setDifficulty(previousDifficulty, true);
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void researchTableFindsBrainJarWithInjectedRandom(GameTestHelper helper) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos tableOffset = new BlockPos(8, 4, 8);
        BlockPos jarOffset = tableOffset.west(8);
        helper.setBlock(tableOffset, ModBlocks.RESEARCH_TABLE.get());
        helper.setBlock(jarOffset, ModBlocks.BRAIN_JAR.get());
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getBlockEntity(tableOffset);
        BlockPos tablePos = helper.absolutePos(tableOffset);
        BrainJarRandomSource random = new BrainJarRandomSource();
        for (int tick = 0; tick < 602; tick++) {
            ResearchTableBlockEntity.serverTick(level, tablePos, helper.getBlockState(tableOffset), table, random);
        }
        helper.assertTrue(random.brainJarRolls > 0, "research table did not perform the Brain Jar chance roll");
        helper.assertValueEqual(table.getBonusAspects().visSize(), 1, "Brain Jar did not add exactly one bonus aspect");
        helper.assertValueEqual(table.getBonusAspects().size(), 1, "Brain Jar bonus was not a single aspect");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void primalOrbTaintsBiomeAndPlacesFibres(GameTestHelper helper) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos orbPos = helper.absolutePos(new BlockPos(8, 3, 8));
        int centerX = (int) (orbPos.getX() + 0.5);
        int centerZ = (int) (orbPos.getZ() + 0.5);
        int[][] heights = new int[6][6];
        var plains = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                int worldX = centerX - 5 + x;
                int worldZ = centerZ - 5 + z;
                BlockPos column = new BlockPos(worldX, 0, worldZ);
                helper.assertTrue(level.hasChunkAt(column), "test taint column is not loaded");
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                level.setBlock(new BlockPos(worldX, surfaceY - 1, worldZ), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(new BlockPos(worldX, surfaceY, worldZ), Blocks.AIR.defaultBlockState(), 3);
                BiomeEdit.setColumn(level, new BlockPos(worldX, surfaceY, worldZ), plains);
                heights[x][z] = surfaceY;
            }
        }
        PrimalOrb orb = ModEntities.PRIMAL_ORB.get().create(level);
        helper.assertTrue(orb != null, "failed to create primal orb");
        orb.setPos(orbPos.getX() + 0.5, orbPos.getY(), orbPos.getZ() + 0.5);
        long seed = 0L;
        orb.getRandom().setSeed(seed);
        orb.taintSplosion();
        int taintedColumns = 0;
        int fibreBlocks = 0;
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                BlockPos surface = new BlockPos(centerX - 5 + x, heights[x][z], centerZ - 5 + z);
                if (TaintBiomes.isTainted(level, surface)) {
                    taintedColumns++;
                }
                if (level.getBlockState(surface).is(ModBlocks.FIBROUS_TAINT.get())) {
                    fibreBlocks++;
                }
            }
        }
        helper.assertTrue(taintedColumns > 0, "primal orb did not taint any biome columns");
        helper.assertTrue(fibreBlocks > 0, "primal orb did not place fibres over supported surface");
        BlockState[][] states = new BlockState[6][6];
        boolean[][] tainted = new boolean[6][6];
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                BlockPos surface = new BlockPos(centerX - 5 + x, heights[x][z], centerZ - 5 + z);
                states[x][z] = level.getBlockState(surface);
                tainted[x][z] = TaintBiomes.isTainted(level, surface);
            }
        }
        orb.getRandom().setSeed(seed);
        orb.taintSplosion();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                BlockPos surface = new BlockPos(centerX - 5 + x, heights[x][z], centerZ - 5 + z);
                helper.assertValueEqual(level.getBlockState(surface), states[x][z], "repeat taint burst changed an already processed block");
                helper.assertValueEqual(TaintBiomes.isTainted(level, surface), tainted[x][z], "repeat taint burst changed an already tainted biome column");
            }
        }
        helper.succeed();
    }

    private static final class BrainJarRandomSource implements RandomSource {
        private int brainJarRolls;

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
        }

        @Override
        public int nextInt() {
            return 1;
        }

        @Override
        public int nextInt(int bound) {
            if (bound == 200) {
                brainJarRolls++;
                return 0;
            }
            return Math.min(1, bound - 1);
        }

        @Override
        public long nextLong() {
            return 1L;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return 0.5F;
        }

        @Override
        public double nextDouble() {
            return 0.5;
        }

        @Override
        public double nextGaussian() {
            return 0.0;
        }
    }
}
