package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.DungeonMap;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.*;
import java.time.LocalDateTime;

public class DungeonMapManager {
    private final Dungeoneer client;
    private TiledMap map;

    public DungeonMapManager(Dungeoneer client) {
        this.client = client;
    }

    public void init(TiledMap map) {
        this.map = map;
    }

    public void save() {
        try {
            byte[] blob = DungeonMapUtils.serializeMap(map);
            client.getState().setDungeonMap(new DungeonMap(-1L, client.getState().getAccount().id(), blob, LocalDateTime.now()));
            client.get().sendTCP(new SaveDungeonMapC2S(client.getState().getAccount().id(), blob));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TiledMap read(byte[] blob) {
        try {
            TiledMapTileSet tiledMapTileSet = new TiledMapTileSet();
            tiledMapTileSet.putTile(1, Tiles.STONE);
            tiledMapTileSet.putTile(2, Tiles.DIRT);
            tiledMapTileSet.putTile(3, Tiles.GRASS);
            tiledMapTileSet.putTile(4, Tiles.SAND);
            tiledMapTileSet.putTile(5, Tiles.LAVA);
            return DungeonMapUtils.deserializeMap(blob, tiledMapTileSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
