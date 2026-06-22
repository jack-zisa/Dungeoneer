package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.client.util.RenderLayer;
import dev.creoii.dungeoneer.client.util.RenderUtils;
import dev.creoii.dungeoneer.client.util.Renderable;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Direction;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientDungeonMap implements Disposable {
    public static final float WALL_HEIGHT = -4f;
    public static final float TILE_SIZE = 8f;
    private TiledMap map;
    private List<WallTop> wallTops;
    private List<WallFace> wallFaces;

    public void build(Dungeoneer client, byte[] mapData) {
        try {
            map = DungeonMapUtils.deserializeMap(mapData, ClientTiles.TILESET);
            wallTops = new ArrayList<>();
            wallFaces = new ArrayList<>();

            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(Constants.MAP_LAYER_WALL);
            for (int x = 0; x < layer.getWidth(); ++x) {
                for (int y = 0; y < layer.getHeight(); ++y) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        TiledMapTile tile = cell.getTile();
                        TextureRegion texture = tile.getTextureRegion();

                        String topTextureId = DataManager.getTile(ClientTiles.getTileId(tile)).id() + "_top";
                        Texture top = client.getAssets().getTexture(Assets.Atlas.TILE, topTextureId);
                        TextureRegion topTexture = top == Assets.MISSING_TEXTURE ? texture : new TextureRegion(top);

                        wallTops.add(new WallTop(topTexture, x, y));

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
        wallTops.clear();
    }

    public List<WallTop> getWallTops() {
        return wallTops;
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
        float rad = angle * MathUtils.degreesToRadians;
        x -= originX;
        y -= originY;
        float rx = x * MathUtils.cos(rad) - y * MathUtils.sin(rad);
        float ry = x * MathUtils.sin(rad) + y * MathUtils.cos(rad);
        rx += z * .15f;
        ry -= z * .85f;
        rx += originX;
        ry += originY;
        return new Vector2(rx, ry);
    }

    public record WallTop(TextureRegion texture, int x, int y) implements Renderable {
        @Override
        public RenderLayer renderLayer() {
            return RenderLayer.OBJECT;
        }

        @Override
        public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
            RenderUtils.drawWallTop(camera, batch, texture, x, y, rotation);
        }

        @Override
        public float depth(float rotation, OrthographicCamera camera) {
            float worldX = x * ClientDungeonMap.TILE_SIZE;
            float worldY = y * ClientDungeonMap.TILE_SIZE;
            Vector2 p = ClientDungeonMap.project(worldX, worldY, ClientDungeonMap.WALL_HEIGHT, rotation, camera.position.x, camera.position.y);
            return p.y;
        }
    }

    public record WallFace(TextureRegion texture, int x, int y, Direction direction) implements Renderable {
        @Override
        public RenderLayer renderLayer() {
            return RenderLayer.OBJECT;
        }

        @Override
        public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
            RenderUtils.drawWall(camera, batch, this, rotation);
        }

        @Override
        public float depth(float rotation, OrthographicCamera camera) {
            float worldX = x * ClientDungeonMap.TILE_SIZE;
            float worldY = y * ClientDungeonMap.TILE_SIZE;
            Vector2 p = ClientDungeonMap.project(worldX, worldY, ClientDungeonMap.WALL_HEIGHT, rotation, camera.position.x, camera.position.y);
            return p.y;
        }

        public static boolean isVisible(Direction direction, float rotation) {
            Vector2 normal = new Vector2(direction.getVector()[0], direction.getVector()[1]);
            normal.rotateDeg(rotation);
            return normal.y <= 0f;
        }
    }
}
