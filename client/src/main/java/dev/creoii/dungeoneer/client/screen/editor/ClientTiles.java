package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Tile;

import javax.annotation.Nullable;

public final class ClientTiles {
    public static final TiledMapTileSet TILESET = new TiledMapTileSet();
    public static final BiMap<String, TiledMapTile> TILES = HashBiMap.create();

    public static void load(Dungeoneer client) {
        DataManager.getTiles().forEach((_, identifiable) -> {
            Tile tile = (Tile) identifiable;
            TiledMapTile tile1 = new StaticTiledMapTile(new TextureRegion(client.getAssets().getTexture(Assets.Atlas.TILE, tile.id())));
            tile1.setBlendMode(TiledMapTile.BlendMode.NONE);
            tile1.setId(tile.tileId());
            TILES.put(tile.id(), tile1);
            TILESET.putTile(tile.tileId(), tile1);
        });
    }

    @Nullable
    public static TiledMapTile getTile(String id) {
        return TILES.getOrDefault(id, null);
    }
}
