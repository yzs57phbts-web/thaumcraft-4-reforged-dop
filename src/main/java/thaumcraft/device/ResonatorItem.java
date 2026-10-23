package thaumcraft.device;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.essentia.EssentiaBufferBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.essentia.TubeArms;
import thaumcraft.essentia.TubeBlockEntity;
import thaumcraft.registry.ModSounds;

public class ResonatorItem extends Item {
    public ResonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(pos) instanceof EssentiaTransport transport)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        Direction face = context.getClickedFace();
        List<TubeArms.TraceBox> boxes = entity instanceof TubeBlockEntity tube ? tube.traceBoxes() : entity instanceof EssentiaBufferBlockEntity buffer ? buffer.traceBoxes() : List.of();
        Direction arm = TubeArms.direction(TubeArms.trace(player, pos, boxes));
        if (arm != null) {
            face = arm;
        }
        for (Component message : report(entity, transport, face)) {
            player.sendSystemMessage(message);
        }
        level.playSound(null, pos, ModSounds.ALEMBICKNOCK.get(), SoundSource.BLOCKS, 0.5F, 1.9F + level.getRandom().nextFloat() * 0.1F);
        return InteractionResult.SUCCESS;
    }

    public static List<Component> report(BlockEntity entity, EssentiaTransport transport, Direction face) {
        List<Component> messages = new ArrayList<>();
        if (!(entity instanceof EssentiaBufferBlockEntity) && transport.getEssentiaType(face) != null) {
            messages.add(Component.translatable("tc.resonator1", "" + transport.getEssentiaAmount(face), transport.getEssentiaType(face).getName()));
        } else if (entity instanceof EssentiaBufferBlockEntity && ((AspectContainer) entity).getAspects().size() > 0) {
            for (Aspect aspect : ((AspectContainer) entity).getAspects().getAspectsSorted()) {
                messages.add(Component.translatable("tc.resonator1", "" + ((AspectContainer) entity).getAspects().getAmount(aspect), aspect.getName()));
            }
        }
        Component suction = Component.translatable("tc.resonator3");
        if (transport.getSuctionType(face) != null) {
            suction = Component.literal(transport.getSuctionType(face).getName());
        }
        messages.add(Component.translatable("tc.resonator2", "" + transport.getSuctionAmount(face), suction));
        return messages;
    }
}
