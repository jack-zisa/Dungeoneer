package dev.creoii.dungeoneer.util;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.generator.MapGenerator;
import dev.creoii.dungeoneer.definitions.map.tile.Tileset;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

import java.util.Random;
import java.util.function.Function;

public final class DungeonMapUtils {
    public static TiledMap buildEmptyMap() {
        TiledMap map = new TiledMap();
        for (String layerName : Constants.MAP_LAYERS) {
            TiledMapTileLayer layer = new TiledMapTileLayer(256, 256, 8, 8);
            layer.setName(layerName);
            map.getLayers().add(layer);
        }
        return map;
    }

    public static TiledMap deserializeMap2(String templateId, String tilesetId, TiledMapTileSet tileSet, Function<String, TiledMapTile> tileFunction) {
        TiledMap map = new TiledMap();

        DungeonMapTemplate template = DataManager.getMapTemplate(templateId);
        Tileset tileset = DataManager.getTileset(tilesetId);

        if (template == null || tileset == null) {
            return map;
        }

        for (DungeonMapTemplate.LayerType layerType : DungeonMapTemplate.LayerType.values()) {
            TiledMapTileLayer tiledLayer = new TiledMapTileLayer(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, 8, 8);
            MapGenerator generator = template.layers().get(layerType);

            if (generator != null) {
                TileProvider provider = switch (layerType) {
                    case GROUND -> tileset.ground();
                    case WALL -> tileset.wall();
                    case OBJECT -> null;
                };

                if (provider == null)
                    continue;

                generator.apply(tiledLayer, layerType, new Random(), tileFunction);
            }

            tiledLayer.setName(layerType.name().toLowerCase());
            map.getLayers().add(tiledLayer);
        }

        map.getTileSets().addTileSet(tileSet);
        return map;
    }
}
