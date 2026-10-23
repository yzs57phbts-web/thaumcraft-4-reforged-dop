package thaumcraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.eldritch.EldritchBlock;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKeys;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.entity.golem.GolemType;
import thaumcraft.registry.EldritchContent;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AuditFixSmoke {
    private static boolean started;
    private static int ticks;
    private static int captures;

    private static void capture(String name) {
        var minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.getMainRenderTarget(), message -> captures++);
    }

    private static void sprites() {
        var minecraft = Minecraft.getInstance();
        for (String name : List.of("focus_pech_depth", "focus_primal_depth", "focus_portablehole_depth", "focus_warding_depth")) {
            var sprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Thaumcraft.id("item/" + name));
            var contents = sprite.contents();
            int scale = name.equals("focus_primal_depth") ? 8 : 2;
            int frames = switch (name) {
                case "focus_primal_depth" -> 30;
                case "focus_portablehole_depth" -> 16;
                default -> 9;
            };
            if (contents.width() % 16 != 0 || contents.height() % 16 != 0 || contents.byMipLevel.length != 5) {
                throw new IllegalStateException("Sprite still limits the block atlas mipmaps: " + contents);
            }
            if (contents.getUniqueFrames().count() != frames) throw new IllegalStateException("Animation frames lost");
            var resource = minecraft.getResourceManager().getResource(Thaumcraft.id("textures/item/" + name + ".png")).orElseThrow();
            try (var stream = resource.open(); var original = NativeImage.read(stream)) {
                var loaded = contents.getOriginalImage();
                if (loaded.getWidth() != original.getWidth() * scale || loaded.getHeight() != original.getHeight() * scale) {
                    throw new IllegalStateException("Unexpected sprite scale");
                }
                for (int y = 0; y < loaded.getHeight(); y++) {
                    for (int x = 0; x < loaded.getWidth(); x++) {
                        if (loaded.getPixelRGBA(x, y) != original.getPixelRGBA(x / scale, y / scale)) {
                            throw new IllegalStateException("Source sprite pixels changed");
                        }
                    }
                }
            } catch (java.io.IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
        System.out.println("AUDIT24_SPRITES_PASS mip_levels=4 source_pixels=exact animation_frames=9,30,16,9");
    }

    private static void scene(ServerPlayer player) {
        var level = player.serverLevel();
        level.setDayTime(6000);
        for (BlockPos pos : BlockPos.betweenClosed(-9, 99, -6, 9, 99, 7)) {
            level.setBlockAndUpdate(pos, Blocks.STONE_BRICKS.defaultBlockState());
        }
        BlockPos furnace = new BlockPos(-1, 100, 0);
        BlockPos jar = new BlockPos(1, 100, 0);
        level.setBlockAndUpdate(furnace, ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState());
        level.setBlockAndUpdate(furnace.above(), ModBlocks.ALEMBIC.get().defaultBlockState());
        level.setBlockAndUpdate(jar, ModBlocks.WARDED_JAR.get().defaultBlockState());
        var golem = new GolemBase(ModEntities.GOLEM.get(), level);
        golem.configure(GolemType.IRON, new GolemSnapshot(6, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        golem.setHome(furnace.south(), Direction.SOUTH);
        golem.setPos(-0.5, 100, 1.5);
        level.addFreshEntity(golem);
        ItemStack bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
        player.setItemInHand(InteractionHand.MAIN_HAND, bell);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        player.gameMode.useItemOn(player, level, bell, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(jar).add(0, 0, 0.5), Direction.SOUTH, jar, false));
        BlockPos obelisk = new BlockPos(-5, 100, -2);
        var lower = EldritchContent.ELDRITCH_OBELISK.get().defaultBlockState();
        level.setBlockAndUpdate(obelisk, lower);
        for (int y = 1; y <= 4; y++) level.setBlockAndUpdate(obelisk.above(y), lower.setValue(EldritchBlock.PART, DoubleBlockHalf.UPPER));
        player.connection.teleport(0.5, 101, 6.5, 180, 18);
        player.containerMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("thaumcraft.auditFixSmoke")) return;
        var minecraft = Minecraft.getInstance();
        minecraft.options.pauseOnLostFocus = false;
        if (!started && minecraft.getOverlay() == null && minecraft.screen != null) {
            started = true;
            minecraft.options.renderDistance().set(5);
            minecraft.options.simulationDistance().set(5);
            minecraft.options.hideGui = false;
            var rules = new GameRules();
            rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
            minecraft.createWorldOpenFlows().createFreshLevel("audit24-" + System.currentTimeMillis(),
                new LevelSettings("Audit fixes validation", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                new WorldOptions(4181, false, false),
                registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), minecraft.screen);
        }
        if (minecraft.level == null || minecraft.player == null || minecraft.getOverlay() != null) return;
        ticks++;
        if (ticks == 5) minecraft.setScreen(null);
        if (ticks == 10) {
            var id = minecraft.player.getUUID();
            minecraft.getSingleplayerServer().execute(() -> scene(minecraft.getSingleplayerServer().getPlayerList().getPlayer(id)));
        }
        if (ticks == 80) {
            var id = minecraft.player.getUUID();
            minecraft.getSingleplayerServer().execute(() -> {
                var player = minecraft.getSingleplayerServer().getPlayerList().getPlayer(id);
                var selected = GolemKeys.BELL.get(player.getMainHandItem());
                if (selected != null && player.serverLevel().getEntity(selected.entity()) instanceof GolemBase golem) {
                    player.interactOn(golem, InteractionHand.MAIN_HAND);
                }
            });
        }
        if (ticks == 100) {
            sprites();
            var selection = GolemKeys.BELL.get(minecraft.player.getMainHandItem());
            if (selection == null || selection.markers().size() != 1) throw new IllegalStateException("Creative bell selection not synchronized");
            if (!selection.homeBlock().equals(new BlockPos(-1, 100, 0)) || selection.homeFacing() != Direction.SOUTH) throw new IllegalStateException("Source overlay is not anchored to the furnace face");
            capture("audit24-creative-main-hand");
        }
        if (ticks == 120) {
            var id = minecraft.player.getUUID();
            minecraft.getSingleplayerServer().execute(() -> {
                var player = minecraft.getSingleplayerServer().getPlayerList().getPlayer(id);
                ItemStack bell = player.getMainHandItem();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND, bell);
                player.containerMenu.broadcastChanges();
            });
        }
        if (ticks == 150) {
            var selection = GolemKeys.BELL.get(minecraft.player.getOffhandItem());
            if (selection == null || selection.markers().size() != 1) throw new IllegalStateException("Offhand bell selection lost");
            capture("audit24-creative-off-hand");
        }
        if (ticks == 180) {
            if (captures != 2) throw new IllegalStateException("Screenshots missing");
            System.out.println("AUDIT24_CLIENT_PASS creative=main,offhand screenshots=" + captures);
            minecraft.stop();
        }
    }
}
