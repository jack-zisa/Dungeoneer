package dev.creoii.dungeoneer.client.editor.selection;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

import java.util.function.Consumer;

public class AreaSelection implements Selection {
    public final Rectangle area;

    public AreaSelection() {
        area = new Rectangle();
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }

    public void setMin(int x, int y) {
        area.setX(x);
        area.setY(y);
    }

    public void setMax(int x, int y) {
        area.setWidth(x - area.x);
        area.setHeight(y - area.y);
    }

    @Override
    public void clear() {
        area.setPosition(0f, 0f);
        area.setSize(0f);
    }

    @Override
    public void forEach(TiledMapTileLayer layer, Consumer<TiledMapTileLayer.Cell> action) {
        int minX = Math.min((int) area.x, (int) (area.x + area.width));
        int maxX = Math.max((int) area.x, (int) (area.x + area.width));

        int minY = Math.min((int) area.y, (int) (area.y + area.height)) - 1;
        int maxY = Math.max((int) area.y, (int) (area.y + area.height)) - 1;

        TiledMapTileLayer.Cell cell;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                cell = layer.getCell(x, y);
                if (cell == null) {
                    cell = new TiledMapTileLayer.Cell();
                    layer.setCell(x, y, cell);
                    action.accept(cell);
                } else action.accept(cell);
            }
        }
    }
}
