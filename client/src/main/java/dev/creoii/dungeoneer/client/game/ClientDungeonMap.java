package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.client.screen.editor.Tiles;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.IOException;

public class ClientDungeonMap implements Disposable {
    private TiledMap map;

    public void build(byte[] mapData) {
        try {
            map = DungeonMapUtils.deserializeMap(mapData, Tiles.TILESET);
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
