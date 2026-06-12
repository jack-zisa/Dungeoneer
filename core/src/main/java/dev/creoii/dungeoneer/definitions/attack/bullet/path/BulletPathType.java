package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.Codec;

public enum BulletPathType {
    STRAIGHT,
    WAVY,
    ORBIT,
    SEGMENTED;

    public static final Codec<BulletPathType> CODEC = Codec.STRING.xmap(s -> BulletPathType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
}
