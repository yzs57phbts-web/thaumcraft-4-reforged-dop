import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.LoadingModList;
import sun.misc.Unsafe;
import thaumcraft.entity.boss.EldritchGolem;
import thaumcraft.entity.boss.EldritchWarden;
import thaumcraft.entity.golem.GolemBobber;

public final class ClientDamageTest {
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        LoadingModList.of(List.of(), List.of(), List.of(), new ArrayList<>(), Map.of());
        Bootstrap.bootStrap();
        Field access = Unsafe.class.getDeclaredField("theUnsafe");
        access.setAccessible(true);
        Unsafe memory = (Unsafe) access.get(null);
        ClientLevel client = (ClientLevel) memory.allocateInstance(ClientLevel.class);
        memory.putBoolean(client, memory.objectFieldOffset(Level.class.getDeclaredField("isClientSide")), true);
        if (!client.isClientSide()) throw new AssertionError("client fixture has the wrong logical side");
        long levelField = memory.objectFieldOffset(Entity.class.getDeclaredField("level"));
        DamageSource source = new DamageSource(Holder.direct(new DamageType("client_test", 0.0F)));
        for (Class<? extends Entity> type : List.of(EldritchWarden.class, EldritchGolem.class, GolemBobber.class)) {
            Entity entity = (Entity) memory.allocateInstance(type);
            memory.putObject(entity, levelField, client);
            for (float damage : new float[]{0.0F, 1.0F, 1000.0F}) {
                if (entity.hurt(source, damage)) throw new AssertionError(type.getSimpleName() + " accepted client damage");
            }
            System.out.println(type.getSimpleName() + ": client damage rejected without accessing server state");
        }
    }
}
