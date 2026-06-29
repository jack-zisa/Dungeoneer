package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;

import java.util.Map;

public interface DungeonMap {
    DungeonMapDefinition get();

    void set(DungeonMapDefinition definition);

    DungeonMapTemplate getTemplate();

    Map<Integer, String> getTileIds();
}
