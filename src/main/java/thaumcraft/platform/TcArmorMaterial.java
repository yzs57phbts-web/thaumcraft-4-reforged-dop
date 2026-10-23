package thaumcraft.platform;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record TcArmorMaterial(
    int durabilityMultiplier,
    int helmetDefense,
    int chestplateDefense,
    int leggingsDefense,
    int bootsDefense,
    int enchantability,
    Holder<SoundEvent> equipSound,
    float toughness,
    float knockbackResistance,
    TagKey<Item> repairItems,
    String assetName
) {
    public TcArmorMaterial(int durabilityMultiplier, int helmetDefense, int chestplateDefense, int leggingsDefense, int bootsDefense, int enchantability, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairItems, net.minecraft.resources.ResourceLocation assetId) {
        this(durabilityMultiplier, helmetDefense, chestplateDefense, leggingsDefense, bootsDefense, enchantability, equipSound, toughness, knockbackResistance, repairItems, assetId.toString());
    }

    public net.minecraft.resources.ResourceLocation assetId() {
        return assetName.contains(":") ? net.minecraft.resources.ResourceLocation.parse(assetName) : thaumcraft.Thaumcraft.id(assetName);
    }

    public int defense(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> helmetDefense;
            case CHESTPLATE -> chestplateDefense;
            case LEGGINGS -> leggingsDefense;
            case BOOTS -> bootsDefense;
        };
    }
}
