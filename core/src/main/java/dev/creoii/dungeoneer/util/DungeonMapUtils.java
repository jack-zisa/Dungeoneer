package dev.creoii.dungeoneer.util;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.Tile;
import dev.creoii.dungeoneer.definitions.Tileset;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

import java.io.*;
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

        int width = 0;
        int height = 0;

        for (DungeonMapTemplate.Layer layer : template.layers().values()) {
            if (layer.map().length == 0)
                continue;

            height = Math.max(height, layer.map().length);
            width = Math.max(width, layer.map()[0].length);
        }

        for (DungeonMapTemplate.LayerType layerType : DungeonMapTemplate.LayerType.values()) {
            TiledMapTileLayer tiledLayer = new TiledMapTileLayer(width, height, 8, 8);
            DungeonMapTemplate.Layer layer = template.layers().get(layerType);

            if (layer != null) {
                TileProvider provider = switch (layerType) {
                    case GROUND -> tileset.ground();
                    case WALL -> tileset.wall();
                    case OBJECT -> null;
                };

                if (provider == null)
                    continue;

                char[][] chars = layer.map();
                for (int y = 0; y < chars.length; ++y) {
                    for (int x = 0; x < chars[y].length; ++x) {

                        char c = chars[y][x];
                        if (c == ' ')
                            continue;

                        Tile tile = provider.getTile();
                        if (tile == null)
                            continue;

                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tileFunction.apply(tile.id()));
                        tiledLayer.setCell(x, height - y - 1, cell);
                    }
                }
            }

            tiledLayer.setName(layerType.name().toLowerCase());
            map.getLayers().add(tiledLayer);
        }

        map.getTileSets().addTileSet(tileSet);
        return map;
    }
}
