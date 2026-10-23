package thaumcraft.item.tool;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.entity.projectile.PrimalArrow;

public class PrimalArrowItem extends ArrowItem {
    private final int type;

    public PrimalArrowItem(Properties properties, int type) {
        super(properties);
        this.type = type;
    }

    public int getArrowType() {
        return type;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity owner, @Nullable ItemStack weapon) {
        return new PrimalArrow(level, owner, type, weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        return new PrimalArrow(level, position.x(), position.y(), position.z(), type);
    }
}
