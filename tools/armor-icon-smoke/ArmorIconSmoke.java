package armor_icon_smoke;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.registry.ModBlocks;
import thaumcraft.device.WardedKeys;
import thaumcraft.aspect.Aspect;

/** Separate, opt-in fixture; never included in the Thaumcraft release JAR. */
@Mod("armor_icon_smoke")
public class ArmorIconSmoke {
    @EventBusSubscriber(modid = "armor_icon_smoke", value = Dist.CLIENT)
    public static class Events {
        private static int ticks;

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) throws Exception {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
                minecraft.options.guiScale().set(1);
                minecraft.resizeDisplay();
                minecraft.setScreen(new Preview());
            }
            if (!(minecraft.screen instanceof Preview) || minecraft.getOverlay() != null) {
                return;
            }
            if (++ticks == 30) {
                Path output = minecraft.gameDirectory.toPath().resolve("evidence");
                Files.createDirectories(output);
                Screenshot.grab(output.toFile(), "inventory-icon-slots.png", minecraft.getMainRenderTarget(), message -> {});
                System.out.println("ARMOR_ICON_PREVIEW_SAVED " + output);
            }
            if (ticks == 45) {
                minecraft.stop();
            }
        }
    }

    private static class Preview extends Screen {
        Preview() {
            super(Component.literal("Inventory icon bounds"));
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff282c34);
            graphics.drawCenteredString(font, "Chargers and banners - 16x16 slot bounds", width / 2, 16, 0xffffffff);
            int row = 0;
            for (int scale : new int[]{1, 2, 4}) {
                int y = 48 + row++ * 74;
                graphics.drawString(font, "Scale " + scale + "x", 22, y + 4, 0xffffffff);
                graphics.pose().pushPose();
                graphics.pose().translate(125, y, 0);
                graphics.pose().scale(scale, scale, 1);
                for (int column = 0; column < 11; column++) {
                    int x = column * 18;
                    graphics.fill(x - 1, -1, x + 17, 17, 0xffeeeeee);
                    graphics.fill(x, 0, x + 16, 16, 0xff999999);
                    ItemStack stack = switch (column) {
                        case 0, 4, 10 -> new ItemStack(Items.STONE, 64);
                        case 1, 2 -> new ItemStack(ModBlocks.VIS_CHARGE_RELAY.get(), column == 1 ? 64 : 1);
                        case 3 -> new ItemStack(ModBlocks.VIS_RELAY.get(), 64);
                        default -> new ItemStack(ModBlocks.BANNER.get(), column == 9 ? 64 : 1);
                    };
                    if (column >= 5 && column <= 8) {
                        WardedKeys.BANNER_COLOR.set(stack, new int[]{13, 10, 7, 12}[column - 5]);
                        if (column == 8) {
                            WardedKeys.BANNER_ASPECT.set(stack, Aspect.FIRE);
                        }
                    }
                    graphics.renderItem(stack, x, 0);
                    graphics.renderItemDecorations(font, stack, x, 0);
                }
                graphics.pose().popPose();
            }
        }
    }
}
