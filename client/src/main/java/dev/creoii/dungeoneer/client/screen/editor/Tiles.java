package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import dev.creoii.dungeoneer.client.AssetManager;

public final class Tiles {
    public static final StaticTiledMapTile DIRT = new StaticTiledMapTile(AssetManager.DIRT_TEXTURE);
    public static final StaticTiledMapTile GRASS = new StaticTiledMapTile(AssetManager.GRASS_TEXTURE);
    public static final StaticTiledMapTile LAVA = new StaticTiledMapTile(AssetManager.LAVA_TEXTURE);
    public static final StaticTiledMapTile SAND = new StaticTiledMapTile(AssetManager.SAND_TEXTURE);
    public static final StaticTiledMapTile STONE = new StaticTiledMapTile(AssetManager.STONE_TEXTURE);

    static {
        STONE.setId(1);
        DIRT.setId(2);
        GRASS.setId(3);
        SAND.setId(4);
        LAVA.setId(5);
    }
}
