package thaumcraft.client.research;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.GuiBlit;

final class BookText {
    static final ResourceLocation BOOK = Thaumcraft.id("textures/gui/gui_researchbook.png");

    private static final Style UNIFORM = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("uniform"));
    private static final Pattern MARKER = Pattern.compile("^@(\\d+)@$");
    private static final Pattern INSERT = Pattern.compile("<LINE/?>|<IMG>(.*?)</IMG>");

    private BookText() {
    }

    sealed interface Line {
        int height();
    }

    record Text(FormattedCharSequence text) implements Line {
        @Override
        public int height() {
            return 9;
        }
    }

    record Rule() implements Line {
        @Override
        public int height() {
            return 9;
        }
    }

    record Picture(ResourceLocation texture, int u, int v, int width, int regionHeight, float scale) implements Line {
        @Override
        public int height() {
            return (int) Math.floor(regionHeight * scale);
        }
    }

    static Component uniform(String text) {
        MutableComponent result = Component.empty();
        Style style = UNIFORM;
        int start = 0;
        for (int index = 0; index + 1 < text.length(); index++) {
            if (text.charAt(index) != '§') {
                continue;
            }
            if (index > start) {
                result.append(Component.literal(text.substring(start, index)).withStyle(style));
            }
            ChatFormatting format = ChatFormatting.getByCode(text.charAt(++index));
            if (format != null) {
                style = format == ChatFormatting.RESET ? UNIFORM : style.applyLegacyFormat(format);
            }
            start = index + 1;
        }
        if (start < text.length()) {
            result.append(Component.literal(text.substring(start)).withStyle(style));
        }
        return result;
    }

    static List<Line> layout(Font font, String raw, int width) {
        String text = raw.replace("<BR/>", "\n").replace("<BR>", "\n");
        List<String> inserts = new ArrayList<>();
        Matcher matcher = INSERT.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            inserts.add(matcher.group());
            matcher.appendReplacement(builder, Matcher.quoteReplacement("\n@" + (inserts.size() - 1) + "@\n"));
        }
        matcher.appendTail(builder);
        text = builder.toString();
        while (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        List<Line> lines = new ArrayList<>();
        for (FormattedCharSequence sequence : font.split(uniform(text), width)) {
            Matcher marker = MARKER.matcher(plain(sequence).trim());
            if (!marker.matches()) {
                lines.add(new Text(sequence));
                continue;
            }
            Line insert = insert(inserts.get(Integer.parseInt(marker.group(1))));
            if (insert != null) {
                lines.add(insert);
            }
        }
        return lines;
    }

    static List<List<Line>> paginate(List<Line> lines, int firstHeight, int pageHeight) {
        List<List<Line>> pages = new ArrayList<>();
        List<Line> page = new ArrayList<>();
        int height = 0;
        int limit = firstHeight;
        for (Line line : lines) {
            if (!page.isEmpty() && height + line.height() > limit) {
                pages.add(List.copyOf(page));
                page.clear();
                height = 0;
                limit = pageHeight;
            }
            page.add(line);
            height += line.height();
        }
        if (!page.isEmpty() || pages.isEmpty()) {
            pages.add(List.copyOf(page));
        }
        return pages;
    }

    static void draw(GuiGraphics graphics, Font font, List<Line> lines, int x, int y, int width, int color) {
        int currentY = y;
        for (Line line : lines) {
            switch (line) {
                case Text text -> graphics.drawString(font, text.text(), x, currentY, color, false);
                case Rule rule -> GuiBlit.blit(graphics, BOOK, x + width / 2 - 48, currentY + 2, 96, 4, 24, 184, 96, 4, 256, 256, 0xFFFFFFFF);
                case Picture picture -> {
                    graphics.pose().pushPose();
                    graphics.pose().translate(x - 3 + width / 2 - picture.width() / 2 * picture.scale(), currentY, 0.0F);
                    graphics.pose().scale(picture.scale(), picture.scale(), 1.0F);
                    GuiBlit.blit(graphics, picture.texture(), 0, 0, picture.width(), picture.regionHeight(), picture.u(), picture.v(), picture.width(), picture.regionHeight(), 256, 256, 0xFFFFFFFF);
                    graphics.pose().popPose();
                }
            }
            currentY += line.height();
        }
    }

    private static Line insert(String insert) {
        if (insert.startsWith("<LINE")) {
            return new Rule();
        }
        String[] parts = insert.substring(5, insert.length() - 6).split(":");
        if (parts.length < 7) {
            return null;
        }
        String path = parts[1].replace("textures/items/", "textures/item/").replace("textures/blocks/", "textures/block/");
        ResourceLocation texture = ResourceLocation.tryParse(parts[0] + ":" + path);
        if (texture == null) {
            return null;
        }
        return new Picture(texture, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Float.parseFloat(parts[6]));
    }

    private static String plain(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
