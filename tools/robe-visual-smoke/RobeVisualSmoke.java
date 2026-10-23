package robe_visual_smoke;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import thaumcraft.client.render.armor.RobeSkinLayers;
import thaumcraft.registry.ModItems;

@Mod("robe_visual_smoke")
public class RobeVisualSmoke {
    private static final List<Sample> SAMPLES = new ArrayList<>();
    private static boolean started;
    private static int ticks;
    private static int checkedRenders;

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new IllegalStateException("ROBE_VISUAL_FAILURE " + label);
        }
    }

    private static class Sample extends RemotePlayer {
        final String label;
        final boolean hood;
        final boolean chest;
        final boolean legs;
        final PlayerSkin skin;

        Sample(String label, ResourceLocation texture, boolean slim, boolean voidRobe, boolean hood, boolean chest, boolean legs) {
            super(Minecraft.getInstance().level, new GameProfile(UUID.randomUUID(), "robe-preview"));
            this.label = label;
            this.hood = hood;
            this.chest = chest;
            this.legs = legs;
            this.skin = new PlayerSkin(texture, null, null, null, slim ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, true);
            if (hood) setItemSlot(EquipmentSlot.HEAD, new ItemStack(voidRobe ? ModItems.VOID_ROBE_HELMET.get() : ModItems.CRIMSON_CULT_HOOD.get()));
            if (chest) setItemSlot(EquipmentSlot.CHEST, new ItemStack(voidRobe ? ModItems.VOID_ROBE_CHESTPLATE.get() : ModItems.CRIMSON_CULT_ROBE.get()));
            if (legs) setItemSlot(EquipmentSlot.LEGS, new ItemStack(voidRobe ? ModItems.VOID_ROBE_LEGGINGS.get() : ModItems.CRIMSON_CULT_LEGGINGS.get()));
        }

        @Override public PlayerSkin getSkin() { return skin; }
        @Override public boolean isModelPartShown(PlayerModelPart part) { return true; }
    }

    private static ResourceLocation testSkin() {
        NativeImage image = new NativeImage(64, 64, false);
        for (int x = 0; x < 64; x++) for (int y = 0; y < 64; y++) image.setPixelRGBA(x, y, 0xffa05030);
        rectangle(image, 32, 0, 64, 16, 0xff00ff00); // Fully opaque outer hat: green.
        rectangle(image, 16, 32, 40, 48, 0xffffff00); // Jacket: cyan.
        rectangle(image, 40, 32, 56, 48, 0xffff00ff); // Right sleeve: magenta.
        rectangle(image, 48, 48, 64, 64, 0xffff00ff); // Left sleeve: magenta.
        rectangle(image, 0, 32, 16, 64, 0xff00ffff); // Pants: yellow.
        rectangle(image, 10, 10, 11, 11, 0xffffffff);
        rectangle(image, 13, 10, 14, 11, 0xffffffff);
        return Minecraft.getInstance().getTextureManager().register("robe_overlay_regression", new DynamicTexture(image));
    }

    private static void rectangle(NativeImage image, int x1, int y1, int x2, int y2, int color) {
        for (int x = x1; x < x2; x++) for (int y = y1; y < y2; y++) image.setPixelRGBA(x, y, color);
    }

    private static void scene() {
        ResourceLocation texture = testSkin();
        SAMPLES.add(new Sample("Cultist / classic", texture, false, false, true, true, true));
        SAMPLES.add(new Sample("Void / classic", texture, false, true, true, true, true));
        SAMPLES.add(new Sample("Cultist / slim", texture, true, false, true, true, true));
        SAMPLES.add(new Sample("Void / slim", texture, true, true, true, true, true));
        SAMPLES.add(new Sample("Hood only", texture, false, false, true, false, false));
        SAMPLES.add(new Sample("Chest only", texture, false, true, false, true, false));
        SAMPLES.add(new Sample("Legs only", texture, false, true, false, false, true));
        SAMPLES.add(new Sample("No armor / restored", texture, false, false, false, false, false));
    }

    private static void screenshot(String name) throws Exception {
        Minecraft minecraft = Minecraft.getInstance();
        Path output = minecraft.gameDirectory.toPath().resolve("evidence");
        Files.createDirectories(output);
        Screenshot.grab(output.toFile(), name, minecraft.getMainRenderTarget(), message -> {});
    }

    @EventBusSubscriber(modid = "robe_visual_smoke", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) throws Exception {
            Minecraft minecraft = Minecraft.getInstance();
            if (!started && minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
                started = true;
                GameRules rules = new GameRules();
                rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
                minecraft.createWorldOpenFlows().createFreshLevel("robe-preview-" + System.currentTimeMillis(),
                    new LevelSettings("Robe overlay preview", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(29, false, false), registry -> registry.registryOrThrow(Registries.WORLD_PRESET)
                        .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), minecraft.screen);
            }
            if (minecraft.player == null || minecraft.getSingleplayerServer() == null) return;
            if (SAMPLES.isEmpty()) {
                scene();
                minecraft.setScreen(new Preview());
            }
            if (!(minecraft.screen instanceof Preview)) return;
            ticks++;
            if (ticks == 30) screenshot("robes-idle.png");
            if (ticks == 40) {
                for (Sample sample : SAMPLES) {
                    sample.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    sample.startUsingItem(InteractionHand.MAIN_HAND);
                }
            }
            if (ticks == 70) screenshot("robes-raised-arms.png");
            if (ticks == 85) {
                check(checkedRenders >= 16, "actual classic/slim player renders checked");
                System.out.println("ROBE_VISUAL_OK checked-player-renders=" + checkedRenders);
                minecraft.stop();
            }
        }

        @SubscribeEvent
        public static void rendered(RenderPlayerEvent.Post event) {
            if (!(event.getEntity() instanceof Sample sample)) return;
            PlayerModel<?> model = event.getRenderer().getModel();
            check(model.hat.visible == !sample.hood, sample.label + " hat visibility");
            check(model.jacket.visible == !sample.chest, sample.label + " jacket visibility");
            check(model.leftSleeve.visible == !sample.chest && model.rightSleeve.visible == !sample.chest, sample.label + " sleeve visibility");
            check(model.leftPants.visible == !sample.legs && model.rightPants.visible == !sample.legs, sample.label + " pants visibility");
            check(model.head.visible && model.body.visible && model.leftArm.visible && model.rightArm.visible, "base skin must remain visible");
            checkedRenders++;
        }
    }

    private static class Preview extends Screen {
        Preview() { super(Component.literal("Robe overlay regression")); }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff252b34);
            graphics.drawCenteredString(font, "Opaque test skin: green hat, cyan jacket, pink sleeves, yellow pants", width / 2, 8, 0xffffffff);
            int cellWidth = width / 4;
            int cellHeight = (height - 30) / 2;
            for (int index = 0; index < SAMPLES.size(); index++) {
                Sample sample = SAMPLES.get(index);
                int x = index % 4 * cellWidth;
                int y = 28 + index / 4 * cellHeight;
                graphics.drawCenteredString(font, sample.label, x + cellWidth / 2, y, 0xffffffff);
                InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, x + 5, y + 14,
                    x + cellWidth - 5, y + cellHeight - 5, 95, 0, ticks < 40 ? 0.65F : -1.1F, 0.15F, sample);
            }
        }
    }
}
