package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.entity.EldritchMob;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModSounds;

public final class EldritchFieldBlock extends Block {
    private final int kind;

    public EldritchFieldBlock(int kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == 12 ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.kind == 12 || this.kind == 11 && entity instanceof EldritchMob) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        double slowdown = this.kind == 10 ? 0.8 : 0.66;
        entity.setDeltaMovement(motion.x * slowdown, motion.y, motion.z * slowdown);
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (this.kind == 10) {
            entity.hurt(level.damageSources().magic(), 1 + level.getRandom().nextInt(2));
            if (level.getRandom().nextInt(100) == 0) {
                level.removeBlock(pos, false);
            }
        } else {
            if (level.getRandom().nextInt(100) == 0) {
                entity.hurt(level.damageSources().wither(), 1.0F);
            }
            if (entity instanceof Player player) {
                player.causeFoodExhaustion(0.05F);
            }
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, true, true));
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.kind != 12) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.tick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.kind == 12) {
            return;
        }
        float h = random.nextFloat() * 0.33F;
        int color = this.kind == 10 ? 0xB3FFFF : 0x400099;
        Fx.get().sparkle(level, pos.getX() + random.nextFloat(), pos.getY() + 0.1515F + h / 2, pos.getZ() + random.nextFloat(), 0.33F + h, color, 0);
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.JACOBS.get(), SoundSource.BLOCKS, 0.5F, 1 + (random.nextFloat() - random.nextFloat()) * 0.2F, false);
        }
    }
}
