package thaumcraft.wand.focus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public class FocusItem extends Item {
    public static final int MAX_UPGRADES = 5;

    public FocusItem(Properties properties) {
        super(properties);
    }

    public static Properties properties(Properties properties) {
        return properties.stacksTo(1).rarity(Rarity.RARE);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        appendCostText(stack, builder);
        addFocusInformation(stack, builder);
    }

    public void appendCostText(ItemStack stack, List<Component> builder) {
        AspectList cost = getVisCost(stack);
        if (cost == null || cost.size() == 0) {
            return;
        }
        builder.add(Component.translatable(isVisCostPerTick(stack) ? "item.Focus.cost2" : "item.Focus.cost1"));
        DecimalFormat format = new DecimalFormat("#####.##");
        for (Aspect aspect : cost.getAspectsSorted()) {
            String amount = format.format(cost.getAmount(aspect) / 100.0F);
            builder.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount));
        }
    }

    public void addFocusInformation(ItemStack stack, List<Component> builder) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (int id : getAppliedUpgrades(stack)) {
            if (id >= 0) {
                counts.merge(id, 1, Integer::sum);
            }
        }
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            FocusUpgradeType type = FocusUpgradeType.byId(entry.getKey());
            if (type == null) {
                continue;
            }
            MutableComponent line = type.getLocalizedName().copy();
            if (entry.getValue() > 1) {
                line.append(" ").append(Component.translatable("enchantment.level." + entry.getValue()));
            }
            builder.add(line.withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public boolean isVisCostPerTick(ItemStack focus) {
        return false;
    }

    public int getFocusColor(ItemStack focus) {
        return 0;
    }

    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return null;
    }

    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return null;
    }

    public @Nullable ResourceLocation getDepthLayerTexture(ItemStack focus) {
        return null;
    }

    public WandFocusAnimation getAnimation(ItemStack focus) {
        return WandFocusAnimation.WAVE;
    }

    public String getSortingHelper(ItemStack focus) {
        StringBuilder out = new StringBuilder();
        for (int id : getAppliedUpgrades(focus)) {
            out.append(id);
        }
        return out.toString();
    }

    public @Nullable AspectList getVisCost(ItemStack focus) {
        return null;
    }

    public int getActivationCooldown(ItemStack focus) {
        return 0;
    }

    public int getMaxAreaSize(ItemStack focus) {
        return 1;
    }

    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return null;
    }

    public int[] getAppliedUpgrades(ItemStack focus) {
        int[] upgrades = {-1, -1, -1, -1, -1};
        List<Integer> stored = FocusKeys.UPGRADES.get(focus);
        if (stored == null) {
            return upgrades;
        }
        for (int index = 0; index < stored.size() && index < MAX_UPGRADES; index++) {
            upgrades[index] = stored.get(index);
        }
        return upgrades;
    }

    public boolean applyUpgrade(ItemStack focus, FocusUpgradeType type, int rank) {
        if (rank < 1 || rank > MAX_UPGRADES) {
            return false;
        }
        int[] upgrades = getAppliedUpgrades(focus);
        if (upgrades[rank - 1] != -1) {
            return false;
        }
        upgrades[rank - 1] = type.id();
        List<Integer> stored = new ArrayList<>(MAX_UPGRADES);
        for (int id : upgrades) {
            stored.add(id);
        }
        FocusKeys.UPGRADES.set(focus, List.copyOf(stored));
        return true;
    }

    public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
        return true;
    }

    public boolean isUpgradedWith(ItemStack focus, FocusUpgradeType type) {
        return getUpgradeLevel(focus, type) > 0;
    }

    public int getUpgradeLevel(ItemStack focus, FocusUpgradeType type) {
        int level = 0;
        for (int id : getAppliedUpgrades(focus)) {
            if (id == type.id()) {
                level++;
            }
        }
        return level;
    }

    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.PASS;
    }

    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
    }

    public void onPlayerStoppedUsingFocus(ItemStack wand, Level level, Player player, int count) {
    }

    public boolean onFocusBlockStartBreak(ItemStack wand, BlockPos pos, Player player) {
        return false;
    }

    public boolean onEntitySwing(LivingEntity entity, ItemStack wand) {
        return false;
    }

    public enum WandFocusAnimation {
        WAVE,
        CHARGE
    }
}
