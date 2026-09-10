package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.mojang.serialization.MapCodec;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.TileSetter;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public record EmptyMapGenerator() implements MapGenerator {
    private static final EmptyMapGenerator INSTANCE = new EmptyMapGenerator();
    public static final MapCodec<EmptyMapGenerator> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Type getType() {
        return Type.EMPTY;
    }

    @Override
    public void apply(@Nullable TiledMapTileLayer layer, MapLayerType layerType, long seed, Random random, TileSetter setter) {
    }
}
