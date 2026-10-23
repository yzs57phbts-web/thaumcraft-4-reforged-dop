package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.registry.ModBlocks;

final class EnchantmentCategory implements IRecipeCategory<EnchantmentCategory.Entry> {
    static final RecipeType<Entry> TYPE = new RecipeType<>(Thaumcraft.id("infusion_enchantment"), Entry.class);

    record Entry(InfusionEnchantmentRecipe recipe, List<ItemStack> books) {
    }

    private final IDrawable icon;

    EnchantmentCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(ModBlocks.RUNIC_MATRIX.get());
    }

    @Override
    public RecipeType<Entry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.type.infusionenchantment");
    }

    @Override
    public int getWidth() {
        return InfusionLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return InfusionLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        builder.addInputSlot(InfusionLayout.CENTER_X - 8, InfusionLayout.CENTER_Y - 8).setStandardSlotBackground().addItemStack(new ItemStack(Items.BOOK));
        InfusionLayout.components(builder, entry.recipe().key(), entry.recipe().components());
        builder.addOutputSlot(InfusionLayout.WIDTH - 20, 0).setOutputSlotBackground().addItemStacks(entry.books());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemLike(ModBlocks.RUNIC_MATRIX.get());
    }

    @Override
    public void draw(Entry entry, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AspectStrip.draw(graphics, entry.recipe().aspects(), InfusionLayout.PER_ROW, InfusionLayout.WIDTH, InfusionLayout.ASPECTS_Y, 1);
        InfusionLayout.instability(graphics, entry.recipe().instability());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Entry entry, IRecipeSlotsView slots, double mouseX, double mouseY) {
        AspectStrip.tooltip(tooltip, entry.recipe().aspects(), InfusionLayout.PER_ROW, InfusionLayout.WIDTH, InfusionLayout.ASPECTS_Y, mouseX, mouseY);
    }
}
