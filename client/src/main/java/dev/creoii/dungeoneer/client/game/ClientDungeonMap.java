package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Direction;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientDungeonMap implements Disposable {
    public static final float WALL_HEIGHT = 8f;
    public static final float TILE_SIZE = 8f;
    private TiledMap map;
    private List<WallFace> wallFaces;

    public void build(Dungeoneer client, byte[] mapData) {
        try {
            map = DungeonMapUtils.deserializeMap(mapData, ClientTiles.TILESET);
            wallFaces = new ArrayList<>();

            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(Constants.MAP_LAYER_WALL);
            for (int x = 0; x < layer.getWidth(); ++x) {
                for (int y = 0; y < layer.getHeight(); ++y) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        TextureRegion texture = cell.getTile().getTextureRegion();

                        if (layer.getCell(x + 1, y) == null) wallFaces.add(new WallFace(texture, x, y, Direction.RIGHT));
                        if (layer.getCell(x - 1, y) == null) wallFaces.add(new WallFace(texture, x, y, Direction.LEFT));
                        if (layer.getCell(x, y + 1) == null) wallFaces.add(new WallFace(texture, x, y, Direction.UP));
                        if (layer.getCell(x, y - 1) == null) wallFaces.add(new WallFace(texture, x, y, Direction.DOWN));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TiledMap getMap() {
        return map;
    }

    public void clear() {
        map = null;
        wallFaces.clear();
    }

    public List<WallFace> getWallFaces() {
        return wallFaces;
    }

    @Override
    public void dispose() {
        map.dispose();
    }

    public boolean isSolid(int tileX, int tileY) {
        TiledMapTileLayer wallLayer = (TiledMapTileLayer) map.getLayers().get(Constants.MAP_LAYER_WALL);
        if (tileX < 0 || tileY < 0 || tileX >= wallLayer.getWidth() || tileY >= wallLayer.getHeight()) {
            return true;
        }

        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);
        return cell != null;
    }

    public static Vector2 project(float x, float y, float z, float angle, float originX, float originY) {
        float dx = x - originX;
        float dy = y - originY;
        float cos = MathUtils.cosDeg(angle);
        float sin = MathUtils.sinDeg(angle);
        float rx = dx * cos - dy * sin;
        float ry = dx * sin + dy * cos;
        return new Vector2(rx + originX, ry + originY - z);
    }

    public record WallFace(TextureRegion texture, float x, float y, Direction direction) {
        public static boolean isVisible(Direction direction, float rotation) {
            Vector2 cameraDir = new Vector2(MathUtils.cosDeg(rotation), MathUtils.sinDeg(rotation));
            return direction.getVector()[1] * cameraDir.x + direction.getVector()[0] * cameraDir.y < 0f;
        }
    }
}
