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
import dev.creoii.dungeoneer.client.render.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Direction;
import dev.creoii.dungeoneer.util.DungeonMapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientDungeonMap implements DungeonMap, Disposable {
    public static final float WALL_HEIGHT = -4f;
    public static final float TILE_SIZE = 8f;
    private final Dungeoneer client;
    private final Map<Integer, String> tileIds;
    private DungeonMapDefinition definition;
    private DungeonMapTemplate template;
    private OrthogonalTiledMapRenderer mapRenderer;
    private List<WallTopRenderable> wallTops;
    private List<WallFaceRenderable> wallFaces;

    public ClientDungeonMap(Dungeoneer client) {
        this.client = client;
        tileIds = new HashMap<>();
    }

    public void build(Dungeoneer client, long seed, String templateId, String tilesetId) {
        template = DataManager.getMapTemplate(templateId);
        mapRenderer = new OrthogonalTiledMapRenderer(DungeonMapUtils.deserializeMap2(seed, template, tilesetId, ClientTiles.TILESET, ClientTiles::getTile));

        wallTops = new ArrayList<>();
        wallFaces = new ArrayList<>();

        TiledMapTileLayer layer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_WALL);
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

    @Override
    public Map<Integer, String> getTileIds() {
        return tileIds;
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

    public boolean isSolid(int tileX, int tileY, boolean bounded) {
        TiledMapTileLayer wallLayer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_WALL);
        if (bounded && (tileX < 0 || tileY < 0 || tileX >= wallLayer.getWidth() || tileY >= wallLayer.getHeight())) {
            return true;
        }

        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);
        return cell != null;
    }

    public void save() {
        client.get().sendTCP(new SaveDungeonMapC2S(client.getState().getAccount().id(), definition.templateId(), definition.tilesetId()));
    }
}
