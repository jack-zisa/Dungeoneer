package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;

import java.util.Random;
import java.util.function.Function;

public interface MapGenerator {
    Codec<MapGenerator> TYPE_CODEC = Type.CODEC.dispatch(MapGenerator::getType, type -> switch (type) {
        case EMPTY -> EmptyMapGenerator.CODEC;
        case SIMPLE_TILE -> SimpleTileMapGenerator.CODEC;
        case CIRCLE -> CircleMapGenerator.CODEC;
    });

    Type getType();

    void apply(TiledMapTileLayer layer, MapLayerType layerType, long seed, Random random, Function<String, TiledMapTile> tileFunction);

    enum Type {
        EMPTY,
        SIMPLE_TILE,
        CIRCLE;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
