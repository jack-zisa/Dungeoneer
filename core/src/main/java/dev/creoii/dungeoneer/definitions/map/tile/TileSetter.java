package dev.creoii.dungeoneer.definitions.map.tile;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TileSetter {
    void set(@Nullable TiledMapTileLayer layer, int x, int y, String tileId);
}
