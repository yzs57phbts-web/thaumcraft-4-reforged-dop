package thaumcraft.arcane;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.research.ResearchGate;

public class FocalManipulatorBlock extends Block implements EntityBlock {
    public static final String RESEARCH = "FOCALMANIPULATION";

    public FocalManipulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FocalManipulatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.FOCAL_MANIPULATOR.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, entity) -> FocalManipulatorBlockEntity.clientTick(tickLevel, pos, tickState, (FocalManipulatorBlockEntity) entity);
        }
        return (tickLevel, pos, tickState, entity) -> FocalManipulatorBlockEntity.serverTick(tickLevel, pos, tickState, (FocalManipulatorBlockEntity) entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity table)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (ResearchGate.isComplete(player, RESEARCH)) {
                Menus.open(serverPlayer, table, pos);
            } else {
                player.sendSystemMessage(Component.translatable("tc.researchmissing").withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
