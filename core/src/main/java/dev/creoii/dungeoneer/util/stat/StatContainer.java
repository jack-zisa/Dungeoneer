package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public record StatContainer(Stat health, Stat speed, Stat attackSpeed) {
    public static final Codec<StatContainer> INT_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(map -> new StatContainer(
        map.getOrDefault(Stat.Type.HEALTH.name().toLowerCase(), 0),
        map.getOrDefault(Stat.Type.SPEED.name().toLowerCase(), 0),
        map.getOrDefault(Stat.Type.ATTACK_SPEED.name().toLowerCase(), 0)
    ), statContainer -> {
        Map<String, Integer> map = new HashMap<>();
        map.put(Stat.Type.HEALTH.name().toLowerCase(), statContainer.health.value());
        map.put(Stat.Type.SPEED.name().toLowerCase(), statContainer.speed.value());
        map.put(Stat.Type.ATTACK_SPEED.name().toLowerCase(), statContainer.attackSpeed.value());
        return map;
    });public static final StatContainer DEFAULT_STAT_CONTAINER = new StatContainer(200, 100, 1);
    public static final StatContainer ZERO = new StatContainer();

    public StatContainer() {
        this(0, 0, 0);
    }

    public StatContainer(int health, int speed, int attackSpeed) {
        this(new Stat(Stat.Type.HEALTH, health), new Stat(Stat.Type.SPEED, speed), new Stat(Stat.Type.ATTACK_SPEED, attackSpeed));
    }

    public void setSpeed(int speed) {
        this.speed.set(speed);
    }

    public void setHealth(int health) {
        this.health.set(health);
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed.set(attackSpeed);
    }

    public void set(StatContainer other) {
        setHealth(other.health.base());
        setSpeed(other.speed.base());
        setAttackSpeed(other.attackSpeed.base());
    }

    public StatContainer copy() {
        return new StatContainer(health, speed, attackSpeed);
    }
}
