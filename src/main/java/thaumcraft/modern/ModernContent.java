package thaumcraft.modern;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.*;
import thaumcraft.crafting.*;
import thaumcraft.aspect.*;
import thaumcraft.research.*;

/** TC owns its wood families and floating candles. Historical addon IDs are migration aliases. */
public final class ModernContent {
    public static final Map<String,DeferredBlock<? extends Block>> WOOD=new LinkedHashMap<>();
    public static final Map<String,DeferredBlock<? extends FloatingCandleBlock>> CANDLES=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<? extends Item>> BOATS=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<? extends Item>> CHEST_BOATS=new LinkedHashMap<>();
    public static final Map<String,DeferredBlock<? extends FloatingCandleBlock>> LEGACY_CANDLES=new LinkedHashMap<>();
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    public static final DeferredHolder<EntityType<?>,EntityType<WoodBoat>> BOAT=ENTITIES.register("wood_boat",()->EntityType.Builder.<WoodBoat>of(WoodBoat::new,MobCategory.MISC).sized(1.375F,.5625F).clientTrackingRange(10).build("thaumcraft:wood_boat"));
    public static final DeferredHolder<EntityType<?>,EntityType<WoodChestBoat>> CHEST_BOAT=ENTITIES.register("wood_chest_boat",()->EntityType.Builder.<WoodChestBoat>of(WoodChestBoat::new,MobCategory.MISC).sized(1.375F,.5625F).clientTrackingRange(10).build("thaumcraft:wood_chest_boat"));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FloatingCandleEntity>> CANDLE=TILES.register("floating_candle",()->BlockEntityType.Builder.of(FloatingCandleEntity::new,java.util.stream.Stream.concat(CANDLES.values().stream(),LEGACY_CANDLES.values().stream()).map(b->b.get()).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ModernSigns.Entity>> SIGN=TILES.register("wood_sign",()->BlockEntityType.Builder.of(ModernSigns.Entity::new,WOOD.entrySet().stream().filter(e->e.getKey().endsWith("_sign")&&!e.getKey().contains("_hanging_sign")).map(e->e.getValue().get()).toArray(Block[]::new)).build(null));
    public static void init(IEventBus bus) {
        for(String wood:List.of("greatwood","silverwood")) {
            var type=wood.equals("greatwood")?BlockSetType.OAK:BlockSetType.BIRCH;
            var sound=wood.equals("greatwood")?WoodType.OAK:WoodType.BIRCH;
            add(wood+"_fence",()->new FenceBlock(properties()));
            add(wood+"_fence_gate",()->new FenceGateBlock(sound,properties()));
            add(wood+"_door",()->new DoorBlock(type,properties().noOcclusion()));
            add(wood+"_trapdoor",()->new TrapDoorBlock(type,properties().noOcclusion()));
            ModernSigns.register(wood,type);
            BOATS.put(wood,ModItems.ITEMS.registerItem(wood+"_boat",p->new WoodBoatItem(wood,p),new Item.Properties().stacksTo(1)));
            CHEST_BOATS.put(wood,ModItems.ITEMS.registerItem(wood+"_chest_boat",p->new WoodBoatItem(wood,true,p),new Item.Properties().stacksTo(1)));
        }
        for(String color:ModBlocks.CANDLE_COLORS) {
            String name=color+"_floating_candle";
            CANDLES.put(color,ModBlocks.CANDLES.get(color));
            var block=ModBlocks.BLOCKS.register(name,()->new FloatingCandleBlock(BlockBehaviour.Properties.of().strength(.1F).noCollission().noOcclusion().lightLevel(s->14).sound(SoundType.WOOL)){
                @Override public net.minecraft.world.level.block.state.BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context){return CANDLES.get(color).get().getStateForPlacement(context);}
            });
            LEGACY_CANDLES.put(color,block);ModItems.ITEMS.registerSimpleBlockItem(name,block);
        }
        ENTITIES.register(bus);TILES.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent e)->e.modify(BlockEntityType.HANGING_SIGN,WOOD.entrySet().stream().filter(entry->entry.getKey().endsWith("_hanging_sign")).map(entry->entry.getValue().get()).toArray(Block[]::new)));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(UnifiedCandleMigration::loaded);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(UnifiedCandleMigration::inventory);
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e)->e.registerEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ENTITY,CHEST_BOAT.get(),(boat,context)->new net.neoforged.neoforge.items.wrapper.InvWrapper(boat)));
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event)->event.enqueueWork(()->{
            WOOD.values().forEach(b->((FireBlock)Blocks.FIRE).setFlammable(b.get(),5,20));
            BOATS.forEach((wood,item)->WoodBoatItem.dispenser(wood,false,item.get()));
            CHEST_BOATS.forEach((wood,item)->WoodBoatItem.dispenser(wood,true,item.get()));
        }));
    }
    private static BlockBehaviour.Properties properties(){return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).ignitedByLava();}
    private static void add(String name,java.util.function.Supplier<? extends Block> supplier){var b=ModBlocks.BLOCKS.register(name,supplier);WOOD.put(name,b);ModItems.ITEMS.registerSimpleBlockItem(name,b);}
    public static void recipes(){
        for(String wood:List.of("greatwood","silverwood"))for(String kind:List.of("fence","fence_gate","door","trapdoor","boat","chest_boat","sign","hanging_sign")) {
            String name=wood+"_"+kind;
            TcRecipes.register(new CraftingRecipeRef("Wood:"+name,"",Thaumcraft.id(name),TcStack.of("thaumcraft:"+name,kind.equals("fence")||kind.equals("door")||kind.equals("sign")?3:kind.equals("trapdoor")?2:kind.equals("hanging_sign")?6:1)));
        }
    }
    // No extra woodwork or floating-candle research nodes: candles are already floating.
    public static void research(){}
}
