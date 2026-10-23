package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.device.ArcaneBoreMenu;
import thaumcraft.device.ArcaneSpaMenu;
import thaumcraft.menu.AlchemicalFurnaceMenu;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.menu.DeconstructionTableMenu;
import thaumcraft.menu.FocalManipulatorMenu;
import thaumcraft.menu.FocusPouchMenu;
import thaumcraft.menu.HoverHarnessMenu;
import thaumcraft.menu.ResearchTableMenu;
import thaumcraft.menu.ThaumatoriumMenu;
import thaumcraft.menu.PechTradeMenu;
import thaumcraft.platform.Menus;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Thaumcraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchTableMenu>> RESEARCH_TABLE = MENUS.register("research_table", () -> Menus.withPos(ResearchTableMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AlchemicalFurnaceMenu>> ALCHEMICAL_FURNACE = MENUS.register("alchemical_furnace", () -> Menus.withPos(AlchemicalFurnaceMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ThaumatoriumMenu>> THAUMATORIUM = MENUS.register("thaumatorium", () -> Menus.withPos(ThaumatoriumMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH = MENUS.register("arcane_workbench", () -> Menus.withPos(ArcaneWorkbenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DeconstructionTableMenu>> DECONSTRUCTION_TABLE = MENUS.register("deconstruction_table", () -> Menus.withPos(DeconstructionTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR = MENUS.register("focal_manipulator", () -> Menus.withPos(FocalManipulatorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FocusPouchMenu>> FOCUS_POUCH = MENUS.register("focus_pouch", () -> Menus.simple(FocusPouchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<HoverHarnessMenu>> HOVER_HARNESS = MENUS.register("hover_harness", () -> Menus.simple(HoverHarnessMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<thaumcraft.device.HandMirrorMenu>> HAND_MIRROR = MENUS.register("hand_mirror", () -> Menus.simple(thaumcraft.device.HandMirrorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneBoreMenu>> ARCANE_BORE = MENUS.register("arcane_bore", () -> Menus.withPos(ArcaneBoreMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneSpaMenu>> ARCANE_SPA = MENUS.register("arcane_spa", () -> Menus.withPos(ArcaneSpaMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PechTradeMenu>> PECH_TRADE = MENUS.register("pech_trade", () -> IMenuTypeExtension.create((id, inventory, buffer) -> new PechTradeMenu(id, inventory, buffer.readVarInt())));
    public static final DeferredHolder<MenuType<?>, MenuType<thaumcraft.menu.GolemMenu>> GOLEM = MENUS.register("golem", () -> Menus.withEntity(thaumcraft.menu.GolemMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<thaumcraft.menu.TravelingTrunkMenu>> TRAVELING_TRUNK = MENUS.register("traveling_trunk", () -> Menus.withEntity(thaumcraft.menu.TravelingTrunkMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
