package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;

public abstract class LinkedMirrorBlockEntity extends TcBlockEntity {
    protected boolean linked;
    protected @Nullable GlobalPos link;
    protected int count;
    protected int inc = 40;

    protected LinkedMirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isLinked() {
        return linked;
    }

    public @Nullable GlobalPos getLink() {
        return link;
    }

    public void setLinkTarget(@Nullable GlobalPos target) {
        link = target;
    }

    public Direction getFacing() {
        return getBlockState().getValue(MirrorBlock.FACING);
    }

    protected boolean isSameKind(@Nullable BlockEntity other) {
        return other != null && other.getType() == getType();
    }

    protected @Nullable GlobalPos self() {
        return level == null ? null : GlobalPos.of(level.dimension(), worldPosition);
    }

    protected boolean pointsTo(LinkedMirrorBlockEntity other) {
        return link != null && link.equals(other.self());
    }

    public @Nullable ServerLevel targetLevel() {
        if (link == null || !(level instanceof ServerLevel own)) {
            return null;
        }
        return MirrorLinks.levelOf(own, link);
    }

    public @Nullable LinkedMirrorBlockEntity target(boolean force) {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return null;
        }
        if (!force && !targetLevel.hasChunkAt(link.pos())) {
            return null;
        }
        BlockEntity entity = MirrorLinks.fetch(targetLevel, link.pos());
        return isSameKind(entity) ? (LinkedMirrorBlockEntity) entity : null;
    }

    protected void unlink() {
        linked = false;
        sync();
    }

    public void restoreLink(boolean force) {
        if (!isDestinationValid(force)) {
            return;
        }
        LinkedMirrorBlockEntity other = target(force);
        GlobalPos own = self();
        if (other != null && own != null) {
            other.linked = true;
            other.link = own;
            other.sync();
            linked = true;
            setChanged();
            other.setChanged();
            sync();
        }
    }

    public void invalidateLink() {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null || !targetLevel.hasChunkAt(link.pos())) {
            return;
        }
        if (target(false) instanceof LinkedMirrorBlockEntity other) {
            other.linked = false;
            setChanged();
            other.sync();
        }
    }

    public boolean isLinkValid(boolean force) {
        if (!linked) {
            return false;
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return false;
        }
        if (!force && !targetLevel.hasChunkAt(link.pos())) {
            return true;
        }
        LinkedMirrorBlockEntity other = target(true);
        if (other == null || !other.linked) {
            unlink();
            return false;
        }
        if (other.pointsTo(this)) {
            return true;
        }
        unlink();
        return false;
    }

    public boolean isLinkValidSimple() {
        if (!linked) {
            return false;
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return false;
        }
        if (!targetLevel.hasChunkAt(link.pos())) {
            return true;
        }
        LinkedMirrorBlockEntity other = target(false);
        return other != null && other.linked && other.pointsTo(this);
    }

    public boolean isDestinationValid(boolean force) {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return false;
        }
        if (!force && !targetLevel.hasChunkAt(link.pos())) {
            return false;
        }
        LinkedMirrorBlockEntity other = target(true);
        if (other == null) {
            unlink();
            return false;
        }
        return !other.isLinkValid(force);
    }

    protected void linkTick() {
        if (count++ % inc == 0) {
            if (!isLinkValidSimple()) {
                if (inc < 600) {
                    inc += 20;
                }
                restoreLink(false);
            } else {
                inc = 40;
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        linked = input.getBooleanOr("linked", false);
        link = input.read("link", GlobalPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("linked", linked);
        output.storeNullable("link", GlobalPos.CODEC, link);
    }
}
