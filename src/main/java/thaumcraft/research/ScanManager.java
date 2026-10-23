package thaumcraft.research;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.EntityAspects;
import thaumcraft.network.ScanPayload;
import thaumcraft.node.NodeView;

public final class ScanManager {
    public static final int KIND_BLOCK = 1;
    public static final int KIND_ENTITY = 2;
    public static final int KIND_NODE = 3;
    public static final double ENTITY_RANGE = 10.0;
    private static final double RANGE_TOLERANCE = 3.0;

    public record Target(ScanKind kind, String key, @Nullable AspectList aspects, ItemStack clueStack, @Nullable ResourceLocation clueEntity) {
    }

    private ScanManager() {
    }

    public static void handleScan(ScanPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Target target = resolveChecked(serverPlayer, payload);
        if (target != null && !hasBeenScanned(serverPlayer, target)) {
            completeScan(serverPlayer, target);
        }
    }

    public static @Nullable Target resolveChecked(Player player, ScanPayload payload) {
        Level level = player.level();
        if (payload.kind() == KIND_ENTITY) {
            Entity entity = level.getEntity(payload.entityId());
            if (entity == null || entity == player || entity.isRemoved() || player.distanceTo(entity) > ENTITY_RANGE + RANGE_TOLERANCE) {
                return null;
            }
            return resolveEntity(entity);
        }
        BlockPos pos = payload.pos();
        double range = player.blockInteractionRange() + RANGE_TOLERANCE;
        if (!level.isLoaded(pos) || player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > range * range) {
            return null;
        }
        if (payload.kind() == KIND_NODE) {
            return resolveNode(level, pos);
        }
        if (payload.kind() == KIND_BLOCK) {
            return resolveBlock(level, pos);
        }
        return null;
    }

    public static @Nullable Target resolveBlock(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NodeView) {
            return resolveNode(level, pos);
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }
        if (state.is(Blocks.FIRE)) {
            return new Target(ScanKind.OBJECT, "minecraft:fire", new AspectList().add(Aspect.FIRE, 4), ItemStack.EMPTY, null);
        }
        if (state.is(Blocks.WATER)) {
            return new Target(ScanKind.OBJECT, "minecraft:water", new AspectList().add(Aspect.WATER, 4), ItemStack.EMPTY, null);
        }
        if (state.is(Blocks.LAVA)) {
            return new Target(ScanKind.OBJECT, "minecraft:lava", new AspectList().add(Aspect.FIRE, 3).add(Aspect.EARTH, 1), ItemStack.EMPTY, null);
        }
        ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
        if (stack.isEmpty()) {
            return null;
        }
        return resolveStack(stack);
    }

    public static BlockHitResult pickBlock(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange()));
        BlockHitResult solid = clipBlock(level, player, eye, end, ClipContext.Fluid.NONE);
        if (!level.getFluidState(BlockPos.containing(eye)).isEmpty()) {
            return solid;
        }
        BlockHitResult hit = clipBlock(level, player, eye, end, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(solid.getBlockPos())) {
            Target target = resolveBlock(level, hit.getBlockPos());
            if (target != null && hasBeenScanned(player, target) && solid.getType() == HitResult.Type.BLOCK) {
                return solid;
            }
        }
        return hit;
    }

    private static BlockHitResult clipBlock(Level level, Player player, Vec3 eye, Vec3 end, ClipContext.Fluid fluids) {
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, fluids, player) {
            @Override
            public VoxelShape getBlockShape(BlockState state, BlockGetter view, BlockPos pos) {
                return state.is(Blocks.FIRE) ? Shapes.block() : super.getBlockShape(state, view, pos);
            }
        });
    }

    public static @Nullable Target resolveStack(ItemStack stack) {
        ItemStack single = stack.copyWithCount(1);
        AspectList aspects = AspectHelper.getBonusTags(single, AspectHelper.getObjectTags(single));
        return new Target(ScanKind.OBJECT, itemKey(single), aspects, single, null);
    }

    public static @Nullable Target resolveNode(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof NodeView node)) {
            return null;
        }
        return new Target(ScanKind.PHENOMENON, "NODE" + node.getId(), nodeAspects(node), ItemStack.EMPTY, null);
    }

    public static @Nullable Target resolveEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            return resolveStack(itemEntity.getItem());
        }
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return new Target(ScanKind.ENTITY, entityKey(entity), entityAspects(entity), ItemStack.EMPTY, type);
    }

    public static String itemKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static String entityKey(Entity entity) {
        StringBuilder key = new StringBuilder();
        if (entity instanceof Player player) {
            key.append("player_").append(player.getGameProfile().getName());
        } else {
            key.append(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
            List<EntityAspects> candidates = AspectRegistry.active().exportEntities().get(entity.getType());
            if (candidates != null) {
                for (EntityAspects candidate : candidates) {
                    if (!candidate.conditions().isEmpty() && matchesData(candidate, entity)) {
                        for (EntityAspects.Condition condition : candidate.conditions()) {
                            key.append(condition.name()).append(condition.value());
                        }
                    }
                }
            }
        }
        if (entity instanceof LivingEntity living && living.isBaby()) {
            key.append("CHILD");
        }
        if (entity instanceof Creeper creeper) {
            if (creeper.getSwellDir() == 1) {
                key.append("FLASHING");
            }
            if (creeper.isPowered()) {
                key.append("POWERED");
            }
        }
        return key.toString();
    }

    private static boolean matchesData(EntityAspects candidate, Entity entity) {
        return candidate.matches(entity.saveWithoutId(new CompoundTag()));
    }

    public static @Nullable AspectList entityAspects(Entity entity) {
        if (entity instanceof Player player) {
            String name = player.getGameProfile().getName();
            AspectList tags = new AspectList();
            tags.add(Aspect.MAN, 4);
            if (name.equalsIgnoreCase("azanor")) {
                tags.add(Aspect.ELDRITCH, 20);
            } else if (name.equalsIgnoreCase("direwolf20")) {
                tags.add(Aspect.BEAST, 20);
            } else if (name.equalsIgnoreCase("pahimar")) {
                tags.add(Aspect.EXCHANGE, 20);
            } else {
                Random random = new Random(("player_" + name).hashCode());
                Aspect[] aspects = Aspect.getEnabledAspects();
                tags.add(aspects[random.nextInt(aspects.length)], 4);
                tags.add(aspects[random.nextInt(aspects.length)], 4);
                tags.add(aspects[random.nextInt(aspects.length)], 4);
            }
            return tags;
        }
        return AspectHelper.getEntityTags(entity);
    }

    public static @Nullable AspectList nodeAspects(NodeView node) {
        AspectList tags = new AspectList();
        AspectList base = node.getAspects();
        for (Aspect aspect : base.getAspectsSorted()) {
            tags.merge(aspect, Math.max(4, base.getAmount(aspect) / 10));
        }
        switch (node.getNodeType()) {
            case UNSTABLE -> tags.merge(Aspect.ENTROPY, 4);
            case HUNGRY -> tags.merge(Aspect.HUNGER, 4);
            case TAINTED -> tags.merge(Aspect.TAINT, 4);
            case PURE -> {
                tags.merge(Aspect.HEAL, 2);
                tags.add(Aspect.ORDER, 2);
            }
            case DARK -> {
                tags.merge(Aspect.DEATH, 2);
                tags.add(Aspect.DARKNESS, 2);
            }
            default -> {
            }
        }
        return tags.size() > 0 ? tags : null;
    }

    public static boolean hasBeenScanned(Player player, Target target) {
        return Knowledge.get(player).isScanned(target.kind(), target.key());
    }

    public static boolean validScan(@Nullable AspectList aspects, Player player) {
        PlayerKnowledge knowledge = Knowledge.get(player);
        boolean notify = player.level().isClientSide();
        if (aspects != null && aspects.size() > 0) {
            for (Aspect aspect : aspects.getAspects()) {
                if (!aspect.isPrimal() && !knowledge.hasDiscoveredParentAspects(aspect)) {
                    if (notify) {
                        for (Aspect parent : aspect.getComponents()) {
                            if (!knowledge.hasDiscoveredAspect(parent)) {
                                player.displayClientMessage(
                                    Component.translatable("tc.discoveryerror", Component.translatable("tc.aspect.help." + parent.getTag())),
                                    true
                                );
                                break;
                            }
                        }
                    }
                    return false;
                }
            }
            return true;
        }
        if (notify) {
            player.displayClientMessage(Component.translatable("tc.unknownobject"), true);
        }
        return false;
    }

    public static boolean completeScan(ServerPlayer player, Target target) {
        if (!validScan(target.aspects(), player)) {
            return false;
        }
        Knowledge.get(player).addScanned(target.kind(), target.key());
        AspectList aspects = target.aspects();
        AspectList awarded = new AspectList();
        for (Aspect aspect : aspects.getAspects()) {
            if (Knowledge.get(player).hasDiscoveredParentAspects(aspect)) {
                int saved = ResearchManager.awardAspect(player, aspect, aspects.getAmount(aspect));
                if (saved > 0) {
                    awarded.merge(aspect, saved);
                }
            }
        }
        Knowledge.sync(player);
        if (!target.clueStack().isEmpty()) {
            ResearchManager.createClue(player, target.clueStack(), awarded);
        } else if (target.clueEntity() != null) {
            ResearchManager.createClue(player, target.clueEntity(), awarded);
        }
        return true;
    }
}
