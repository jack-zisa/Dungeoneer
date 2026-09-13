package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import org.jspecify.annotations.Nullable;

public interface DungeonMap {
    DungeonMapDefinition get();

    void set(DungeonMapDefinition definition);

    DungeonMapTemplate getTemplate();

    /**
     * @param bounded Whether out-of-bounds positions should be considered solid.
     */
    boolean isSolid(int tileX, int tileY, boolean bounded);

    @Nullable
    Tile getTileAt(MapLayerType layer, int tileX, int tileY);

    int getWidth();

    int getHeight();
}
