package thaumcraft.registry;

import thaumcraft.platform.Transfer;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register() {
        Transfer.exposeItems(ModBlockEntities.ALCHEMICAL_FURNACE, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.THAUMATORIUM, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.THAUMATORIUM_TOP, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.ARCANE_WORKBENCH, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.DECONSTRUCTION_TABLE, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.PEDESTAL, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeFluids(ModBlockEntities.CRUCIBLE, (entity, side) -> entity.exposedFluids(side));
        Transfer.exposeItems(ModBlockEntities.ITEM_GRATE, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.HUNGRY_CHEST, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeItems(ModBlockEntities.ARCANE_SPA, (entity, side) -> entity.exposedItems(side));
        Transfer.exposeFluids(ModBlockEntities.ARCANE_SPA, (entity, side) -> entity.exposedFluids(side));
        Transfer.exposeItems(ModBlockEntities.MAGIC_MIRROR, (entity, side) -> entity.exposedItems(side));
    }
}
