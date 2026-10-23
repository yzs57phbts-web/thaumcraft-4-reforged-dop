package thaumcraft.essentia;

import thaumcraft.aspect.Aspect;
import thaumcraft.platform.StackKey;

public final class EssentiaKeys {
    public static final StackKey<Aspect> FILTER = StackKey.register("aspect_filter", Aspect.CODEC, Aspect.STREAM_CODEC);

    private EssentiaKeys() {
    }

    public static void init() {
    }
}
