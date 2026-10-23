package thaumcraft.modern;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
public final class WoodChestBoat extends ChestBoat implements MagicalWoodBoat {
    private static final EntityDataAccessor<Boolean> SILVER=SynchedEntityData.defineId(WoodChestBoat.class,EntityDataSerializers.BOOLEAN);
    public WoodChestBoat(EntityType<? extends Boat> type,Level level){super(type,level);}
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SILVER,false);}
    public boolean silver(){return entityData.get(SILVER);}
    public void wood(String wood){entityData.set(SILVER,wood.equals("silverwood"));}
    public Item getDropItem(){return ModernContent.CHEST_BOATS.get(silver()?"silverwood":"greatwood").get();}
    @Override protected net.minecraft.network.chat.Component getTypeName(){return net.minecraft.network.chat.Component.translatable("entity.thaumcraft.wood_chest_boat");}
    protected void addAdditionalSaveData(CompoundTag nbt){super.addAdditionalSaveData(nbt);nbt.putBoolean("Silverwood",silver());}
    protected void readAdditionalSaveData(CompoundTag nbt){super.readAdditionalSaveData(nbt);entityData.set(SILVER,nbt.getBoolean("Silverwood"));}
}
