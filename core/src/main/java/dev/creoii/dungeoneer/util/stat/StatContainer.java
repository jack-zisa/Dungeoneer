package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record StatContainer(Stat health, Stat speed, Stat attackSpeed) {
    public static final Codec<StatContainer> FLOAT_CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(map -> new StatContainer(
        map.getOrDefault(Stat.Type.HEALTH.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.SPEED.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.ATTACK_SPEED.name().toLowerCase(), 0f)
    ), statContainer -> {
        Map<String, Float> map = new HashMap<>();
        map.put(Stat.Type.HEALTH.name().toLowerCase(), statContainer.health.value());
        map.put(Stat.Type.SPEED.name().toLowerCase(), statContainer.speed.value());
        map.put(Stat.Type.ATTACK_SPEED.name().toLowerCase(), statContainer.attackSpeed.value());
        return map;
    });public static final StatContainer DEFAULT_STAT_CONTAINER = new StatContainer(200, 100, 1);
    public static final StatContainer ZERO = new StatContainer();

    public StatContainer() {
        this(0f, 0f, 0f);
    }

    public StatContainer(float health, float speed, float attackSpeed) {
        this(new Stat(Stat.Type.HEALTH, health), new Stat(Stat.Type.SPEED, speed), new Stat(Stat.Type.ATTACK_SPEED, attackSpeed));
    }

    public void setStat(Stat.Type type, float value) {
        switch (type) {
            case HEALTH -> setHealth(value);
            case SPEED -> setSpeed(value);
            case ATTACK_SPEED -> setAttackSpeed(value);
        }
    }

    public void setSpeed(float speed) {
        this.speed.set(speed);
    }


    public void setHealth(float health) {
        this.health.set(health);
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed.set(attackSpeed);
    }

    public void set(StatContainer other) {
        setHealth(other.health.base());
        setSpeed(other.speed.base());
        setAttackSpeed(other.attackSpeed.base());
    }

    public void applyModifier(ModifierEntry modifier) {
        switch (modifier.type()) {
            case HEALTH -> health.addModifier(modifier);
            case SPEED -> speed.addModifier(modifier);
            case ATTACK_SPEED -> attackSpeed.addModifier(modifier);
        }
    }

    public void removeModifier(Stat.Type type, UUID uuid) {
        switch (type) {
            case HEALTH -> health.removeModifier(uuid);
            case SPEED -> speed.removeModifier(uuid);
            case ATTACK_SPEED -> attackSpeed.removeModifier(uuid);
        }
    }

    public StatContainer copy() {
        return new StatContainer(
            new Stat(Stat.Type.HEALTH, health.base()),
            new Stat(Stat.Type.SPEED, speed.base()),
            new Stat(Stat.Type.ATTACK_SPEED, attackSpeed.base())
        );
    }

    public String toDebugString(StatContainer maxStatContainer) {
        return "H:" + health + "/" + maxStatContainer.health
            + "|S:" + speed + "/" + maxStatContainer.speed
            + "|AS:" + attackSpeed + "/" + maxStatContainer.attackSpeed;
    }
}
