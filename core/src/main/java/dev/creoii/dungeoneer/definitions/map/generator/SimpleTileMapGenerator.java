package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.provider.tileprovider.SimpleTileProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

import java.util.Random;
import java.util.function.Function;

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
    public void apply(TiledMapTileLayer layer, DungeonMapTemplate.LayerType layerType, Random random, Function<String, TiledMapTile> tileFunction) {
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);

        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(tileFunction.apply(tile.get(random).id()));
        layer.setCell(x, y, cell);
    }
}
