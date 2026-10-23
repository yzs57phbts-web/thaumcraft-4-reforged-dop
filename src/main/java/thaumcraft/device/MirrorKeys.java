package thaumcraft.device;

import net.minecraft.core.GlobalPos;
import thaumcraft.platform.StackKey;

public final class MirrorKeys {
    public static final StackKey<GlobalPos> LINK = StackKey.register("mirror_link", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);

    private MirrorKeys() {
    }

    public static void init() {
    }
}
