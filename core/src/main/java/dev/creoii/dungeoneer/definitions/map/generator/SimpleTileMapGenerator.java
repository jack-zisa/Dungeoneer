package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.TileSetter;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.provider.tileprovider.SimpleTileProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public record SimpleTileMapGenerator(Vector2 pos, TileProvider tile) implements MapGenerator {
    public static final MapCodec<SimpleTileMapGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codecs.VECTOR_2.fieldOf("pos").orElse(Vector2.Zero).forGetter(SimpleTileMapGenerator::pos),
            TileProvider.CODEC.fieldOf("tile").orElse(SimpleTileProvider.EMPTY).forGetter(SimpleTileMapGenerator::tile)
        ).apply(instance, SimpleTileMapGenerator::new)
    );

    @Override
    public Type getType() {
        return Type.SIMPLE_TILE;
    }

    @Override
    public void apply(@Nullable TiledMapTileLayer layer, MapLayerType layerType, long seed, Random random, TileSetter setter) {
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);

        Context context = new Context()
            .set(ValueType.RANDOM, random)
            .set(ValueType.POSITION, new Vector2(x, y))
            .set(ValueType.SEED, seed);

        setter.set(layer, x, y, tile.get(context).id());
    }
}
