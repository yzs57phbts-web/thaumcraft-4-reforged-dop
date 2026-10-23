package thaumcraft.entity.monster;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import thaumcraft.platform.DataKey;

public final class ChampionKeys {
    public static final DataKey<Integer> MODIFIER = DataKey.builder("champion_modifier", () -> -2)
        .codec(Codec.INT)
        .syncToAll(ByteBufCodecs.INT)
        .build();

    private ChampionKeys() {
    }

    public static void init() {
    }
}
