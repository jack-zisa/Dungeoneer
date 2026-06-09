package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.maps.tiled.TiledMap;
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
            return DungeonMapUtils.deserializeMap(blob, Tiles.TILESET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
