package thaumcraft.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchClientHooks;
import thaumcraft.research.ResearchConfig;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;

public class ThaumonomiconItem extends Item {
    private final boolean cheat;

    public ThaumonomiconItem(Properties properties, boolean cheat) {
        super(properties);
        this.cheat = cheat;
    }

    public boolean isCheat() {
        return cheat;
    }

    public static Properties properties(Properties properties) {
        return properties.stacksTo(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = handleUse(level, player, hand);
        return InteractionResults.holder(result, player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (cheat && ResearchConfig.ALLOW_CHEAT_SHEET) {
                for (ResearchItem item : ResearchCategories.allResearch()) {
                    ResearchManager.complete(serverPlayer, item.key(), false);
                }
                for (Aspect aspect : Aspect.getEnabledAspects()) {
                    if (!Knowledge.get(serverPlayer).hasDiscoveredAspect(aspect)) {
                        Knowledge.get(serverPlayer).setAspectPool(aspect, 50);
                    }
                }
            } else {
                for (ResearchItem item : ResearchCategories.allResearch()) {
                    if (ResearchManager.isComplete(serverPlayer, item.key())) {
                        for (String sibling : item.siblings()) {
                            ResearchManager.complete(serverPlayer, sibling, false);
                        }
                    }
                }
            }
            Knowledge.sync(serverPlayer);
        } else if (level.isClientSide()) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.PAGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            ResearchClientHooks.openBook();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (cheat) {
            tooltip.add(Component.literal("Cheat Sheet"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
