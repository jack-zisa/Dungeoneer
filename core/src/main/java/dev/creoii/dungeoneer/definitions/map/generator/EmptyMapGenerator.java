package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.mojang.serialization.MapCodec;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;

import java.util.Random;
import java.util.function.Function;

public record EmptyMapGenerator() implements MapGenerator {
    private static final EmptyMapGenerator INSTANCE = new EmptyMapGenerator();
    public static final MapCodec<EmptyMapGenerator> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Type getType() {
        return Type.EMPTY;
    }

    @Override
    public void apply(TiledMapTileLayer layer, MapLayerType layerType, long seed, Random random, Function<String, TiledMapTile> tileFunction) {
    }
}
