package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record StatContainer(Stat health, Stat defense, Stat speed, Stat dexterity, Stat vitality) {
    public static final Codec<StatContainer> FLOAT_CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(map -> new StatContainer(
        map.getOrDefault(Stat.Type.HEALTH.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.DEFENSE.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.SPEED.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.DEXTERITY.name().toLowerCase(), 0f),
        map.getOrDefault(Stat.Type.VITALITY.name().toLowerCase(), 0f)
    ), statContainer -> {
        Map<String, Float> map = new HashMap<>();
        map.put(Stat.Type.HEALTH.name().toLowerCase(), statContainer.health.value());
        map.put(Stat.Type.DEFENSE.name().toLowerCase(), statContainer.defense.value());
        map.put(Stat.Type.SPEED.name().toLowerCase(), statContainer.speed.value());
        map.put(Stat.Type.DEXTERITY.name().toLowerCase(), statContainer.dexterity.value());
        map.put(Stat.Type.VITALITY.name().toLowerCase(), statContainer.vitality.value());
        return map;
    });
    public static final StatContainer ZERO = new StatContainer();

    public StatContainer() {
        this(0f, 0f, 0f, 0f, 0f);
    }

    public StatContainer(float health, float defense, float speed, float dexterity, float vitality) {
        this(new Stat(Stat.Type.HEALTH, health), new Stat(Stat.Type.DEFENSE, defense), new Stat(Stat.Type.SPEED, speed), new Stat(Stat.Type.DEXTERITY, dexterity), new Stat(Stat.Type.VITALITY, vitality));
    }

    public void setStat(Stat.Type type, float value) {
        switch (type) {
            case HEALTH -> setHealth(value);
            case SPEED -> setSpeed(value);
            case ATTACK_SPEED -> setAttackSpeed(value);
        }
    }
	
    public void setHealth(float health) {
        this.health.set(health);
    }

    public void setDefense(float defense) {
        this.defense.set(defense);
    }

    public void setSpeed(float speed) {
        this.speed.set(speed);
    }

    public void setDexterity(float dexterity) {
        this.dexterity.set(dexterity);
    }

    public void setVitality(float vitality) {
        this.vitality.set(vitality);
    }

    public void set(StatContainer other) {
        setHealth(other.health.base());
        setDefense(other.defense.base());
        setSpeed(other.speed.base());
        setDexterity(other.dexterity.base());
        setVitality(other.vitality.base());
    }

    public void applyModifier(ModifierEntry modifier) {
        switch (modifier.type()) {
            case HEALTH -> health.addModifier(modifier);
            case DEFENSE -> defense.addModifier(modifier);
            case SPEED -> speed.addModifier(modifier);
            case DEXTERITY -> dexterity.addModifier(modifier);
            case VITALITY -> vitality.addModifier(modifier);
        }
    }

    public void removeModifier(Stat.Type type, UUID uuid) {
        switch (type) {
            case HEALTH -> health.removeModifier(uuid);
            case DEFENSE -> defense.removeModifier(uuid);
            case SPEED -> speed.removeModifier(uuid);
            case DEXTERITY -> dexterity.removeModifier(uuid);
            case VITALITY -> vitality.removeModifier(uuid);
        }
    }

    public StatContainer copy() {
        return new StatContainer(
            new Stat(Stat.Type.HEALTH, health.base()),
            new Stat(Stat.Type.DEFENSE, defense.base()),
            new Stat(Stat.Type.SPEED, speed.base()),
            new Stat(Stat.Type.DEXTERITY, dexterity.base()),
            new Stat(Stat.Type.VITALITY, vitality.base())
        );
    }

    public String toDebugString(StatContainer maxStatContainer) {
        return "HP:" + health + "/" + maxStatContainer.health
            + "|DEF:" + defense + "/" + maxStatContainer.defense
            + "|SPD:" + speed + "/" + maxStatContainer.speed
            + "|DEX:" + dexterity + "/" + maxStatContainer.dexterity
            + "|VIT:" + vitality + "/" + maxStatContainer.vitality;
    }
}
