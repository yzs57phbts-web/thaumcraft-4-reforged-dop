package thaumcraft.aspect;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public final class AspectHelper {
    private static final int CAP = 64;
    private static final int MAX_BONUS_TAGS = 6;

    private static final Map<TagKey<Block>, Integer> HARVEST_LEVELS = Map.of(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0,
        BlockTags.INCORRECT_FOR_GOLD_TOOL, 0,
        BlockTags.INCORRECT_FOR_STONE_TOOL, 1,
        BlockTags.INCORRECT_FOR_IRON_TOOL, 2,
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 3,
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4
    );

    private static final Map<ResourceLocation, Aspect> ENCHANTMENT_ASPECTS = Map.ofEntries(
        Map.entry(ResourceLocation.withDefaultNamespace("aqua_affinity"), Aspect.WATER),
        Map.entry(ResourceLocation.withDefaultNamespace("bane_of_arthropods"), Aspect.BEAST),
        Map.entry(ResourceLocation.withDefaultNamespace("blast_protection"), Aspect.ARMOR),
        Map.entry(ResourceLocation.withDefaultNamespace("efficiency"), Aspect.TOOL),
        Map.entry(ResourceLocation.withDefaultNamespace("feather_falling"), Aspect.FLIGHT),
        Map.entry(ResourceLocation.withDefaultNamespace("fire_aspect"), Aspect.FIRE),
        Map.entry(ResourceLocation.withDefaultNamespace("fire_protection"), Aspect.ARMOR),
        Map.entry(ResourceLocation.withDefaultNamespace("flame"), Aspect.FIRE),
        Map.entry(ResourceLocation.withDefaultNamespace("fortune"), Aspect.GREED),
        Map.entry(ResourceLocation.withDefaultNamespace("infinity"), Aspect.CRAFT),
        Map.entry(ResourceLocation.withDefaultNamespace("knockback"), Aspect.AIR),
        Map.entry(ResourceLocation.withDefaultNamespace("looting"), Aspect.GREED),
        Map.entry(ResourceLocation.withDefaultNamespace("power"), Aspect.WEAPON),
        Map.entry(ResourceLocation.withDefaultNamespace("projectile_protection"), Aspect.ARMOR),
        Map.entry(ResourceLocation.withDefaultNamespace("protection"), Aspect.ARMOR),
        Map.entry(ResourceLocation.withDefaultNamespace("punch"), Aspect.AIR),
        Map.entry(ResourceLocation.withDefaultNamespace("respiration"), Aspect.AIR),
        Map.entry(ResourceLocation.withDefaultNamespace("sharpness"), Aspect.WEAPON),
        Map.entry(ResourceLocation.withDefaultNamespace("silk_touch"), Aspect.EXCHANGE),
        Map.entry(ResourceLocation.withDefaultNamespace("thorns"), Aspect.WEAPON),
        Map.entry(ResourceLocation.withDefaultNamespace("smite"), Aspect.ENTROPY),
        Map.entry(ResourceLocation.withDefaultNamespace("unbreaking"), Aspect.EARTH),
        Map.entry(ResourceLocation.withDefaultNamespace("luck_of_the_sea"), Aspect.GREED),
        Map.entry(ResourceLocation.withDefaultNamespace("lure"), Aspect.BEAST),
        Map.entry(Thaumcraft.id("haste"), Aspect.MOTION),
        Map.entry(Thaumcraft.id("repair"), Aspect.TOOL)
    );

    private AspectHelper() {
    }

    public static @Nullable AspectList getObjectTags(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        AspectList tags = AspectRegistry.active().getObjectTags(stack.getItem());
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            tags = applyPotion(stack, tags == null ? new AspectList() : tags);
        }
        return cap(tags);
    }

    public static @Nullable AspectList getEntityTags(Entity entity) {
        return AspectRegistry.active().getEntityTags(entity);
    }

    public static AspectList getBonusTags(ItemStack stack, @Nullable AspectList source) {
        AspectList result = new AspectList();
        if (stack.isEmpty()) {
            return source == null ? result : source.copy();
        }
        AspectList contained = getContainedAspects(stack);
        if (contained != null) {
            for (Aspect aspect : contained.getAspects()) {
                if (contained.getAmount(aspect) > 0) {
                    result.add(aspect, contained.getAmount(aspect));
                }
            }
        }
        if (source != null) {
            for (Aspect aspect : source.getAspects()) {
                result.add(aspect, source.getAmount(aspect));
            }
        }
        double armor = attributeTotal(stack, Attributes.ARMOR, EquipmentSlotGroup.ARMOR);
        if (armor > 0) {
            result.merge(Aspect.ARMOR, (int) armor);
        } else if (stack.is(ItemTags.SWORDS)) {
            double damage = attributeTotal(stack, Attributes.ATTACK_DAMAGE, EquipmentSlotGroup.MAINHAND) - 3.0 + 1.0;
            if (damage > 0.0) {
                result.merge(Aspect.WEAPON, (int) damage);
            }
        } else if (stack.getItem() instanceof BowItem) {
            result.merge(Aspect.WEAPON, 3).merge(Aspect.FLIGHT, 1);
        } else if (stack.is(ItemTags.PICKAXES)) {
            result.merge(Aspect.MINE, harvestLevel(stack) + 1);
        } else if (stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS)) {
            result.merge(Aspect.TOOL, harvestLevel(stack) + 1);
        } else if (stack.getItem() instanceof ShearsItem || stack.is(ItemTags.HOES)) {
            int durability = stack.getMaxDamage();
            if (durability <= 59) {
                result.merge(Aspect.HARVEST, 1);
            } else if (durability <= 131) {
                result.merge(Aspect.HARVEST, 2);
            } else if (durability <= 250) {
                result.merge(Aspect.HARVEST, 3);
            } else {
                result.merge(Aspect.HARVEST, 4);
            }
        }
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        int total = 0;
        for (var entry : enchantments.entrySet()) {
            int level = entry.getIntValue();
            Aspect aspect = enchantmentAspect(entry.getKey());
            if (aspect != null) {
                result.merge(aspect, level);
            }
            total += level;
        }
        if (total > 0) {
            result.merge(Aspect.MAGIC, total);
        }
        return cullTags(result);
    }

    public static AspectList getBonusTags(ItemStack stack) {
        return getBonusTags(stack, getObjectTags(stack));
    }

    public static AspectList cullTags(AspectList source) {
        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            result.add(aspect, source.getAmount(aspect));
        }
        while (result.size() > MAX_BONUS_TAGS) {
            Aspect lowest = null;
            float low = 32767.0F;
            for (Aspect aspect : result.getAspects()) {
                float weight = weight(aspect, result.getAmount(aspect));
                if (weight < low) {
                    low = weight;
                    lowest = aspect;
                }
            }
            result.remove(lowest);
        }
        return result;
    }

    public static AspectList reduceToPrimals(AspectList source) {
        return reduceToPrimals(source, false);
    }

    public static AspectList reduceToPrimals(AspectList source, boolean merge) {
        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            int amount = source.getAmount(aspect);
            if (aspect.isPrimal()) {
                if (merge) {
                    result.merge(aspect, amount);
                } else {
                    result.add(aspect, amount);
                }
                continue;
            }
            Aspect[] components = aspect.getComponents();
            AspectList parts = new AspectList();
            parts.add(components[0], amount);
            parts.add(components[1], amount);
            AspectList reduced = reduceToPrimals(parts, merge);
            for (Aspect primal : reduced.getAspects()) {
                if (merge) {
                    result.merge(primal, reduced.getAmount(primal));
                } else {
                    result.add(primal, reduced.getAmount(primal));
                }
            }
        }
        return result;
    }

    private static float weight(Aspect aspect, int amount) {
        float weight = amount;
        if (aspect.isPrimal()) {
            return weight * 0.9F;
        }
        Aspect[] components = aspect.getComponents();
        for (Aspect component : components) {
            if (!component.isPrimal()) {
                weight *= 1.1F;
                Aspect[] inner = component.getComponents();
                if (!inner[0].isPrimal()) {
                    weight *= 1.05F;
                }
                if (!inner[1].isPrimal()) {
                    weight *= 1.05F;
                }
            }
        }
        return weight;
    }

    private static @Nullable AspectList getContainedAspects(ItemStack stack) {
        if (stack.getItem() instanceof AspectProvidingItem provider) {
            return provider.getAspects(stack);
        }
        return AspectKeys.ASPECTS.get(stack);
    }

    private static @Nullable AspectList cap(@Nullable AspectList source) {
        if (source == null) {
            return null;
        }
        AspectList out = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            out.merge(aspect, Math.min(CAP, source.getAmount(aspect)));
        }
        return out;
    }

    private static AspectList applyPotion(ItemStack stack, AspectList tags) {
        tags.merge(Aspect.WATER, 1);
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return tags;
        }
        if (!stack.is(Items.POTION)) {
            tags.merge(Aspect.ENTROPY, 2);
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            int level = effect.getAmplifier() + 1;
            tags.merge(Aspect.MAGIC, level * 2);
            Holder<MobEffect> type = effect.getEffect();
            if (type.is(MobEffects.BLINDNESS)) {
                tags.merge(Aspect.DARKNESS, level * 3);
            } else if (type.is(MobEffects.CONFUSION)) {
                tags.merge(Aspect.ELDRITCH, level * 3);
            } else if (type.is(MobEffects.DAMAGE_BOOST)) {
                tags.merge(Aspect.WEAPON, level * 3);
            } else if (type.is(MobEffects.DIG_SLOWDOWN)) {
                tags.merge(Aspect.TRAP, level * 3);
            } else if (type.is(MobEffects.DIG_SPEED)) {
                tags.merge(Aspect.TOOL, level * 3);
            } else if (type.is(MobEffects.FIRE_RESISTANCE)) {
                tags.merge(Aspect.ARMOR, level);
                tags.merge(Aspect.FIRE, level * 2);
            } else if (type.is(MobEffects.HARM)) {
                tags.merge(Aspect.DEATH, level * 3);
            } else if (type.is(MobEffects.HEAL)) {
                tags.merge(Aspect.HEAL, level * 3);
            } else if (type.is(MobEffects.HUNGER)) {
                tags.merge(Aspect.DEATH, level * 3);
            } else if (type.is(MobEffects.INVISIBILITY)) {
                tags.merge(Aspect.SENSES, level * 3);
            } else if (type.is(MobEffects.JUMP)) {
                tags.merge(Aspect.FLIGHT, level * 3);
            } else if (type.is(MobEffects.MOVEMENT_SLOWDOWN)) {
                tags.merge(Aspect.TRAP, level * 3);
            } else if (type.is(MobEffects.MOVEMENT_SPEED)) {
                tags.merge(Aspect.MOTION, level * 3);
            } else if (type.is(MobEffects.NIGHT_VISION)) {
                tags.merge(Aspect.SENSES, level * 3);
            } else if (type.is(MobEffects.POISON)) {
                tags.merge(Aspect.POISON, level * 3);
            } else if (type.is(MobEffects.REGENERATION)) {
                tags.merge(Aspect.HEAL, level * 3);
            } else if (type.is(MobEffects.DAMAGE_RESISTANCE)) {
                tags.merge(Aspect.ARMOR, level * 3);
            } else if (type.is(MobEffects.WATER_BREATHING)) {
                tags.merge(Aspect.AIR, level * 3);
            } else if (type.is(MobEffects.WEAKNESS)) {
                tags.merge(Aspect.DEATH, level * 3);
            }
        }
        return tags;
    }

    private static @Nullable Aspect enchantmentAspect(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(key -> ENCHANTMENT_ASPECTS.get(key.location())).orElse(null);
    }

    private static double attributeTotal(ItemStack stack, Holder<Attribute> attribute, EquipmentSlotGroup slot) {
        double[] total = {0};
        stack.getAttributeModifiers().forEach(slot, (holder, modifier) -> {
            if (holder.is(attribute)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    private static int harvestLevel(ItemStack stack) {
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) {
            return 0;
        }
        for (Tool.Rule rule : tool.rules()) {
            if (rule.correctForDrops().isPresent() && !rule.correctForDrops().get()) {
                Optional<TagKey<Block>> tag = rule.blocks().unwrapKey();
                if (tag.isPresent() && HARVEST_LEVELS.containsKey(tag.get())) {
                    return HARVEST_LEVELS.get(tag.get());
                }
            }
        }
        return 0;
    }
}
