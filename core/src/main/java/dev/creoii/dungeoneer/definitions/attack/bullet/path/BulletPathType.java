package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.Codec;

public enum BulletPathType {
    EMPTY,
    STRAIGHT,
    WAVY,
    ORBIT,
    SEGMENTED,
    PARAMETRIC;

    public static final Codec<BulletPathType> CODEC = Codec.STRING.xmap(s -> BulletPathType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
}
