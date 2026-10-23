package thaumcraft.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.client.fx.ShieldRunesFx;
import thaumcraft.client.hud.GearHud;
import thaumcraft.client.platform.ClientNetwork;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.client.research.ResearchNotifications;
import thaumcraft.device.ArcaneBoreBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.infusion.InfusionMatrixBlockEntity;
import thaumcraft.network.AspectDiscoveryPayload;
import thaumcraft.network.AspectPoolPayload;
import thaumcraft.network.AspectTagsPayload;
import thaumcraft.network.BeamPulsePayload;
import thaumcraft.network.BlockBubbleFxPayload;
import thaumcraft.network.BlockSparkleFxPayload;
import thaumcraft.network.BoreDigPayload;
import thaumcraft.network.EssentiaSourceFxPayload;
import thaumcraft.network.InfusionSourceFxPayload;
import thaumcraft.network.NodeZapPayload;
import thaumcraft.network.ResearchCompletePayload;
import thaumcraft.network.RunicChargePayload;
import thaumcraft.network.ShieldFxPayload;
import thaumcraft.network.ShockZapPayload;
import thaumcraft.network.VisDrainFxPayload;
import thaumcraft.network.WarpEventPayload;
import thaumcraft.network.WarpMessagePayload;
import thaumcraft.platform.DataAttachments.SyncPayload;
import thaumcraft.platform.DataAttachments;
import thaumcraft.registry.ModSounds;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void register() {
        ClientNetwork.handle(BoreDigPayload.TYPE, (payload, player) -> {
            Level level = player.level();
            if (level.getBlockEntity(payload.bore()) instanceof ArcaneBoreBlockEntity bore) {
                bore.receiveDig(payload.target(), payload.stateId(), payload.broken());
            }
        });
        ClientNetwork.handle(SyncPayload.TYPE, DataAttachments::receive);
        ClientNetwork.handle(BeamPulsePayload.TYPE, ClientPayloadHandlers::beamPulse);
        ClientNetwork.handle(AspectTagsPayload.TYPE, ClientPayloadHandlers::aspectTags);
        ClientNetwork.handle(NodeZapPayload.TYPE, ClientPayloadHandlers::nodeZap);
        ClientNetwork.handle(VisDrainFxPayload.TYPE, ClientPayloadHandlers::visDrain);
        ClientNetwork.handle(ResearchCompletePayload.TYPE, ResearchNotifications::researchComplete);
        ClientNetwork.handle(AspectPoolPayload.TYPE, ResearchNotifications::aspectPool);
        ClientNetwork.handle(AspectDiscoveryPayload.TYPE, ResearchNotifications::aspectDiscovery);
        ClientNetwork.handle(WarpMessagePayload.TYPE, ResearchNotifications::warpMessage);
        ClientNetwork.handle(EssentiaSourceFxPayload.TYPE, ClientPayloadHandlers::essentiaSource);
        ClientNetwork.handle(InfusionSourceFxPayload.TYPE, ClientPayloadHandlers::infusionSource);
        ClientNetwork.handle(BlockSparkleFxPayload.TYPE, ClientPayloadHandlers::blockSparkle);
        ClientNetwork.handle(BlockBubbleFxPayload.TYPE, ClientPayloadHandlers::blockBubble);
        ClientNetwork.handle(ShockZapPayload.TYPE, ClientPayloadHandlers::shockZap);
        ClientNetwork.handle(RunicChargePayload.TYPE, (payload, player) -> GearHud.setRunic(payload.charge(), payload.max()));
        ClientNetwork.handle(ShieldFxPayload.TYPE, ClientPayloadHandlers::shield);
        ClientNetwork.handle(WarpEventPayload.TYPE, (payload, player) -> {
            if (player instanceof LocalPlayer localPlayer) {
                WarpEffectClient.handle(payload, localPlayer);
            }
        });
    }

    private static void blockBubble(BlockBubbleFxPayload payload, Player player) {
        Fx.get().blockBubble(player.level(), payload.pos(), payload.color());
    }

    private static void shockZap(ShockZapPayload payload, Player player) {
        Entity source = player.level().getEntity(payload.sourceId());
        Entity target = player.level().getEntity(payload.targetId());
        if (source != null && target != null) {
            Fx.get().shockZap(player.level(), source, target);
        }
    }

    private static void shield(ShieldFxPayload payload, Player player) {
        Level level = player.level();
        Entity entity = level.getEntity(payload.entityId());
        if (entity == null) {
            return;
        }
        if (payload.target() >= 0) {
            Entity target = level.getEntity(payload.target());
            float pitch = 90.0F;
            float yaw = 0.0F;
            if (target != null) {
                double dx = entity.getX() - target.getX();
                double dy = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
                double dz = entity.getZ() - target.getZ();
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
                pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
            }
            WorldFxRenderer.add(new ShieldRunesFx(level.getRandom(), entity, 8, yaw, pitch));
        } else if (payload.target() == -1) {
            WorldFxRenderer.add(new ShieldRunesFx(level.getRandom(), entity, 8, 0.0F, 90.0F));
            WorldFxRenderer.add(new ShieldRunesFx(level.getRandom(), entity, 8, 0.0F, 270.0F));
        } else if (payload.target() == -2) {
            WorldFxRenderer.add(new ShieldRunesFx(level.getRandom(), entity, 8, 0.0F, 270.0F));
        } else if (payload.target() == -3) {
            WorldFxRenderer.add(new ShieldRunesFx(level.getRandom(), entity, 8, 0.0F, 90.0F));
        }
    }

    private static void beamPulse(BeamPulsePayload payload, Player player) {
        Entity source = player.level().getEntity(payload.source());
        Entity target = player.level().getEntity(payload.target());
        if (source != null && target != null) {
            Fx.get().beamPulse(player.level(), source, target, payload.color());
        }
    }

    private static void aspectTags(AspectTagsPayload payload, Player player) {
        AspectRegistry.setActive(AspectRegistry.fromSync(payload.items(), payload.entities()));
    }

    private static void nodeZap(NodeZapPayload payload, Player player) {
        Level level = player.level();
        Fx.get().nodeBolt(level, payload.x(), payload.y(), payload.z(), payload.targetX(), payload.targetY(), payload.targetZ());
        level.playLocalSound(payload.x(), payload.y(), payload.z(), ModSounds.ZAP.get(), SoundSource.BLOCKS, 0.1F, 1.0F + level.getRandom().nextFloat() * 0.2F, false);
    }

    private static void visDrain(VisDrainFxPayload payload, Player player) {
        Fx.get().visDrain(player.level(), payload.drainer(), payload.source(), payload.primal());
    }

    private static void essentiaSource(EssentiaSourceFxPayload payload, Player player) {
        Fx.get().essentiaSource(player.level(), payload.pos(), payload.dx(), payload.dy(), payload.dz(), payload.color());
    }

    private static void infusionSource(InfusionSourceFxPayload payload, Player player) {
        if (player.level().getBlockEntity(payload.pos()) instanceof InfusionMatrixBlockEntity matrix) {
            matrix.addSourceFx(payload.dx(), payload.dy(), payload.dz(), payload.color());
        }
    }

    private static void blockSparkle(BlockSparkleFxPayload payload, Player player) {
        Fx.get().blockSparkle(player.level(), payload.pos(), payload.color(), 7);
    }
}
