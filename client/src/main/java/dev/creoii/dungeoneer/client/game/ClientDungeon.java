package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import dev.creoii.dungeoneer.client.AssetManager;

public class ClientDungeon {
    private TiledMap map;

    public void build() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer ground = new TiledMapTileLayer(32, 32, 8, 8);
        ground.setName("ground");

        StaticTiledMapTile tile = new StaticTiledMapTile(AssetManager.STONE_TEXTURE);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                ground.setCell(x, y, cell);
            }
        }

        map.getLayers().add(ground);
        this.map = map;
    }

    public TiledMap getMap() {
    return map;
}
}
