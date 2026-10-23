package thaumcraft.aspect;

import thaumcraft.platform.StackKey;

public final class AspectKeys {
    public static final StackKey<AspectList> ASPECTS = StackKey.register("aspects", AspectList.CODEC, AspectList.STREAM_CODEC);

    private AspectKeys() {
    }

    public static void init() {
    }
}
