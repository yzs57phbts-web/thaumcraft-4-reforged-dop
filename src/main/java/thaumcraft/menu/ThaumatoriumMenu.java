package thaumcraft.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.alchemy.ThaumatoriumBlockEntity;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModMenus;

public class ThaumatoriumMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Player player;
    private final ThaumatoriumBlockEntity thaumatorium;
    private final Container container;
    private final List<CrucibleRecipe> recipes = new ArrayList<>();

    public ThaumatoriumMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.THAUMATORIUM.get(), containerId);
        this.pos = pos;
        this.player = inventory.player;
        this.thaumatorium = player.level().getBlockEntity(pos) instanceof ThaumatoriumBlockEntity entity ? entity : null;
        this.container = thaumatorium != null ? thaumatorium : new SimpleContainer(1);
        if (thaumatorium != null) {
            thaumatorium.setListener(this);
        }
        addSlot(new Slot(container, 0, 48, 16));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        updateRecipes();
    }

    public BlockPos pos() {
        return pos;
    }

    public ThaumatoriumBlockEntity thaumatorium() {
        return thaumatorium;
    }

    public List<CrucibleRecipe> recipes() {
        return recipes;
    }

    public void updateRecipes() {
        recipes.clear();
        if (thaumatorium == null) {
            return;
        }
        ItemStack input = thaumatorium.getInputStack();
        for (CrucibleRecipe recipe : TcRecipes.all(CrucibleRecipe.class)) {
            if (CrucibleCrafting.isResearched(player, recipe) && CrucibleCrafting.catalystMatches(recipe, input)) {
                recipes.add(recipe);
            } else if (thaumatorium.hasRecipe(recipe.key())) {
                recipes.add(recipe);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        updateRecipes();
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (thaumatorium != null) {
            thaumatorium.setListener(null);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (thaumatorium == null || id < 0 || id >= recipes.size()) {
            return false;
        }
        return thaumatorium.toggleRecipe(recipes.get(id), player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index != 0) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 1, 37, true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return thaumatorium != null && thaumatorium.stillValid(player);
    }
}
