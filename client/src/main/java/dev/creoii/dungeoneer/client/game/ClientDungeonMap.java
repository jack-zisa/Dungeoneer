package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.object.WallFaceRenderable;
import dev.creoii.dungeoneer.client.render.object.WallTopRenderable;
import dev.creoii.dungeoneer.client.render.ui.editor.ClientTiles;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Direction;
import dev.creoii.dungeoneer.util.DungeonMapUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClientDungeonMap implements DungeonMap, Disposable {
    public static final float WALL_HEIGHT = -4f;
    public static final float TILE_SIZE = 8f;
    private final Dungeoneer client;
    private DungeonMapDefinition definition;
    private DungeonMapTemplate template;
    private OrthogonalTiledMapRenderer mapRenderer;
    private List<WallTopRenderable> wallTops;
    private List<WallFaceRenderable> wallFaces;

    public ClientDungeonMap(Dungeoneer client) {
        this.client = client;
    }

    public void build(Dungeoneer client, long seed, String templateId, String tilesetId) {
        template = DataManager.getMapTemplate(templateId);
        mapRenderer = new OrthogonalTiledMapRenderer(DungeonMapUtils.deserializeMap2(seed, template, tilesetId, ClientTiles.TILESET, ClientTiles.SETTER));

        wallTops = new ArrayList<>();
        wallFaces = new ArrayList<>();

        TiledMapTileLayer layer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get(MapLayerType.WALL.id());
        if (layer == null)
            return;

        for (int x = 0; x < layer.getWidth(); ++x) {
            for (int y = 0; y < layer.getHeight(); ++y) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    TiledMapTile tile = cell.getTile();
                    TextureRegion texture = tile.getTextureRegion();

                    String topTextureId = DataManager.getTile(ClientTiles.getTileId(tile)).id() + "_top";
                    Texture top = client.getAssets().getTexture(Assets.Atlas.TILE, topTextureId);
                    TextureRegion topTexture = top == Assets.MISSING_TEXTURE ? texture : new TextureRegion(top);

                    wallTops.add(new WallTopRenderable(topTexture, x, y));

                    if (layer.getCell(x + 1, y) == null) wallFaces.add(new WallFaceRenderable(texture, x, y, Direction.RIGHT));
                    if (layer.getCell(x - 1, y) == null) wallFaces.add(new WallFaceRenderable(texture, x, y, Direction.LEFT));
                    if (layer.getCell(x, y + 1) == null) wallFaces.add(new WallFaceRenderable(texture, x, y, Direction.UP));
                    if (layer.getCell(x, y - 1) == null) wallFaces.add(new WallFaceRenderable(texture, x, y, Direction.DOWN));
                }
            }
        }
    }

    @Override
    public int getWidth() {
        return Constants.MAP_WIDTH;
    }

    @Override
    public int getHeight() {
        return Constants.MAP_HEIGHT;
    }

    @Override
    public DungeonMapDefinition get() {
        return definition;
    }

    @Override
    public void set(DungeonMapDefinition definition) {
        this.definition = definition;
    }

    @Override
    public DungeonMapTemplate getTemplate() {
        return template;
    }

    public TiledMap getMap() {
        return mapRenderer.getMap();
    }

    public void setMap(TiledMap map) {
        mapRenderer.setMap(map);
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public void setMapRenderer(OrthogonalTiledMapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
    }

    public void clear() {
        mapRenderer.setMap(null);
        wallFaces.clear();
        wallTops.clear();
    }

    public List<WallTopRenderable> getWallTops() {
        return wallTops;
    }

    public List<WallFaceRenderable> getWallFaces() {
        return wallFaces;
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
    }

    @Override
    @Nullable
    public Tile getTileAt(MapLayerType layer, int tileX, int tileY) {
        TiledMapTileLayer layer1 = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get(layer.id());
        if (tileX < 0 || tileY < 0 || tileX >= layer1.getWidth() || tileY >= layer1.getHeight()) {
            return null;
        }
        TiledMapTileLayer.Cell cell = layer1.getCell(tileX, tileY);
        return cell != null ? DataManager.getTile(cell.getTile().getId()) : null;
    }

    @Override
    public boolean isSolid(int tileX, int tileY, boolean bounded) {
        TiledMapTileLayer wallLayer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get(MapLayerType.WALL.id());
        if (bounded && (tileX < 0 || tileY < 0 || tileX >= wallLayer.getWidth() || tileY >= wallLayer.getHeight())) {
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

    public void save() {
        client.get().sendTCP(new SaveDungeonMapC2S(client.getState().getAccount().id(), definition.templateId(), definition.tilesetId()));
    }
}
