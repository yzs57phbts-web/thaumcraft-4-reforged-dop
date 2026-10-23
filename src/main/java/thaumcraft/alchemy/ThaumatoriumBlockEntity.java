package thaumcraft.alchemy;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.neoforged.neoforge.event.EventHooks;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.fx.Fx;
import thaumcraft.menu.ThaumatoriumMenu;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class ThaumatoriumBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport, WorldlyContainer, MenuProvider {
    public static final int EVENT_VENT = 0;
    private static final int[] SLOTS = {0};

    private final NonNullList<ItemStack> input = NonNullList.withSize(1, ItemStack.EMPTY);
    private AspectList essentia = new AspectList();
    private final List<String> recipeKeys = new ArrayList<>();
    private final List<UUID> recipePlayers = new ArrayList<>();
    private int currentCraft = -1;
    private int maxRecipes = 1;
    private @Nullable Aspect currentSuction;
    private int venting;
    private int counter;
    private boolean heated;
    private @Nullable CrucibleRecipe currentRecipe;
    private @Nullable ThaumatoriumMenu listener;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    protected ThaumatoriumBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable WorldlyContainer exposedItems(@Nullable Direction side) {
        return this;
    }

    public Direction getFacing() {
        return getBlockState().getValue(ThaumatoriumBlock.FACING);
    }

    public int getMaxRecipes() {
        return maxRecipes;
    }

    public List<String> getRecipeKeys() {
        return recipeKeys;
    }

    public int getCurrentCraft() {
        return currentCraft;
    }

    public AspectList getEssentia() {
        return essentia;
    }

    public @Nullable Aspect getCurrentSuction() {
        return currentSuction;
    }

    public ItemStack getInputStack() {
        return input.get(0);
    }

    public void setListener(@Nullable ThaumatoriumMenu listener) {
        this.listener = listener;
    }

    public boolean hasRecipe(String key) {
        return recipeKeys.contains(key);
    }

    public boolean toggleRecipe(CrucibleRecipe recipe, Player player) {
        int index = recipeKeys.indexOf(recipe.key());
        if (index >= 0) {
            recipeKeys.remove(index);
            recipePlayers.remove(index);
            currentCraft = -1;
        } else {
            recipeKeys.add(recipe.key());
            recipePlayers.add(player.getUUID());
        }
        sync();
        return true;
    }

    public @Nullable CrucibleRecipe recipeAt(int index) {
        if (index < 0 || index >= recipeKeys.size()) {
            return null;
        }
        return TcRecipes.get(recipeKeys.get(index)) instanceof CrucibleRecipe recipe ? recipe : null;
    }

    public @Nullable ItemStack getCurrentOutputRecipe() {
        CrucibleRecipe recipe = currentCraft >= 0 ? recipeAt(currentCraft) : null;
        return recipe == null ? null : recipe.result().create();
    }

    private boolean checkHeat() {
        BlockPos source = worldPosition.below(2);
        BlockState state = level.getBlockState(source);
        return state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.FIRE) || AlchemyHooks.isNitor(level, source, state);
    }

    public boolean gettingPower() {
        return level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.below()) || level.hasNeighborSignal(worldPosition.above());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity thaumatorium) {
        if (thaumatorium.venting > 0) {
            thaumatorium.venting--;
            RandomSource random = level.getRandom();
            Direction facing = thaumatorium.getFacing();
            float fx = 0.1F - random.nextFloat() * 0.2F;
            float fz = 0.1F - random.nextFloat() * 0.2F;
            float fy = 0.1F - random.nextFloat() * 0.2F;
            float fx2 = 0.1F - random.nextFloat() * 0.2F;
            float fz2 = 0.1F - random.nextFloat() * 0.2F;
            float fy2 = 0.1F - random.nextFloat() * 0.2F;
            Fx.get().ventParticles(
                level,
                pos.getX() + 0.5F + fx + facing.getStepX() / 2.0F,
                pos.getY() + 0.5F + fy,
                pos.getZ() + 0.5F + fz + facing.getStepZ() / 2.0F,
                facing.getStepX() / 4.0F + fx2,
                fy2,
                facing.getStepZ() / 4.0F + fz2,
                16777215,
                1.0F
            );
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity thaumatorium) {
        if (thaumatorium.counter == 0 || thaumatorium.counter % 40 == 0) {
            thaumatorium.heated = thaumatorium.checkHeat();
            thaumatorium.getUpgrades();
        }
        thaumatorium.counter++;
        if (!thaumatorium.heated || thaumatorium.gettingPower() || thaumatorium.counter % 5 != 0 || thaumatorium.recipeKeys.isEmpty()) {
            return;
        }
        ItemStack stack = thaumatorium.input.get(0);
        if (stack.isEmpty()) {
            thaumatorium.currentSuction = null;
            return;
        }
        if (thaumatorium.currentCraft < 0
            || thaumatorium.currentCraft >= thaumatorium.recipeKeys.size()
            || thaumatorium.currentRecipe == null
            || !CrucibleCrafting.catalystMatches(thaumatorium.currentRecipe, stack)) {
            for (int index = 0; index < thaumatorium.recipeKeys.size(); index++) {
                CrucibleRecipe recipe = thaumatorium.recipeAt(index);
                if (recipe != null && CrucibleCrafting.catalystMatches(recipe, stack)) {
                    thaumatorium.currentCraft = index;
                    thaumatorium.currentRecipe = recipe;
                    break;
                }
            }
        }
        if (thaumatorium.currentCraft < 0 || thaumatorium.currentCraft >= thaumatorium.recipeKeys.size()) {
            return;
        }
        Direction facing = thaumatorium.getFacing();
        BlockPos outputPos = pos.relative(facing);
        ItemStack output = thaumatorium.getCurrentOutputRecipe();
        if (output != null && Transfer.hasItems(level, outputPos, facing.getOpposite())) {
            ItemStack remaining = Transfer.insert(level, outputPos, facing.getOpposite(), output, true);
            if (!remaining.isEmpty()) {
                return;
            }
        }
        CrucibleRecipe recipe = thaumatorium.recipeAt(thaumatorium.currentCraft);
        if (recipe == null) {
            return;
        }
        boolean done = true;
        thaumatorium.currentSuction = null;
        for (Aspect aspect : recipe.aspects().getAspectsSorted()) {
            if (thaumatorium.essentia.getAmount(aspect) < recipe.aspects().getAmount(aspect)) {
                thaumatorium.currentSuction = aspect;
                done = false;
                break;
            }
        }
        if (done) {
            thaumatorium.completeRecipe();
        } else if (thaumatorium.currentSuction != null) {
            thaumatorium.fill();
        }
    }

    private void completeRecipe() {
        ItemStack stack = input.get(0);
        if (currentRecipe == null || currentCraft >= recipeKeys.size() || currentCraft < 0
            || !CrucibleCrafting.matches(currentRecipe, essentia, stack) || removeItem(0, 1).isEmpty()) {
            return;
        }
        essentia = new AspectList();
        ItemStack dropped = getCurrentOutputRecipe();
        if (dropped == null) {
            return;
        }
        UUID playerId = currentCraft < recipePlayers.size() ? recipePlayers.get(currentCraft) : null;
        Player player = playerId == null ? null : level.getPlayerByUUID(playerId);
        if (player != null) {
            EventHooks.firePlayerCraftingEvent(player, dropped, new SimpleContainer(stack.copyWithCount(1)));
        }
        Direction facing = getFacing();
        BlockPos outputPos = worldPosition.relative(facing);
        if (Transfer.hasItems(level, outputPos, facing.getOpposite())) {
            dropped = Transfer.insert(level, outputPos, facing.getOpposite(), dropped, false);
        }
        if (!dropped.isEmpty()) {
            ItemEntity entity = new ItemEntity(
                level,
                worldPosition.getX() + 0.5 + facing.getStepX() * 0.66,
                worldPosition.getY() + 0.33,
                worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.66,
                dropped.copy()
            );
            entity.setDeltaMovement(0.075F * facing.getStepX(), 0.025F, 0.075F * facing.getStepZ());
            level.addFreshEntity(entity);
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_VENT, 0);
        }
        RandomSource random = level.getRandom();
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
            SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.25F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        currentCraft = -1;
        sync();
    }

    private void fill() {
        Direction facing = getFacing();
        for (int y = 0; y <= 1; y++) {
            BlockPos origin = worldPosition.above(y);
            for (Direction direction : Direction.values()) {
                if (direction == facing || direction == Direction.DOWN || y == 0 && direction == Direction.UP) {
                    continue;
                }
                EssentiaTransport transport = Essentia.getConnectable(level, origin, direction);
                if (transport == null || !transport.isConnectable(direction.getOpposite())) {
                    continue;
                }
                Direction opposite = direction.getOpposite();
                if (transport.getEssentiaAmount(opposite) > 0
                    && transport.getSuctionAmount(opposite) < getSuctionAmount(null)
                    && getSuctionAmount(null) >= transport.getMinimumSuction()) {
                    int taken = transport.takeEssentia(currentSuction, 1, opposite);
                    if (taken > 0) {
                        addToContainer(currentSuction, taken);
                        return;
                    }
                }
            }
        }
    }

    public void getUpgrades() {
        if (level == null) {
            return;
        }
        int matrices = 1;
        Direction facing = getFacing();
        for (int y = 0; y <= 1; y++) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN || direction == facing) {
                    continue;
                }
                BlockPos target = worldPosition.offset(direction.getStepX(), y + direction.getStepY(), direction.getStepZ());
                BlockState state = level.getBlockState(target);
                if (state.is(ModBlocks.MNEMONIC_MATRIX.get()) && state.getValue(MnemonicMatrixBlock.FACING) == direction.getOpposite()) {
                    matrices += 2;
                }
            }
        }
        if (matrices != maxRecipes) {
            maxRecipes = matrices;
            while (recipeKeys.size() > maxRecipes) {
                recipeKeys.remove(recipeKeys.size() - 1);
                recipePlayers.remove(recipePlayers.size() - 1);
            }
            sync();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (level != null && level.isClientSide()) {
                venting = 7;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("tile.blockMetalDevice.10.name");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ThaumatoriumMenu(containerId, inventory, worldPosition);
    }

    @Override
    public AspectList getAspects() {
        return essentia;
    }

    @Override
    public void setAspects(AspectList aspects) {
        essentia = aspects;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (currentRecipe == null) {
            return amount;
        }
        int missing = currentRecipe.aspects().getAmount(aspect) - essentia.getAmount(aspect);
        if (missing > 0) {
            int added = Math.min(missing, amount);
            essentia.add(aspect, added);
            sync();
            return amount - added;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (essentia.getAmount(aspect) >= amount) {
            essentia.remove(aspect, amount);
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return essentia.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return essentia.getAmount(aspect);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        currentSuction = aspect;
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return currentSuction != null ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return input.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return input.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(input, 0, amount);
        if (!result.isEmpty()) {
            inputChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(input, 0);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        input.set(0, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        inputChanged();
    }

    private void inputChanged() {
        setChanged();
        if (listener != null) {
            listener.updateRecipes();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        input.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.input.clear();
        ContainerHelper.loadAllItems(input.tag(), this.input, input.lookup());
        essentia = input.read("Essentia", AspectList.CODEC).orElseGet(AspectList::new);
        maxRecipes = input.getIntOr("maxrec", 1);
        List<String> keys = input.read("recipes", Codec.STRING.listOf()).orElseGet(List::of);
        List<String> players = input.read("OutputPlayer", Codec.STRING.listOf()).orElseGet(List::of);
        recipeKeys.clear();
        recipePlayers.clear();
        for (int index = 0; index < keys.size(); index++) {
            if (TcRecipes.get(keys.get(index)) instanceof CrucibleRecipe) {
                recipeKeys.add(keys.get(index));
                recipePlayers.add(index < players.size() ? parseUuid(players.get(index)) : null);
            }
        }
        currentCraft = input.getIntOr("currentCraft", -1);
        currentRecipe = null;
    }

    private static @Nullable UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), input, output.lookup());
        output.store("Essentia", AspectList.CODEC, essentia);
        output.putInt("maxrec", maxRecipes);
        output.putInt("currentCraft", currentCraft);
        output.store("recipes", Codec.STRING.listOf(), recipeKeys);
        List<String> players = new ArrayList<>();
        for (UUID playerId : recipePlayers) {
            players.add(playerId == null ? "" : playerId.toString());
        }
        output.store("OutputPlayer", Codec.STRING.listOf(), players);
    }
}
