package dev.creoii.dungeoneer.util;

import com.mojang.serialization.Codec;

public enum RemovalReason {
    SURRENDER,
    DEATH,
    CANCEL,
    DISCONNECTED,
    UNKNOWN;

    public static final Codec<RemovalReason> CODEC = Codec.STRING.xmap(s -> RemovalReason.valueOf(s.toUpperCase()), reason -> reason.name().toLowerCase());
}
