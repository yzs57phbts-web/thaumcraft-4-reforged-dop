package thaumcraft.platform;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.entity.golem.GolemBase;

public final class GolemContainers {
    public interface User {
        GolemContainers containers();
    }

    private final GolemBase golem;
    private @Nullable Container opened;
    private @Nullable ServerPlayer opener;
    private List<BlockPos> positions = List.of();
    private long closeAt;

    public GolemContainers(GolemBase golem) { this.golem = golem; }

    private ServerPlayer opener() {
        if (opener == null) opener = FakePlayerFactory.get((ServerLevel) golem.level(), new GameProfile(golem.getUUID(), "[TC4 Golem]"));
        opener.setPos(golem.position());
        return opener;
    }

    public void open(BlockPos pos) {
        var level = golem.level();
        var state = level.getBlockState(pos);
        Container container;
        List<BlockPos> parts;
        if (state.getBlock() instanceof ChestBlock chest) {
            container = ChestBlock.getContainer(chest, state, level, pos, true);
            parts = state.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? List.of(pos.immutable())
                : List.of(pos.immutable(), pos.relative(ChestBlock.getConnectedDirection(state)));
        } else {
            container = level.getBlockEntity(pos) instanceof Container candidate ? candidate : null;
            parts = List.of(pos.immutable());
        }
        if (container == null) return;
        closeAt = level.getGameTime() + 5;
        if (opened != null && positions.equals(parts)) return;
        close();
        for (BlockPos part : parts) {
            var entity = level.getBlockEntity(part);
            if (entity instanceof ChestBlockEntity chest && !(chest.openersCounter instanceof GolemOpenersCounter)) {
                chest.openersCounter = new GolemOpenersCounter(chest.openersCounter);
            } else if (entity instanceof BarrelBlockEntity barrel && !(barrel.openersCounter instanceof GolemOpenersCounter)) {
                barrel.openersCounter = new GolemOpenersCounter(barrel.openersCounter);
            }
        }
        opened = container;
        positions = parts;
        opened.startOpen(opener());
    }

    public void tick() {
        if (opened != null && (!golem.isWorking() || golem.level().getGameTime() >= closeAt)) close();
    }

    public void close() {
        if (opened == null) return;
        Container container = opened;
        opened = null;
        positions = List.of();
        container.stopOpen(opener());
    }

    private static final class GolemOpenersCounter extends ContainerOpenersCounter {
        private final ContainerOpenersCounter delegate;

        private GolemOpenersCounter(ContainerOpenersCounter delegate) {
            this.delegate = delegate;
            openCount = delegate.openCount;
            maxInteractionRange = delegate.maxInteractionRange;
        }

        @Override
        public void onOpen(Level level, BlockPos pos, BlockState state) { delegate.onOpen(level, pos, state); }

        @Override
        public void onClose(Level level, BlockPos pos, BlockState state) { delegate.onClose(level, pos, state); }

        @Override
        public void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            delegate.openerCountChanged(level, pos, state, previous, current);
        }

        @Override
        public boolean isOwnContainer(Player player) { return delegate.isOwnContainer(player); }

        @Override
        public List<Player> getPlayersWithContainerOpen(Level level, BlockPos pos) {
            List<Player> users = new ArrayList<>(super.getPlayersWithContainerOpen(level, pos));
            for (GolemBase golem : level.getEntitiesOfClass(GolemBase.class, new AABB(pos).inflate(7))) {
                if (golem.containers().positions.contains(pos)) users.add(golem.containers().opener());
            }
            return users;
        }
    }
}
