package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.serialization.Codec;

public enum BulletDefinitionType {
    SINGLE,
    GROUP;

    public static final Codec<BulletDefinitionType> CODEC = Codec.STRING.xmap(s -> BulletDefinitionType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
}
