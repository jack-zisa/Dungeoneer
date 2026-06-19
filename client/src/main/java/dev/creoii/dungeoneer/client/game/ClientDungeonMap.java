package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.client.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.IOException;

public class ClientDungeonMap implements Disposable {
    private TiledMap map;

    public void build(byte[] mapData) {
        try {
            map = DungeonMapUtils.deserializeMap(mapData, ClientTiles.TILESET);
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

    public boolean isSolid(int tileX, int tileY) {
        TiledMapTileLayer wallLayer = (TiledMapTileLayer) map.getLayers().get(Constants.MAP_LAYER_WALL);
        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);
        return cell != null;
    }
}
