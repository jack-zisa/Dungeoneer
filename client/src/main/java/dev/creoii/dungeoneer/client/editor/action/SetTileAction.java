package dev.creoii.dungeoneer.client.editor.action;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class SetTileAction implements EditorAction {
    private final TiledMapTileLayer layer;
    private final int x;
    private final int y;

    private final TiledMapTile oldTile;
    private final TiledMapTile newTile;

    public SetTileAction(TiledMapTileLayer layer, int x, int y, TiledMapTile oldTile, TiledMapTile newTile) {
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

    private void setTile(TiledMapTile tile) {
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

        cell.setTile(tile);
    }
}
