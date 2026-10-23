package thaumcraft.essentia;

import net.minecraft.core.Direction;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

public interface EssentiaTransport {
    boolean isConnectable(Direction face);

    boolean canInputFrom(Direction face);

    boolean canOutputTo(Direction face);

    void setSuction(@Nullable Aspect aspect, int amount);

    @Nullable Aspect getSuctionType(Direction face);

    int getSuctionAmount(Direction face);

    int takeEssentia(Aspect aspect, int amount, Direction face);

    int addEssentia(Aspect aspect, int amount, Direction face);

    @Nullable Aspect getEssentiaType(Direction face);

    int getEssentiaAmount(Direction face);

    int getMinimumSuction();

    boolean renderExtendedTube();
}
