package dev.creoii.dungeoneer.server.util;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Tile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class ServerTiles {
    public static final Int2ObjectMap<Tile> TILES_BY_ID = new Int2ObjectOpenHashMap<>();

    public static void load() {
        DataManager.getTiles().forEach((_, identifiable) -> {
            Tile tile = (Tile) identifiable;
            TILES_BY_ID.put(tile.tileId(), tile);
        });
    }
}
