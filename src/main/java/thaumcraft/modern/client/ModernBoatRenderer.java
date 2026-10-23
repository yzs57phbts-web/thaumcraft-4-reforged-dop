package thaumcraft.modern.client;
import thaumcraft.modern.*;
import thaumcraft.Thaumcraft;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
/** Vanilla hull, two animated paddles, passenger poses, damage tilt and water mask. */
final class ModernBoatRenderer extends BoatRenderer {
    private final ListModel<Boat> model;
    private final boolean chest;
    ModernBoatRenderer(EntityRendererProvider.Context context){this(context,false);}
    ModernBoatRenderer(EntityRendererProvider.Context context,boolean chest){super(context,chest);this.chest=chest;model=chest?new ChestBoatModel(context.bakeLayer(ModelLayers.createChestBoatModelName(Boat.Type.OAK))):new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));}
    @Override public Pair<ResourceLocation,ListModel<Boat>> getModelWithLocation(Boat boat){return Pair.of(Thaumcraft.id("textures/entity/modern_"+(chest?"chest_boat_":"boat_")+(((MagicalWoodBoat)boat).silver()?"silverwood":"greatwood")+".png"),model);}
}
