package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;

import java.util.Map;

public interface DungeonMap {
    DungeonMapDefinition get();

    void set(DungeonMapDefinition definition);

    Map<Integer, String> getTileIds();
}
