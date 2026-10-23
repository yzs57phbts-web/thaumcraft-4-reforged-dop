package thaumcraft.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.block.MagicalSaplingBlock;
import thaumcraft.fx.Fx;
import thaumcraft.item.gear.Repairable;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import net.minecraft.world.item.BoneMealItem;

public class ElementalHoeItem extends HoeItem implements Repairable {
    private static final int TILL_COLOR = 8401408;

    public ElementalHoeItem(Properties properties) {
        super(Registration.tier(TcMaterials.ELEMENTAL_TOOL), properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        boolean did = false;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = pos.offset(x, 0, z);
                BlockHitResult hit = new BlockHitResult(context.getClickLocation().add(x, 0, z), context.getClickedFace(), target, context.isInside());
                if (super.useOn(new UseOnContext(level, player, context.getHand(), stack, hit)).consumesAction()) {
                    if (level.isClientSide()) {
                        Fx.get().blockSparkle(level, target, TILL_COLOR, 2);
                    }
                    did = true;
                }
            }
        }
        if (did) {
            return InteractionResult.SUCCESS;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MagicalSaplingBlock)) {
            did = BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player);
        }
        if (did) {
            stack.hurtAndBreak(1, player, ((context.getHand()) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos, 0, 3);
            }
        } else if (state.is(ModBlocks.GREATWOOD_SAPLING.get()) && stack.getDamageValue() + 20 <= stack.getMaxDamage()) {
            if (level instanceof ServerLevel serverLevel) {
                ((MagicalSaplingBlock) state.getBlock()).grow(serverLevel, pos, state, level.getRandom());
            } else {
                Fx.get().blockSparkle(level, pos, 0, 2);
            }
            stack.hurtAndBreak(5, player, ((context.getHand()) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
            did = true;
        } else if (state.is(ModBlocks.SILVERWOOD_SAPLING.get()) && stack.getDamageValue() + 150 <= stack.getMaxDamage()) {
            if (level instanceof ServerLevel serverLevel) {
                ((MagicalSaplingBlock) state.getBlock()).grow(serverLevel, pos, state, level.getRandom());
            } else {
                Fx.get().blockSparkle(level, pos, 0, 2);
            }
            stack.hurtAndBreak(25, player, ((context.getHand()) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
            did = true;
        }
        if (did && !level.isClientSide()) {
            level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.PLAYERS, 0.75F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        return did ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
