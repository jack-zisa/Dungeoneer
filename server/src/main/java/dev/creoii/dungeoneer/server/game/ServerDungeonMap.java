package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.definitions.map.tile.TileSetter;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import dev.creoii.dungeoneer.util.Constants;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class ServerDungeonMap implements DungeonMap {
    private final DungeonMapDefinition definition;
    private final long seed;
    private final DungeonMapTemplate template;
    private final int[][] ground;
    private final int[][] walls;
    private final int[][] objects;
    private final int[][] overlays;
    private final TileSetter[] setters;

    public ServerDungeonMap(DungeonMapDefinition definition, long seed) {
        this.definition = definition;
        this.seed = seed;
        template = DataManager.getMapTemplate(definition.templateId());
        ground = new int[256][256];
        walls = new int[256][256];
        objects = new int[256][256];
        overlays = new int[256][256];

        setters = new TileSetter[]{
            (_, x, y, tileId) -> {
                ground[x][y] = (int) DataManager.getInternalId(DataManager.SchemaType.TILE, tileId);
            },
            (_, x, y, tileId) -> {
                objects[x][y] = (int) DataManager.getInternalId(DataManager.SchemaType.TILE, tileId);
            },
            (_, x, y, tileId) -> {
                walls[x][y] = (int) DataManager.getInternalId(DataManager.SchemaType.TILE, tileId);
            },
            (_, x, y, tileId) -> {
                overlays[x][y] = (int) DataManager.getInternalId(DataManager.SchemaType.TILE, tileId);
            }
        };

        template.layers().forEach((mapLayerType, mapGenerator) -> mapGenerator.apply(null, mapLayerType, seed, new Random(), setters[mapLayerType.ordinal()]));
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

    public long getSeed() {
        return seed;
    }

    @Override
    public void set(DungeonMapDefinition definition) {
    }

    @Override
    public DungeonMapTemplate getTemplate() {
        return template;
    }

    @Override
    @Nullable
    public Tile getTileAt(MapLayerType layer, int tileX, int tileY) {
        if (tileX < 0 || tileX > 255 || tileY < 0 || tileY > 255)
            return null;
        int id = switch (layer) {
            case GROUND -> ground[tileX][tileY];
            case OBJECT -> objects[tileX][tileY];
            case WALL -> walls[tileX][tileY];
            case OVERLAY -> overlays[tileX][tileY];
        };
        if (id == 0)
            return null;
        return DataManager.getTile(id);
    }

    @Override
    public boolean isSolid(int tileX, int tileY, boolean bounded) {
        if (bounded && (tileX < 0 || tileY < 0 || tileX >= walls.length || tileY >= walls[0].length)) {
            return true;
        }

        Tile tile = getTileAt(MapLayerType.WALL, tileX, tileY);
        return tile != null;
    }
}
