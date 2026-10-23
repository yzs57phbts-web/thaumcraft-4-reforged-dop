package thaumcraft.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.item.gear.RunicShield;
import thaumcraft.item.armor.ModernArmorProtection;
import thaumcraft.registry.ModItems;

@GameTestHolder("thaumcraft_armor")
@PrefixGameTestTemplate(false)
public final class ArmorBalanceGameTests {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static ServerPlayer player(GameTestHelper helper) throws Exception {
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Field spawnProtection = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
        spawnProtection.setAccessible(true);
        spawnProtection.setInt(player, 0);
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        return player;
    }

    private static Object shield(ServerPlayer player, int charge) throws Exception {
        Method stateMethod = RunicShield.class.getDeclaredMethod("state", Player.class);
        stateMethod.setAccessible(true);
        Object state = stateMethod.invoke(null, player);
        set(state, "active", true);
        set(state, "charge", charge);
        set(state, "max", charge);
        return state;
    }

    private static void set(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static void equip(ServerPlayer player, ItemStack... stacks) {
        for (int i = 0; i < stacks.length; i++) {
            changeSlot(player, SLOTS[i], stacks[i]);
        }
        ModernArmorProtection.refreshFortressBonus(player);
    }

    private static void changeSlot(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
        player.getItemBySlot(slot).forEachModifier(slot, (attribute, modifier) -> player.getAttribute(attribute).removeModifier(modifier.id()));
        player.setItemSlot(slot, stack);
        stack.forEachModifier(slot, (attribute, modifier) -> player.getAttribute(attribute).addTransientModifier(modifier));
    }

    private static void close(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.001, message + ": " + actual + " != " + expected);
    }

    @GameTest(template = "test/platform")
    public static void runicRejectsOverlappingHits(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        shield(player, 8);
        var source = player.damageSources().mobAttack(new Zombie(helper.getLevel()));
        player.hurt(source, 3);
        helper.assertTrue(RunicShield.getCharge(player) == 5, "first hit costs three charge");
        close(helper, player.lastHurt, 3, "remember original hit, not shield remainder");
        helper.assertTrue(!player.hurt(source, 3), "equal repeat must be rejected");
        helper.assertTrue(!player.hurt(source, 2), "weaker repeat must be rejected");
        helper.assertTrue(RunicShield.getCharge(player) == 5, "rejected hits must not spend charge");
        player.hurt(source, 6);
        helper.assertTrue(RunicShield.getCharge(player) == 2, "stronger hit only spends the 6-3 difference");
        close(helper, player.getHealth(), 20, "shield covers accepted damage");
        player.hurt(source, 10);
        helper.assertTrue(RunicShield.getCharge(player) == 0, "remaining two charge spent");
        close(helper, player.getHealth(), 18, "10-6-2 reaches health");
        close(helper, player.lastHurt, 10, "original strong hit retained");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void runicAbsorbsBeforeArmorAndWear(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        equip(player, new ItemStack(ModItems.THAUMIUM_HELMET.get()), new ItemStack(ModItems.THAUMIUM_CHESTPLATE.get()),
            new ItemStack(ModItems.THAUMIUM_LEGGINGS.get()), new ItemStack(ModItems.THAUMIUM_BOOTS.get()));
        shield(player, 8);
        player.hurt(player.damageSources().mobAttack(new Zombie(helper.getLevel())), 10);
        close(helper, player.getHealth(), 19.12, "vanilla iron armor receives only two damage");
        for (EquipmentSlot slot : SLOTS) {
            helper.assertTrue(player.getItemBySlot(slot).getDamageValue() == 1, "wear must use the two-damage remainder");
        }
        ServerPlayer covered = player(helper);
        equip(covered, ItemStack.EMPTY, new ItemStack(ModItems.FORTRESS_CHESTPLATE.get()));
        covered.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(4);
        covered.setAbsorptionAmount(4);
        shield(covered, 20);
        covered.hurt(covered.damageSources().mobAttack(new Zombie(helper.getLevel())), 10);
        close(helper, covered.getHealth(), 20, "fully covered health");
        close(helper, covered.getAbsorptionAmount(), 4, "fully covered absorption hearts");
        helper.assertTrue(covered.getItemBySlot(EquipmentSlot.CHEST).getDamageValue() == 0, "fully covered armor must not wear");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void runicRespectsBypassSourcesAndExpiry(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        shield(player, 8);
        player.hurt(player.damageSources().wither(), 2);
        close(helper, player.getHealth(), 18, "wither still bypasses runes");
        helper.assertTrue(RunicShield.getCharge(player) == 8, "wither spends no charge");
        player.invulnerableTime = 0;
        var bypass = new net.minecraft.world.damagesource.DamageSource(player.damageSources().generic().typeHolder()) {
            @Override
            public boolean is(net.minecraft.tags.TagKey<net.minecraft.world.damagesource.DamageType> tag) {
                return tag.equals(net.minecraft.tags.DamageTypeTags.BYPASSES_COOLDOWN) || super.is(tag);
            }
        };
        player.hurt(bypass, 3);
        player.hurt(bypass, 3);
        helper.assertTrue(RunicShield.getCharge(player) == 2, "cooldown-bypassing accepted hits each spend charge");
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().mobAttack(new Zombie(helper.getLevel())), 3);
        helper.assertTrue(RunicShield.getCharge(player) == 0, "new hit after cooldown spends charge");
        close(helper, player.getHealth(), 17, "partial final hit");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void runicEmergencyAndMaskEffects(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        ItemStack helmet = new ItemStack(ModItems.FORTRESS_HELMET.get());
        InfusionKeys.TAGS.set(helmet, Map.of("mask", "1"));
        equip(player, helmet);
        Object state = shield(player, 2);
        set(state, "max", 8);
        set(state, "emergency", 1);
        Zombie attacker = new Zombie(helper.getLevel());
        player.hurt(player.damageSources().mobAttack(attacker), 10);
        helper.assertTrue(RunicShield.getCharge(player) == 8, "emergency refill survives relocation");
        helper.assertTrue(attacker.hasEffect(MobEffects.WITHER), "mask works before runic absorption on accepted hit");
        attacker.removeEffect(MobEffects.WITHER);
        player.hurt(player.damageSources().mobAttack(attacker), 10);
        helper.assertTrue(!attacker.hasEffect(MobEffects.WITHER), "rejected hit must not trigger mask");
        helper.assertTrue(RunicShield.getCharge(player) == 8, "rejected hit must not consume emergency charge");

        ServerPlayer vampire = player(helper);
        ItemStack vampireHelmet = new ItemStack(ModItems.FORTRESS_HELMET.get());
        InfusionKeys.TAGS.set(vampireHelmet, Map.of("mask", "2"));
        equip(vampire, vampireHelmet);
        vampire.setHealth(10);
        Zombie victim = new Zombie(helper.getLevel());
        victim.hurt(vampire.damageSources().playerAttack(vampire), 12);
        close(helper, vampire.getHealth(), 11, "vampire mask on a non-player accepted hit");
        victim.hurt(vampire.damageSources().playerAttack(vampire), 12);
        close(helper, vampire.getHealth(), 11, "rejected outgoing hit must not heal");
        helper.succeed();
    }

    private static ItemStack[] fortress(boolean mask) {
        ItemStack helmet = new ItemStack(ModItems.FORTRESS_HELMET.get());
        if (mask) {
            InfusionKeys.TAGS.set(helmet, Map.of("mask", "0"));
        }
        return new ItemStack[]{helmet, new ItemStack(ModItems.FORTRESS_CHESTPLATE.get()),
            new ItemStack(ModItems.FORTRESS_LEGGINGS.get()), new ItemStack(ModItems.VOID_BOOTS.get())};
    }

    private static ItemStack[] voidArmor(boolean robes) {
        return new ItemStack[]{new ItemStack(robes ? ModItems.VOID_ROBE_HELMET.get() : ModItems.VOID_HELMET.get()),
            new ItemStack(robes ? ModItems.VOID_ROBE_CHESTPLATE.get() : ModItems.VOID_CHESTPLATE.get()),
            new ItemStack(robes ? ModItems.VOID_ROBE_LEGGINGS.get() : ModItems.VOID_LEGGINGS.get()), new ItemStack(ModItems.VOID_BOOTS.get())};
    }

    private static float melee(GameTestHelper helper, ServerPlayer player, float damage) {
        float before = player.getHealth();
        player.hurt(player.damageSources().mobAttack(new Zombie(helper.getLevel())), damage);
        return before - player.getHealth();
    }

    @GameTest(template = "test/platform")
    public static void modernArmorTiersAndVanillaDamage(GameTestHelper helper) throws Exception {
        for (int index = 0; index < 6; index++) {
            ServerPlayer player = player(helper);
            ItemStack[] stacks = switch (index) {
                case 0 -> new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
                case 1 -> new ItemStack[]{new ItemStack(ModItems.THAUMIUM_HELMET.get()), new ItemStack(ModItems.THAUMIUM_CHESTPLATE.get()), new ItemStack(ModItems.THAUMIUM_LEGGINGS.get()), new ItemStack(ModItems.THAUMIUM_BOOTS.get())};
                case 2 -> voidArmor(false);
                case 3 -> voidArmor(true);
                default -> fortress(index == 5);
            };
            equip(player, stacks);
            close(helper, player.getArmorValue(), new int[]{15, 15, 19, 19, 23, 24}[index], "armor points tier " + index);
            close(helper, player.getAttributeValue(Attributes.ARMOR_TOUGHNESS), index < 2 ? 0 : 8, "toughness tier " + index);
            close(helper, melee(helper, player, 10), new double[]{6, 6, 3.4, 3.4, 2, 2}[index], "ten-damage hit tier " + index);
            player.invulnerableTime = 0;
            player.setHealth(20);
            close(helper, melee(helper, player, 20), new double[]{16, 16, 8.8, 8.8, 5.6, 4.8}[index], "twenty-damage hit tier " + index);
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void cultistAndTravellerToughness(GameTestHelper helper) throws Exception {
        for (boolean plate : new boolean[]{false, true}) {
            ServerPlayer cultist = player(helper);
            equip(cultist, new ItemStack(plate ? ModItems.CRIMSON_CULT_HELM.get() : ModItems.CRIMSON_CULT_HOOD.get()),
                new ItemStack(plate ? ModItems.CRIMSON_CULT_CHESTPLATE.get() : ModItems.CRIMSON_CULT_ROBE.get()),
                new ItemStack(plate ? ModItems.CRIMSON_CULT_GREAVES.get() : ModItems.CRIMSON_CULT_LEGGINGS.get()), new ItemStack(ModItems.CRIMSON_CULT_BOOTS.get()));
            close(helper, cultist.getArmorValue(), 15, "ordinary cultist iron defense");
            close(helper, cultist.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 0, "ordinary cultist has no toughness");
            close(helper, melee(helper, cultist, 10), 6, "ordinary cultist matches iron");
        }
        ServerPlayer praetor = player(helper);
        equip(praetor, new ItemStack(ModItems.CRIMSON_PRAETOR_HELM.get()), new ItemStack(ModItems.CRIMSON_PRAETOR_CHESTPLATE.get()),
            new ItemStack(ModItems.CRIMSON_PRAETOR_GREAVES.get()), new ItemStack(ModItems.CRIMSON_CULT_BOOTS.get()));
        close(helper, praetor.getArmorValue(), 18, "praetor has no fortress set bonus");
        close(helper, praetor.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 6, "praetor three pieces give six toughness");
        close(helper, melee(helper, praetor, 10), 10 * (1 - (18 - 10 / 3.5) / 25), "praetor follows vanilla formula");
        ServerPlayer traveller = player(helper);
        ItemStack[] set = fortress(false);
        set[3] = new ItemStack(ModItems.BOOTS_TRAVELLER.get());
        equip(traveller, set);
        close(helper, traveller.getArmorValue(), 21, "traveller boots add only their own one armor point");
        close(helper, traveller.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 6, "traveller boots have no toughness");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void fortressBonusTracksPiecesMaskAndRemoval(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        equip(player, new ItemStack(ModItems.FORTRESS_HELMET.get()));
        helper.assertTrue(ModernArmorProtection.fortressBonus(player) == 0, "one piece has no set bonus");
        changeSlot(player, EquipmentSlot.CHEST, new ItemStack(ModItems.FORTRESS_CHESTPLATE.get()));
        ModernArmorProtection.refreshFortressBonus(player);
        helper.assertTrue(ModernArmorProtection.fortressBonus(player) == 1, "helmet+chest rounds 10*0.125 to one");
        changeSlot(player, EquipmentSlot.HEAD, ItemStack.EMPTY);
        changeSlot(player, EquipmentSlot.LEGS, new ItemStack(ModItems.FORTRESS_LEGGINGS.get()));
        ModernArmorProtection.refreshFortressBonus(player);
        helper.assertTrue(ModernArmorProtection.fortressBonus(player) == 2, "chest+legs rounds 13*0.125 to two");
        ItemStack helmet = new ItemStack(ModItems.FORTRESS_HELMET.get());
        changeSlot(player, EquipmentSlot.HEAD, helmet);
        ModernArmorProtection.refreshFortressBonus(player);
        close(helper, player.getAttributeValue(Attributes.ARMOR), 20, "set bonus applied once to all three pieces");
        InfusionKeys.TAGS.set(helmet, Map.of("mask", "0"));
        ModernArmorProtection.refreshFortressBonus(player);
        close(helper, player.getAttributeValue(Attributes.ARMOR), 21, "in-place mask update grants one armor");
        for (EquipmentSlot slot : SLOTS) {
            changeSlot(player, slot, ItemStack.EMPTY);
        }
        ModernArmorProtection.refreshFortressBonus(player);
        close(helper, player.getAttributeValue(Attributes.ARMOR), 0, "removal cleans bonus without accumulating modifiers");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void magicArmorIsScopedAndWeighted(GameTestHelper helper) throws Exception {
        for (int index = 0; index < 4; index++) {
            ServerPlayer player = player(helper);
            equip(player, index < 2 ? fortress(index == 1) : voidArmor(index == 2));
            float before = player.getHealth();
            player.hurt(player.damageSources().magic(), 10);
            close(helper, before - player.getHealth(), new double[]{30.0 / 7, 10 * (1 - 20.8 / 35), 38.0 / 7, 10}[index], "magic resistance " + index);
            player.invulnerableTime = 0;
            before = player.getHealth();
            player.hurt(player.damageSources().wither(), 2);
            close(helper, before - player.getHealth(), 2, "wither is not magic armor damage");
        }
        ServerPlayer partial = player(helper);
        equip(partial, new ItemStack(ModItems.FORTRESS_HELMET.get()));
        close(helper, ModernArmorProtection.magicProtection(partial), 3.0 / 35, "single-piece magic contribution");
        close(helper, ModernArmorProtection.fireExplosionProtection(partial), 0.25 * 3 / 16, "single-piece fire contribution");
        helper.assertTrue(partial.damageSources().thorns(new Zombie(helper.getLevel())).is(ModernArmorProtection.MAGIC), "thorns retains the legacy magic classification");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void fortressFireAndExplosionAreNotImmunity(GameTestHelper helper) throws Exception {
        for (boolean explosion : new boolean[]{false, true}) {
            ServerPlayer player = player(helper);
            equip(player, fortress(false));
            float before = player.getHealth();
            player.hurt(explosion ? player.damageSources().explosion(null, null) : player.damageSources().inFire(), 10);
            close(helper, before - player.getHealth(), 1.5, "25 percent of remaining damage removed, not immunity");
            for (EquipmentSlot slot : SLOTS) {
                helper.assertTrue(player.getItemBySlot(slot).getDamageValue() == 2, "special pieces now use vanilla wear");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void modernArmorStacksWithEnchantmentsAndResistance(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        ItemStack[] stacks = fortress(false);
        for (ItemStack stack : stacks) {
            stack.enchant(player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION), 4);
        }
        equip(player, stacks);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200));
        float before = player.getHealth();
        player.hurt(player.damageSources().explosion(null, null), 10);
        close(helper, before - player.getHealth(), 2 * 0.8 * 0.36 * 0.75, "vanilla resistance and Protection IV remain effective");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void modernArmorWearAndSelfRepair(GameTestHelper helper) throws Exception {
        for (boolean robes : new boolean[]{false, true}) {
            ServerPlayer player = player(helper);
            ItemStack[] stacks = voidArmor(robes);
            for (ItemStack stack : stacks) {
                stack.setDamageValue(stack.getMaxDamage() - 1);
            }
            equip(player, stacks);
            close(helper, melee(helper, player, 10), 3.4, "nearly broken armor follows modern protection on its last hit");
            for (EquipmentSlot slot : SLOTS) {
                helper.assertTrue(player.getItemBySlot(slot).isEmpty(), "last durability is consumed by vanilla wear");
            }
            ItemStack repaired = robes ? new ItemStack(ModItems.VOID_ROBE_CHESTPLATE.get()) : new ItemStack(ModItems.VOID_CHESTPLATE.get());
            repaired.setDamageValue(10);
            player.tickCount = 20;
            repaired.getItem().inventoryTick(repaired, helper.getLevel(), player, 0, false);
            helper.assertTrue(repaired.getDamageValue() == 9, "self repair still restores one durability per second");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void axesHitHarderThanMatchingSwords(GameTestHelper helper) throws Exception {
        net.minecraft.world.item.Item[][] pairs = {
            {ModItems.THAUMIUM_SWORD.get(), ModItems.THAUMIUM_AXE.get()},
            {ModItems.VOID_SWORD.get(), ModItems.VOID_AXE.get()},
            {ModItems.ELEMENTAL_SWORD.get(), ModItems.ELEMENTAL_AXE.get()}
        };
        Field cooldown = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("attackStrengthTicker");
        cooldown.setAccessible(true);
        for (int material = 0; material < pairs.length; material++) {
            for (int weapon = 0; weapon < 2; weapon++) {
                ServerPlayer player = player(helper);
                ItemStack stack = new ItemStack(pairs[material][weapon]);
                changeSlot(player, EquipmentSlot.MAINHAND, stack);
                float expected = (material == 0 ? 6 : 7) + weapon * 3;
                close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE), expected, "weapon damage attribute");
                double expectedSpeed = weapon == 0 ? 1.6 : material == 0 ? 0.9 : 1.0;
                close(helper, player.getAttributeValue(Attributes.ATTACK_SPEED), expectedSpeed, "weapon attack speed");
                var target = new net.minecraft.world.entity.animal.Pig(net.minecraft.world.entity.EntityType.PIG, helper.getLevel());
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
                target.setHealth(100);
                player.setOnGround(true);
                cooldown.setInt(player, 100);
                player.attack(target);
                close(helper, 100 - target.getHealth(), expected, "fully charged attack without armor or critical hit");
            }
        }
        ServerPlayer crusherUser = player(helper);
        changeSlot(crusherUser, EquipmentSlot.MAINHAND, new ItemStack(ModItems.PRIMAL_CRUSHER.get()));
        close(helper, crusherUser.getAttributeValue(Attributes.ATTACK_SPEED), 1.6, "primal crusher matches sword speed");
        close(helper, crusherUser.getAttributeValue(Attributes.ATTACK_DAMAGE), 7.5, "primal crusher damage unchanged");
        helper.succeed();
    }
}
