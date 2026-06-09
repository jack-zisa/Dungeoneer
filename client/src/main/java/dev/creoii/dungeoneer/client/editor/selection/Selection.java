package dev.creoii.dungeoneer.client.editor.selection;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.function.Consumer;

public interface Selection {
    Type getType();

    void clear();

    void forEach(TiledMapTileLayer layer, Consumer<TiledMapTileLayer.Cell> action);

    enum Type {
        AREA
    }
}
