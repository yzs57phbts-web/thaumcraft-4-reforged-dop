package thaumcraft.entity.golem;

public record GolemStats(int health, int armor, float speed, boolean fireResist, int upgrades, int carry, int regenDelay, int strength, boolean heavy) {
    private static final GolemStats[] DEFAULTS = java.util.Arrays.stream(GolemType.values())
        .map(type -> new GolemStats(type.health, type.armor, type.speed, type.fireResist, type.upgrades, type.carry, type.regenDelay, type.strength, type.ordinal() >= 5))
        .toArray(GolemStats[]::new);

    public static GolemStats of(GolemType type) {
        return DEFAULTS[type.ordinal()];
    }
}
