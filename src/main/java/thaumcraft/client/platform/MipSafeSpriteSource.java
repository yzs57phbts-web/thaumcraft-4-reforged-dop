package thaumcraft.client.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public record MipSafeSpriteSource(ResourceLocation resource) implements SpriteSource {
    public static final MapCodec<MipSafeSpriteSource> CODEC = ResourceLocation.CODEC.fieldOf("resource")
        .xmap(MipSafeSpriteSource::new, MipSafeSpriteSource::resource);
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @SubscribeEvent
    public static void register(RegisterSpriteSourceTypesEvent event) {
        event.register(Thaumcraft.id("mip_safe"), TYPE);
    }

    public static int scaleFor(FrameSize frame) {
        int scale = 1;
        while (frame.width() * scale % 16 != 0 || frame.height() * scale % 16 != 0) scale *= 2;
        return scale;
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        manager.getResource(TEXTURE_ID_CONVERTER.idToFile(resource)).ifPresent(source ->
            output.add(resource, loader -> loader.loadSprite(resource, source, (id, frame, image, metadata) -> {
                int scale = scaleFor(frame);
                if (scale == 1) return new SpriteContents(id, frame, image, metadata);
                NativeImage resized = new NativeImage(image.getWidth() * scale, image.getHeight() * scale, false);
                try {
                    for (int y = 0; y < resized.getHeight(); y++) {
                        for (int x = 0; x < resized.getWidth(); x++) {
                            resized.setPixelRGBA(x, y, image.getPixelRGBA(x / scale, y / scale));
                        }
                    }
                    return new SpriteContents(id, new FrameSize(frame.width() * scale, frame.height() * scale), resized, metadata);
                } catch (RuntimeException exception) {
                    resized.close();
                    throw exception;
                } finally {
                    image.close();
                }
            }))
        );
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
