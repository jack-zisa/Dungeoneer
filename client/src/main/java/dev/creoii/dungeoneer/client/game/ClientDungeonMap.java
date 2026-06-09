package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.client.screen.editor.Tiles;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.IOException;

public class ClientDungeonMap implements Disposable {
    private TiledMap map;

    public void build(byte[] mapData) {
        try {
            TiledMapTileSet tiledMapTileSet = new TiledMapTileSet();
            tiledMapTileSet.putTile(1, Tiles.STONE);
            tiledMapTileSet.putTile(2, Tiles.DIRT);
            tiledMapTileSet.putTile(3, Tiles.GRASS);
            tiledMapTileSet.putTile(4, Tiles.SAND);
            tiledMapTileSet.putTile(5, Tiles.LAVA);
            map = DungeonMapUtils.deserializeMap(mapData, tiledMapTileSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TiledMap getMap() {
        return map;
    }

    public void clearMap() {
        map = null;
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
