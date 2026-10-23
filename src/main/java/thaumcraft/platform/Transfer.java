package thaumcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import javax.annotation.Nullable;

public final class Transfer {
    private static final List<Exposure<?>> ITEMS = new ArrayList<>();
    private static final List<Exposure<?>> FLUIDS = new ArrayList<>();

    private Transfer() {
    }

    private record Exposure<B extends BlockEntity>(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, Direction, Object> provider) {
    }

    public static final class FluidTank {
        private final Handler handler;

        public FluidTank(int capacity, Predicate<FluidStack> filter, boolean extractable, Runnable onChanged) {
            this.handler = new Handler(capacity, filter, extractable, onChanged);
        }

        public FluidStack getFluid() {
            return handler.getFluid();
        }

        public void setFluid(FluidStack stack) {
            handler.setFluid(stack);
            handler.onContentsChanged();
        }

        public int getAmount() {
            return handler.getFluidAmount();
        }

        public int getCapacity() {
            return handler.getCapacity();
        }

        private static final class Handler extends net.neoforged.neoforge.fluids.capability.templates.FluidTank {
            private final boolean extractable;
            private final Runnable onChanged;

            private Handler(int capacity, Predicate<FluidStack> filter, boolean extractable, Runnable onChanged) {
                super(capacity, filter);
                this.extractable = extractable;
                this.onChanged = onChanged;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return extractable ? super.drain(maxDrain, action) : FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return extractable ? super.drain(resource, action) : FluidStack.EMPTY;
            }

            @Override
            protected void onContentsChanged() {
                onChanged.run();
            }
        }
    }

    public static <B extends BlockEntity> void exposeItems(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, Direction, Container> provider) {
        ITEMS.add(new Exposure<>(type, provider::apply));
    }

    public static <B extends BlockEntity> void exposeFluids(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, Direction, FluidTank> provider) {
        FLUIDS.add(new Exposure<>(type, provider::apply));
    }

    public static void register(IEventBus bus) {
        bus.addListener(Transfer::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Exposure<?> exposure : ITEMS) {
            registerItems(event, exposure);
        }
        for (Exposure<?> exposure : FLUIDS) {
            registerFluids(event, exposure);
        }
    }

    private static <B extends BlockEntity> void registerItems(RegisterCapabilitiesEvent event, Exposure<B> exposure) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, exposure.type().get(), (entity, side) -> {
            Object container = exposure.provider().apply(entity, side);
            if (container instanceof WorldlyContainer worldly && side != null) {
                return new SidedInvWrapper(worldly, side);
            }
            return container instanceof Container plain ? new InvWrapper(plain) : null;
        });
    }

    private static <B extends BlockEntity> void registerFluids(RegisterCapabilitiesEvent event, Exposure<B> exposure) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, exposure.type().get(), (entity, side) ->
            exposure.provider().apply(entity, side) instanceof FluidTank tank ? tank.handler : null
        );
    }

    private static @Nullable IItemHandler items(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    private static @Nullable IFluidHandler fluids(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
    }

    public static boolean hasItems(Level level, BlockPos pos, @Nullable Direction side) {
        return items(level, pos, side) != null;
    }

    public static boolean hasFluids(Level level, BlockPos pos, @Nullable Direction side) {
        return fluids(level, pos, side) != null;
    }

    public static List<ItemStack> contents(Level level, BlockPos pos, @Nullable Direction side) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null) return List.of();
        List<ItemStack> contents = new java.util.ArrayList<>();
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack stack = handler.getStackInSlot(index);
            if (!stack.isEmpty()) contents.add(stack.copy());
        }
        return List.copyOf(contents);
    }

    public static int count(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack stack = handler.getStackInSlot(index);
            if (!stack.isEmpty() && filter.test(stack.copyWithCount(1))) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static ItemStack insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
    }

    public static ItemStack extract(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter, int maxAmount, boolean simulate) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null || maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack stack = handler.getStackInSlot(index);
            if (stack.isEmpty() || !filter.test(stack.copyWithCount(1))) {
                continue;
            }
            ItemStack extracted = handler.extractItem(index, maxAmount, simulate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int countFluid(Level level, BlockPos pos, @Nullable Direction side, Predicate<FluidStack> filter) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.getTanks(); index++) {
            FluidStack fluid = handler.getFluidInTank(index);
            if (!fluid.isEmpty() && filter.test(fluid.copyWithAmount(1))) {
                total += fluid.getAmount();
            }
        }
        return total;
    }

    public static FluidStack insertFluid(Level level, BlockPos pos, @Nullable Direction side, FluidStack stack, boolean simulate) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int inserted = handler.fill(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        return stack.copyWithAmount(stack.getAmount() - inserted);
    }

    public static FluidStack extractFluid(Level level, BlockPos pos, @Nullable Direction side, Predicate<FluidStack> filter, int maxAmount, boolean simulate) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null || maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int index = 0; index < handler.getTanks(); index++) {
            FluidStack fluid = handler.getFluidInTank(index);
            if (fluid.isEmpty() || !filter.test(fluid.copyWithAmount(1))) {
                continue;
            }
            FluidStack extracted = handler.drain(fluid.copyWithAmount(maxAmount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return FluidStack.EMPTY;
    }
}
