package dev.creoii.dungeoneer.client.editor.action;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import dev.creoii.dungeoneer.client.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.definitions.MapObject;
import dev.creoii.dungeoneer.definitions.Tile;

import javax.annotation.Nullable;

public class SetObjectAction implements EditorAction {
    private final TiledMapTileLayer layer;
    private final int x;
    private final int y;
    @Nullable private final MapObject oldTile;
    private final MapObject newTile;

    public SetObjectAction(TiledMapTileLayer layer, int x, int y, MapObject oldTile, MapObject newTile) {
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.oldTile = oldTile;
        this.newTile = newTile;
    }

    @Override
    public void redo() {
        setTile(newTile);
    }

    @Override
    public void undo() {
        setTile(oldTile);
    }

    private void setTile(MapObject tile) {
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);

        if (tile == null) {
            if (cell != null) {
                cell.setTile(null);
            }
            return;
        }

        if (cell == null) {
            cell = new TiledMapTileLayer.Cell();
            layer.setCell(x, y, cell);
        }

        cell.setTile(ClientTiles.getObject(tile.id()));
    }
}
