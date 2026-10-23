package thaumcraft.client.render.entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ChampionSlimeFx;
import thaumcraft.client.fx.CrystalSparkFx;
import thaumcraft.client.fx.GenericFx;
import thaumcraft.client.platform.ParticleSprites;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.entity.monster.ChampionModifier;
import net.minecraft.world.entity.Mob;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ChampionFx {
    private ChampionFx() {}

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !mob.level().isClientSide()) return;
        int id = ChampionModifier.get(mob);
        if (id < 0 || id >= ChampionModifier.count()) return;
        var random = mob.getRandom();
        if (random.nextFloat() >= switch (id) { case 2 -> 0.25F; case 3 -> 0.30F; case 8 -> 1.0F; case 11 -> 0.20F; default -> 0.5F; }) return;
        double x = mob.getX() - mob.getBbWidth() / 2.0 + random.nextFloat() * mob.getBbWidth();
        double y = mob.getY() + random.nextFloat() * mob.getBbHeight();
        double z = mob.getZ() - mob.getBbWidth() / 2.0 + random.nextFloat() * mob.getBbWidth();
        if (id == 0) {
            WorldFxRenderer.add(new CrystalSparkFx(random, x, mob.getY() + random.nextFloat() * mob.getBbHeight() / 3.0F, z, 0.2F)
                .color(0.3F - random.nextFloat() * 0.1F, 0.0F, 0.8F + random.nextFloat() * 0.2F));
            return;
        }
        if (id == 12) {
            var sprite = ParticleSprites.item(mob.level(), new ItemStack(Items.SLIME_BALL), random);
            if (sprite != null) WorldFxRenderer.add(new ChampionSlimeFx(random, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0, mob.getZ(), sprite));
            return;
        }
        int start;
        int count;
        int increment;
        int age;
        boolean loop = false;
        float scale;
        float red = 1.0F, green = 1.0F, blue = 1.0F, alpha = 0.7F;
        double vy = 0.0;
        switch (id) {
            case 0 -> { start = 72; count = 8; increment = 1; age = 8; red = 0.3F; green = 0.0F; blue = 0.9F; scale = 0.2F; }
            case 1 -> { start = 176 + random.nextInt(4) * 3; count = 3; increment = 1; age = 3; red = 0.6F; green = 0.2F; blue = 0.2F; scale = 1.25F; }
            case 2 -> { start = 112; count = 9; increment = 1; age = 5 + random.nextInt(4); red = 0.9F; green = 0.9F; blue = 0.95F; alpha = 0.7F; scale = 0.7F; }
            case 3 -> { start = 176 + random.nextInt(4) * 3; count = 3; increment = 1; age = 4 + random.nextInt(3); red = 0.9F; green = 0.9F; blue = 0.9F; scale = 1.15F; }
            case 4 -> { start = 160; count = 10; increment = 1; age = 8 + random.nextInt(4); red = random.nextFloat() * 0.2F; green = random.nextFloat() * 0.2F; blue = random.nextFloat() * 0.2F; alpha = 0.8F; scale = 0.8F; vy = -0.02; }
            case 5 -> { start = 21; count = 4; increment = 1; age = 4 + random.nextInt(4); loop = true; red = 0.55F; green = 0.55F; blue = 0.55F; alpha = 0.6F; scale = 0.95F; }
            case 6 -> { start = 72; count = 8; increment = 1; age = 10 + random.nextInt(4); loop = true; red = 0.9F; green = 0.0F; blue = 0.95F; scale = 0.8F; }
            case 7 -> { start = 21; count = 4; increment = 1; age = 4 + random.nextInt(4); loop = true; red = 0.15F; green = 0.9F; blue = 0.15F; scale = 0.6F; vy = 0.03; alpha = 0.9F; }
            case 8 -> { start = 160; count = 10; increment = 1; age = 8 + random.nextInt(4); red = 0.95F; green = 1.0F; blue = 1.0F; scale = 0.8F; vy = 0.03; }
            case 9 -> { start = 1; count = 4; increment = 2; age = 5 + random.nextInt(4); red = 0.2F; green = 0.65F; blue = 0.25F; alpha = 0.5F; scale = 1.0F; vy = -0.02; }
            case 10 -> { start = 147; count = 4; increment = 1; age = 8 + random.nextInt(4); red = 0.2F; green = 0.65F; blue = 0.25F; scale = 0.6F; vy = 0.02; }
            case 11 -> { start = 147; count = 4; increment = 1; age = 8 + random.nextInt(4); red = 0.95F; green = 0.0F; blue = 0.0F; alpha = 0.9F; scale = 0.6F; }
            case 12 -> { start = 160; count = 10; increment = 1; age = 8; red = 0.2F; green = 0.8F; blue = 0.2F; scale = 0.8F; }
            default -> { return; }
        }
        GenericFx fx = new GenericFx(random, x, y, z, 0.0, vy, 0.0, loop, start, count, increment, age, 0, scale);
        fx.color(red, green, blue).alpha(alpha);
        WorldFxRenderer.add(fx);
    }
}
