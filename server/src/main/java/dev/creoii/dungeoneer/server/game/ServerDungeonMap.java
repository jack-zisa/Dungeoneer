package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ServerDungeonMap implements DungeonMap {
    private final DungeonMapDefinition definition;
    private final DungeonMapTemplate template;
    private final Map<Integer, String> tileIds;
    private final int[][] ground;
    private final int[][] walls;
    private final int[][] objects;
    private final int[][] overlays;

    public ServerDungeonMap(DungeonMapDefinition definition) {
        this.definition = definition;
        template = DataManager.getMapTemplate(definition.templateId());
        tileIds = new HashMap<>();
        ground = new int[256][256];
        walls = new int[256][256];
        objects = new int[256][256];
        overlays = new int[256][256];
    }

    @Override
    public DungeonMapDefinition get() {
        return definition;
    }

    @Override
    public void set(DungeonMapDefinition definition) {
    }

    @Override
    public DungeonMapTemplate getTemplate() {
        return template;
    }

    @Override
    public Map<Integer, String> getTileIds() {
        return tileIds;
    }

    @Nullable
    public Tile getTileAt(MapLayerType layer, int tileX, int tileY) {
        if (tileX < 0 || tileX > 255 || tileY < 0 || tileY > 255)
            return null;
        int id = switch (layer) {
            case GROUND -> ground[tileX][tileY];
            case OBJECT -> objects[tileX][tileY];
            case WALL -> walls[tileX][tileY];
            case OVERLAY -> overlays[tileX][tileY];
            default -> throw new IllegalArgumentException("Unknown tile layer: " + layer);
        };
        if (id == 0)
            return null;
        return DataManager.getTile(tileIds.get(id));
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
