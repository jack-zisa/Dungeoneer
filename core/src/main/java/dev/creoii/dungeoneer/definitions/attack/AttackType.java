package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.Codec;

public enum AttackType {
    BULLET;

    public static final Codec<AttackType> CODEC = Codec.STRING.xmap(s -> AttackType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
}
