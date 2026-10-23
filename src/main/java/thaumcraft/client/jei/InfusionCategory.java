package thaumcraft.client.jei;

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
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.registry.ModBlocks;

final class InfusionCategory implements IRecipeCategory<InfusionRecipe> {
    static final RecipeType<InfusionRecipe> TYPE = new RecipeType<>(Thaumcraft.id("infusion"), InfusionRecipe.class);

    private final IDrawable icon;

    InfusionCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(ModBlocks.RUNIC_MATRIX.get());
    }

    @Override
    public RecipeType<InfusionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.type.infusion");
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
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
        TcJei.accept(builder.addInputSlot(InfusionLayout.CENTER_X - 8, InfusionLayout.CENTER_Y - 8).setStandardSlotBackground(), recipe.central());
        InfusionLayout.components(builder, recipe.key(), recipe.components());
        TcJei.accept(builder.addOutputSlot(InfusionLayout.WIDTH - 20, 0).setOutputSlotBackground(), recipe.result());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemLike(ModBlocks.RUNIC_MATRIX.get());
    }

    @Override
    public void draw(InfusionRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AspectStrip.draw(graphics, recipe.aspects(), InfusionLayout.PER_ROW, InfusionLayout.WIDTH, InfusionLayout.ASPECTS_Y, 1);
        InfusionLayout.instability(graphics, recipe.instability());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, InfusionRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        AspectStrip.tooltip(tooltip, recipe.aspects(), InfusionLayout.PER_ROW, InfusionLayout.WIDTH, InfusionLayout.ASPECTS_Y, mouseX, mouseY);
    }
}
