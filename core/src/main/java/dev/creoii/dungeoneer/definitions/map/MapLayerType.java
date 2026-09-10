package dev.creoii.dungeoneer.definitions.map;

import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.util.Identifiable;

public enum MapLayerType implements Identifiable {
    GROUND("ground"),
    OBJECT("object"),
    WALL("wall"),
    OVERLAY("overlay");

    public static final Codec<MapLayerType> CODEC = Codec.STRING.xmap(s -> MapLayerType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    private final String id;

    MapLayerType(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Identifiable withId(String id) {
        return this;
    }
}
