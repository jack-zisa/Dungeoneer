package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.definitions.Tile;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class Tiles {
    public static final TiledMapTileSet TILESET = new TiledMapTileSet();
    private static final Map<String, TiledMapTile> TILES = new HashMap<>();

    public static void load() {
        DataManager.getTiles().forEach((_, identifiable) -> {
            Tile tile = (Tile) identifiable;
            TextureRegion region = switch (tile.tileId()) {
                case 1 -> AssetManager.STONE_TEXTURE;
                case 2 -> AssetManager.DIRT_TEXTURE;
                case 3 -> AssetManager.GRASS_TEXTURE;
                case 4 -> AssetManager.SAND_TEXTURE;
                case 5 -> AssetManager.LAVA_TEXTURE;
                default -> null;
            };
            if (region != null) {
                TiledMapTile tile1 = new StaticTiledMapTile(region);
                TILES.put(tile.id(), tile1);
                TILESET.putTile(tile.tileId(), tile1);
            }
        });
    }

    @Nullable
    public static TiledMapTile getTile(String id) {
        return TILES.getOrDefault(id, null);
    }
}
