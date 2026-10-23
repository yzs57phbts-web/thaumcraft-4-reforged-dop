package thaumcraft.infusion;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import thaumcraft.platform.StackKey;

public final class InfusionKeys {
    public static final StackKey<Integer> HARDEN = StackKey.register("runic_harden", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final StackKey<Map<String, String>> TAGS = StackKey.register(
        "infusion_tags",
        Codec.unboundedMap(Codec.STRING, Codec.STRING),
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8)
    );

    private InfusionKeys() {
    }

    public static void init() {
    }
}
